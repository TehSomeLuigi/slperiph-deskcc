/*
 * ContentTypeParser.java February 2001
 *
 * Copyright (C) 2001, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.parse;

import org.simpleframework.http.ContentType;
import org.simpleframework.util.KeyMap;
import org.simpleframework.util.parse.ParseBuffer;
import org.simpleframework.util.parse.Parser;

/**
 * This provides access to the MIME type parts, that is the primary type, the
 * secondary type and an optional character set parameter. The
 * <code>charset</code> parameter is one of many parameters that can be
 * associated with a MIME type. This however this exposes this parameter with a
 * typed method.
 * <p>
 * The <code>getCharset</code> will return the character encoding the content
 * type is encoded within. This allows the user of the content to decode it
 * correctly. Other parameters can be acquired from this by simply providing the
 * name of the parameter.
 * 
 * @author Niall Gallagher
 */
public class ContentTypeParser extends Parser implements ContentType {

    /**
     * Used to store the characters consumed for the secondary type.
     */
    private ParseBuffer secondary;

    /**
     * Used to store the characters consumed for the primary type.
     */
    private ParseBuffer primary;

    /**
     * Used to store the characters for the charset parameter.
     */
    private ParseBuffer charset;

    /**
     * Used to store the characters consumed for the type.
     */
    private ParseBuffer type;

    /**
     * Used to collect the name of a content type parameter.
     */
    private ParseBuffer name;

    /**
     * Used to collect the value of the content type parameter.
     */
    private ParseBuffer value;

    /**
     * Used to store the name value pairs of the parameters.
     */
    private KeyMap<String> map;

    /**
     * The default constructor will create a <code>ContentParser</code> that
     * contains no charset, primary or secondary. This can be used to extract
     * the primary, secondary and the optional charset parameter by using the
     * parser's <code>parse(String)</code> method.
     */
    public ContentTypeParser() {
        this.secondary = new ParseBuffer();
        this.primary = new ParseBuffer();
        this.charset = new ParseBuffer();
        this.value = new ParseBuffer();
        this.type = new ParseBuffer();
        this.name = new ParseBuffer();
        this.map = new KeyMap<String>();
    }

    /**
     * This is primarily a convenience constructor. This will parse the
     * <code>String</code> given to extract the MIME type. This could be
     * achieved by calling the default no-arg constructor and then using the
     * instance to invoke the <code>parse</code> method on that
     * <code>String</code>.
     * 
     * @param header
     *            <code>String</code> containing a MIME type value
     */
    public ContentTypeParser(String header) {
        this();
        this.parse(header);
    }

    /**
     * This method is used to get the primary and secondary parts joined
     * together with a "/". This is typically how a content type is examined.
     * Here convenience is most important, we can easily compare content types
     * without any parameters.
     * 
     * @return this returns the primary and secondary types
     */
    @Override
    public String getType() {
        return this.type.toString();
    }

    /**
     * This sets the primary type to whatever value is in the string provided
     * is. If the string is null then this will contain a null string for the
     * primary type of the parameter, which is likely invalid in most cases.
     * 
     * @param value
     *            the type to set for the primary type of this
     */
    @Override
    public void setPrimary(String value) {
        this.type.reset(value);
        this.type.append('/');
        this.type.append(this.secondary);
        this.primary.reset(value);
    }

    /**
     * This is used to retrieve the primary type of this MIME type. The primary
     * type part within the MIME type defines the generic type. For example
     * <code>text/plain; charset=UTF-8</code>. This will return the text value.
     * If there is no primary type then this will return <code>null</code>
     * otherwise the string value.
     * 
     * @return the primary type part of this MIME type
     */
    @Override
    public String getPrimary() {
        return this.primary.toString();
    }

    /**
     * This sets the secondary type to whatever value is in the string provided
     * is. If the string is null then this will contain a null string for the
     * secondary type of the parameter, which is likely invalid in most cases.
     * 
     * @param value
     *            the type to set for the primary type of this
     */
    @Override
    public void setSecondary(String value) {
        this.type.reset(this.primary);
        this.type.append('/');
        this.type.append(value);
        this.secondary.reset(value);
    }

    /**
     * This is used to retrieve the secondary type of this MIME type. The
     * secondary type part within the MIME type defines the generic type. For
     * example <code>text/html; charset=UTF-8</code>. This will return the HTML
     * value. If there is no secondary type then this will return
     * <code>null</code> otherwise the string value.
     * 
     * @return the primary type part of this MIME type
     */
    @Override
    public String getSecondary() {
        return this.secondary.toString();
    }

    /**
     * This will set the <code>charset</code> to whatever value the string
     * contains. If the string is null then this will not set the parameter to
     * any value and the <code>toString</code> method will not contain any
     * details of the parameter.
     * 
     * @param enc
     *            parameter value to add to the MIME type
     */
    @Override
    public void setCharset(String enc) {
        this.charset.reset(enc);
    }

    /**
     * This is used to retrieve the <code>charset</code> of this MIME type. This
     * is a special parameter associated with the type, if the parameter is not
     * contained within the type then this will return null, which typically
     * means the default of ISO-8859-1.
     * 
     * @return the value that this parameter contains
     */
    @Override
    public String getCharset() {
        return this.charset.toString();
    }

    /**
     * This is used to retrieve an arbitrary parameter from the MIME type
     * header. This ensures that values for <code>boundary</code> or other such
     * parameters are not lost when the header is parsed. This will return the
     * value, unquoted if required, as a string.
     * 
     * @param name
     *            this is the name of the parameter to be retrieved
     * 
     * @return this is the value for the parameter, or null if empty
     */
    @Override
    public String getParameter(String name) {
        return this.map.get(name);
    }

    /**
     * This will add a named parameter to the content type header. If a
     * parameter of the specified name has already been added to the header then
     * that value will be replaced by the new value given. Parameters such as
     * the <code>boundary</code> as well as other common parameters can be set
     * with this method.
     * 
     * @param name
     *            this is the name of the parameter to be added
     * @param value
     *            this is the value to associate with the name
     */
    @Override
    public void setParameter(String name, String value) {
        this.map.put(name, value);
    }

    /**
     * This will initialize the parser when it is ready to parse a new
     * <code>String</code>. This will reset the parser to a ready state. The
     * init method is invoked by the parser when the <code>Parser.parse</code>
     * method is invoked.
     */
    @Override
    protected void init() {
        if (this.count > 0) {
            this.pack();
        }
        this.clear();
    }

    /**
     * This is used to clear all previously collected tokens. This allows the
     * parser to be reused when there are multiple source strings to be parsed.
     * Clearing of the tokens is performed when the parser is initialized.
     */
    private void clear() {
        this.primary.clear();
        this.secondary.clear();
        this.charset.clear();
        this.name.clear();
        this.value.clear();
        this.type.clear();
        this.map.clear();
        this.off = 0;
    }

    /**
     * Reads and parses the MIME type from the given <code>String</code> object.
     * This uses the syntax defined by RFC 2616 for the media-type syntax. This
     * parser is only concerned with one parameter, the <code>charset</code>
     * parameter. The syntax for the media type is
     * 
     * <pre>
     * media-type = token "/" token *( ";" parameter )
     * parameter = token | literal
     * </pre>
     */
    @Override
    protected void parse() {
        this.primary();
        this.off++;
        this.secondary();
        this.parameters();
    }

    /**
     * This is used to remove all whitespace characters from the
     * <code>String</code> excluding the whitespace within literals. The
     * definition of a literal can be found in RFC 2616.
     * <p>
     * The definition of a literal for RFC 2616 is anything between 2 quotes but
     * excluding quotes that are prefixed with the backward slash character.
     */
    private void pack() {
        char old = this.buf[0];
        int len = this.count;
        int seek = 0;
        int pos = 0;

        while (seek < len) {
            char ch = this.buf[seek++];

            if ((ch == '"') && (old != '\\')) { /* qd-text */
                this.buf[pos++] = ch;

                while (seek < len) {
                    old = this.buf[seek - 1];
                    ch = this.buf[seek++];
                    this.buf[pos++] = ch;

                    if ((ch == '"') && (old != '\\')) { /* qd-text */
                        break;
                    }
                }
            } else if (!this.space(ch)) {
                old = this.buf[seek - 1];
                this.buf[pos++] = old;
            }
        }
        this.count = pos;
    }

    /**
     * This reads the type from the MIME type. This will fill the type
     * <code>ParseBuffer</code>. This will read all chars upto but not including
     * the first instance of a '/'. The type of a media-type as defined by RFC
     * 2616 is <code>type/subtype;param=val;param2=val</code>.
     */
    private void primary() {
        while (this.off < this.count) {
            if (this.buf[this.off] == '/') {
                this.type.append('/');
                break;
            }
            this.type.append(this.buf[this.off]);
            this.primary.append(this.buf[this.off]);
            this.off++;
        }
    }

    /**
     * This reads the subtype from the MIME type. This will fill the subtype
     * <code>ParseBuffer</code>. This will read all chars upto but not including
     * the first instance of a ';'. The subtype of a media-type as defined by
     * RFC 2616 is <code>type/subtype;param=val;param2=val</code>.
     */
    private void secondary() {
        while (this.off < this.count) {
            if (this.buf[this.off] == ';') {
                break;
            }
            this.type.append(this.buf[this.off]);
            this.secondary.append(this.buf[this.off]);
            this.off++;
        }
    }

    /**
     * This will read the parameters from the MIME type. This will search for
     * the <code>charset</code> parameter within the set of parameters which are
     * given to the type. The <code>charset</code> param is the only parameter
     * that this parser will tokenize.
     * <p>
     * This will remove any parameters that preceed the charset parameter. Once
     * the <code>charset</code> is retrived the MIME type is considered to be
     * parsed.
     */
    private void parameters() {
        while (this.skip(";")) {
            if (this.skip("charset=")) {
                this.charset();
                break;
            } else {
                this.parameter();
                this.insert();
            }
        }
    }

    /**
     * This will add the name and value tokens to the parameters map. If any
     * previous value of the given name has been inserted into the map then this
     * will overwrite that value. This is used to ensure that the string value
     * is inserted to the map.
     */
    private void insert() {
        this.insert(this.name, this.value);
        this.name.clear();
        this.value.clear();
    }

    /**
     * This will add the given name and value to the parameters map. If any
     * previous value of the given name has been inserted into the map then this
     * will overwrite that value. This is used to ensure that the string value
     * is inserted to the map.
     * 
     * @param name
     *            this is the name of the value to be inserted
     * @param value
     *            this is the value of a that is to be inserted
     */
    private void insert(ParseBuffer name, ParseBuffer value) {
        this.map.put(name.toString(), value.toString());
    }

    /**
     * This is a parameter as defined by RFC 2616. The parameter is added to a
     * MIME type e.g. <code>type/subtype;param=val</code> etc. The parameter
     * name and value are not stored. This is used to simply update the read
     * offset past the parameter. The reason for reading the parameters is to
     * search for the <code>charset</code> parameter which will indicate the
     * encoding.
     */
    private void parameter() {
        this.name();
        this.off++; /* = */
        this.value();
    }

    /**
     * This will simply read all characters from the buffer before the first '='
     * character. This represents a parameter name (see RFC 2616 for token). The
     * parameter name is not buffered it is simply read from the buffer. This
     * will not cause an <code>IndexOutOfBoundsException</code> as each offset
     * is checked before it is acccessed.
     */
    private void name() {
        while (this.off < this.count) {
            if (this.buf[this.off] == '=') {
                break;
            }
            this.name.append(this.buf[this.off]);
            this.off++;
        }
    }

    /**
     * This is used to read a parameters value from the buf. This will read all
     * <code>char</code>'s upto but excluding the first terminal
     * <code>char</code> encountered from the off within the buf, or if the
     * value is a literal it will read a literal from the buffer (literal is any
     * data between quotes except if the quote is prefixed with a backward slash
     * character).
     */
    private void value() {
        if (this.quote(this.buf[this.off])) {
            for (this.off++; this.off < this.count;) {
                if (this.quote(this.buf[this.off])) {
                    if (this.buf[++this.off - 2] != '\\') {
                        break;
                    }
                }
                this.value.append(this.buf[this.off++]);
            }
        } else {
            while (this.off < this.count) {
                if (this.buf[this.off] == ';') {
                    break;
                }
                this.value.append(this.buf[this.off]);
                this.off++;
            }
        }
    }

    /**
     * This method is used to determine if the specified character is a quote
     * character. The quote character is typically used as a boundary for the
     * values within the header. This accepts a single or double quote.
     * 
     * @param ch
     *            the character to determine if it is a quotation
     * 
     * @return true if the character provided is a quotation character
     */
    private boolean quote(char ch) {
        return (ch == '\'') || (ch == '"');
    }

    /**
     * This is used to read the value from the <code>charset</code> param. This
     * will fill the <code>charset</code> <code>ParseBuffer</code> and with the
     * <code>charset</code> value. This will read a literal or a token as the
     * <code>charset</code> value. If the <code>charset</code> is a literal then
     * the quotes will be read as part of the charset.
     */
    private void charset() {
        if (this.buf[this.off] == '"') {
            this.charset.append('"');
            for (this.off++; this.off < this.count;) {
                this.charset.append(this.buf[this.off]);
                if (this.buf[this.off++] == '"')
                    if (this.buf[this.off - 2] != '\\') {
                        break;
                    }
            }
        } else {
            while (this.off < this.count) {
                if (this.buf[this.off] == ';') {
                    break;
                }
                this.charset.append(this.buf[this.off]);
                this.off++;
            }
        }
    }

    /**
     * This will return the value of the MIME type as a string. This will
     * concatenate the primary and secondary type values and add the
     * <code>charset</code> parameter to the type which will recreate the
     * content type.
     * 
     * @return this returns the string representation of the type
     */
    private String encode() {
        StringBuilder text = new StringBuilder();

        if (this.primary != null) {
            text.append(this.primary);
            text.append("/");
            text.append(this.secondary);
        }
        if (this.charset.length() > 0) {
            text.append("; charset=");
            text.append(this.charset);
        }
        return this.encode(text);
    }

    /**
     * This will return the value of the MIME type as a string. This will
     * concatenate the primary and secondary type values and add the
     * <code>charset</code> parameter to the type which will recreate the
     * content type.
     * 
     * @param text
     *            this is the buffer to encode the parameters to
     * 
     * @return this returns the string representation of the type
     */
    private String encode(StringBuilder text) {
        for (String name : this.map) {
            String value = this.map.get(name);

            text.append("; ");
            text.append(name);

            if (value != null) {
                text.append("=");
                text.append(value);
                ;
            }
        }
        return text.toString();
    }

    /**
     * This will return the value of the MIME type as a string. This will
     * concatenate the primary and secondary type values and add the
     * <code>charset</code> parameter to the type which will recreate the
     * content type.
     * 
     * @return this returns the string representation of the type
     */
    @Override
    public String toString() {
        return this.encode();
    }
}
