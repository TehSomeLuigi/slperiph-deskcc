/*
 * SecureTransport.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.simpleframework.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.Status;

import org.simpleframework.transport.trace.Trace;

/**
 * The <code>SecureTransport</code> object provides an implementation of a
 * transport used to send and receive data over SSL. Data read from this
 * transport is decrypted using an <code>SSLEngine</code>. Also, all data is
 * written is encrypted with the same engine. This ensures that data can be send
 * and received in a transparent way.
 * 
 * @author Niall Gallagher
 */
class SecureTransport implements Transport {

    /**
     * This is the transport used to send data over the socket.
     */
    private Transport transport;

    /**
     * This buffer is used to output the data for the SSL sent.
     */
    private ByteBuffer output;

    /**
     * This is the internal buffer used to exchange the SSL data.
     */
    private ByteBuffer input;

    /**
     * This is the internal buffer used to exchange the SSL data.
     */
    private ByteBuffer swap;

    /**
     * This is the SSL engine used to encrypt and decrypt data.
     */
    private SSLEngine engine;

    /**
     * This is the trace that is used to monitor socket activity.
     */
    private Trace trace;

    /**
     * This is used to determine if the transport was closed.
     */
    private boolean closed;

    /**
     * This is used to determine if the end of stream was reached.
     */
    private boolean finished;

    /**
     * Constructor for the <code>SecureTransport</code> object. This is used to
     * create a transport for sending and receiving data over SSL. This must be
     * created with a pipeline that has already performed the SSL handshake and
     * is read to used.
     * 
     * @param transport
     *            this is the transport to delegate operations to
     * @param input
     *            this is the input buffer used to read the data
     * @param swap
     *            this is the swap buffer to be used for reading
     */
    public SecureTransport(Transport transport, ByteBuffer input,
            ByteBuffer swap) {
        this(transport, input, swap, 20480);
    }

    /**
     * Constructor for the <code>SecureTransport</code> object. This is used to
     * create a transport for sending and receiving data over SSL. This must be
     * created with a pipeline that has already performed the SSL handshake and
     * is read to used.
     * 
     * @param transport
     *            this is the transport to delegate operations to
     * @param input
     *            this is the input buffer used to read the data
     * @param swap
     *            this is the swap buffer to be used for reading
     * @param size
     *            this is the size of the buffers to be allocated
     */
    public SecureTransport(Transport transport, ByteBuffer input,
            ByteBuffer swap, int size) {
        this.output = ByteBuffer.allocate(size);
        this.engine = transport.getEngine();
        this.trace = transport.getTrace();
        this.transport = transport;
        this.input = input;
        this.swap = swap;
    }

    /**
     * This is used to acquire the trace object that is associated with the
     * socket. A trace object is used to collection details on what operations
     * are being performed on the socket. For instance it may contain
     * information relating to I/O events or more application specific events
     * such as errors.
     * 
     * @return this returns the trace associated with this socket
     */
    @Override
    public Trace getTrace() {
        return this.trace;
    }

    /**
     * This is used to acquire the SSL engine used for HTTPS. If the pipeline is
     * connected to an SSL transport this returns an SSL engine which can be
     * used to establish the secure connection and send and receive content over
     * that connection. If this is null then the pipeline represents a normal
     * transport.
     * 
     * @return the SSL engine used to establish a secure transport
     */
    @Override
    public SSLEngine getEngine() {
        return this.engine;
    }

    /**
     * This method is used to get the <code>Map</code> of attributes by this
     * pipeline. The attributes map is used to maintain details about the
     * connection. Information such as security credentials to client details
     * can be placed within the attribute map.
     * 
     * @return this returns the map of attributes for this pipeline
     */
    @Override
    public Map getAttributes() {
        return this.transport.getAttributes();
    }

    /**
     * This method is used to acquire the <code>SocketChannel</code> for the
     * connection. This allows the server to acquire the input and output
     * streams with which to communicate. It can also be used to configure the
     * connection and perform various network operations that could otherwise
     * not be performed.
     * 
     * @return this returns the socket used by this HTTP pipeline
     */
    @Override
    public SocketChannel getChannel() {
        return this.transport.getChannel();
    }

    /**
     * This is used to perform a non-blocking read on the transport. If there
     * are no bytes available on the input buffers then this method will return
     * zero and the buffer will remain the same. If there is data and the buffer
     * can be filled then this will return the number of bytes read. Finally if
     * the socket is closed this will return a -1 value.
     * 
     * @param buffer
     *            this is the buffer to append the bytes to
     * 
     * @return this returns the number of bytes that have been read
     */
    @Override
    public int read(ByteBuffer buffer) throws IOException {
        if (this.closed) throw new TransportException("Transport is closed");
        if (this.finished) return -1;
        int count = this.fill(buffer);

        if (count <= 0) return this.process(buffer);
        return count;
    }

    /**
     * This is used to perform a non-blocking read on the transport. If there
     * are no bytes available on the input buffers then this method will return
     * zero and the buffer will remain the same. If there is data and the buffer
     * can be filled then this will return the number of bytes read.
     * 
     * @param buffer
     *            this is the buffer to append the bytes to
     * 
     * @return this returns the number of bytes that have been read
     */
    private int process(ByteBuffer buffer) throws IOException {
        int size = this.swap.position();

        if (size >= 0) {
            this.swap.compact();
        }
        int space = this.swap.remaining();

        if (space > 0) {
            size = this.transport.read(this.swap);

            if (size < 0) {
                this.finished = true;
            }
        }
        if ((size > 0) || (space > 0)) {
            this.swap.flip();
            this.receive();
        }
        return this.fill(buffer);
    }

    /**
     * This is used to fill the provided buffer with data that has been read
     * from the secure socket channel. This enables reading of the decrypted
     * data in chunks that are smaller than the size of the input buffer used to
     * contain the plain text data.
     * 
     * @param buffer
     *            this is the buffer to append the bytes to
     * 
     * @return this returns the number of bytes that have been read
     */
    private int fill(ByteBuffer buffer) throws IOException {
        int space = buffer.remaining();
        int count = this.input.position();

        if (count > 0) {
            if (count > space) {
                count = space;
            }
        }
        return this.fill(buffer, count);

    }

    /**
     * This is used to fill the provided buffer with data that has been read
     * from the secure socket channel. This enables reading of the decrypted
     * data in chunks that are smaller than the size of the input buffer used to
     * contain the plain text data.
     * 
     * @param buffer
     *            this is the buffer to append the bytes to
     * @param count
     *            this is the number of bytes that are to be read
     * 
     * @return this returns the number of bytes that have been read
     */
    private int fill(ByteBuffer buffer, int count) throws IOException {
        this.input.flip();

        if (count > 0) {
            count = this.append(buffer, count);
        }
        this.input.compact();
        return count;
    }

    /**
     * This will append bytes within the transport to the given buffer. Once
     * invoked the buffer will contain the transport bytes, which will have been
     * drained from the buffer. This effectively moves the bytes in the buffer
     * to the end of the packet instance.
     * 
     * @param buffer
     *            this is the buffer containing the bytes
     * @param count
     *            this is the number of bytes that should be used
     * 
     * @return returns the number of bytes that have been moved
     */
    private int append(ByteBuffer buffer, int count) throws IOException {
        ByteBuffer segment = this.input.slice();

        if (this.closed) throw new TransportException("Transport is closed");
        int mark = this.input.position();
        int size = mark + count;

        if (count > 0) {
            this.input.position(size);
            segment.limit(count);
            buffer.put(segment);
        }
        return count;
    }

    /**
     * This is used to perform a non-blocking read on the transport. If there
     * are no bytes available on the input buffers then this method will return
     * zero and the buffer will remain the same. If there is data and the buffer
     * can be filled then this will return the number of bytes read. Finally if
     * the socket is closed this will return a -1 value.
     */
    private void receive() throws IOException {
        int count = this.swap.remaining();

        while (count > 0) {
            SSLEngineResult result = this.engine.unwrap(this.swap, this.input);
            Status status = result.getStatus();

            switch (status) {
                case BUFFER_OVERFLOW:
                case BUFFER_UNDERFLOW:
                    return;
                case CLOSED:
                    throw new TransportException("Transport error " + result);
            }
            count = this.swap.remaining();

            if (count <= 0) {
                break;
            }
        }
    }

    /**
     * This method is used to deliver the provided buffer of bytes to the
     * underlying transport. Depending on the connection type the array may be
     * encoded for SSL transport or send directly. Any implementation may choose
     * to buffer the bytes for performance.
     * 
     * @param buffer
     *            this is the array of bytes to send to the client
     */
    @Override
    public void write(ByteBuffer buffer) throws IOException {
        if (this.closed) throw new TransportException("Transport is closed");
        int capacity = this.output.capacity();
        int ready = buffer.remaining();
        int length = ready;

        while (ready > 0) {
            int size = Math.min(ready, capacity / 2);
            int mark = buffer.position();

            if ((length * 2) > capacity) {
                buffer.limit(mark + size);
            }
            this.send(buffer);
            this.output.clear();
            ready -= size;
        }
    }

    /**
     * This method is used to deliver the provided buffer of bytes to the
     * underlying transport. Depending on the connection type the array may be
     * encoded for SSL transport or send directly. Any implementation may choose
     * to buffer the bytes for performance.
     * 
     * @param buffer
     *            this is the array of bytes to send to the client
     */
    private void send(ByteBuffer buffer) throws IOException {
        SSLEngineResult result = this.engine.wrap(buffer, this.output);
        Status status = result.getStatus();

        switch (status) {
            case BUFFER_OVERFLOW:
            case BUFFER_UNDERFLOW:
            case CLOSED:
                throw new TransportException("Transport error " + status);
            default:
                this.output.flip();
        }
        this.transport.write(this.output);
    }

    /**
     * This method is used to flush the contents of the buffer to the client.
     * This method will block until such time as all of the data has been sent
     * to the client. If at any point there is an error sending the content an
     * exception is thrown.
     */
    @Override
    public void flush() throws IOException {
        if (this.closed) throw new TransportException("Transport is closed");
        this.transport.flush();
    }

    /**
     * This is used to close the sender and the underlying transport. If a close
     * is performed on the sender then no more bytes can be read from or written
     * to the transport and the client will received a connection close on their
     * side.
     */
    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.transport.close();
            this.closed = true;
        }
    }
}
