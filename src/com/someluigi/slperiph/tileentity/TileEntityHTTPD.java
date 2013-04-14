package com.someluigi.slperiph.tileentity;

import java.io.PrintStream;
import java.util.LinkedList;

import net.deskcc.computercraft.DeviceProperties;

import com.someluigi.slperiph.ccdesk.SLPPlug;
import com.someluigi.slperiph.server.SLPHTTPServer;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityHTTPD implements IPeripheral {

    public static String[] methods = new String[] { "isActive", "respond",
            "start", "stop" };

    public LinkedList<Object> reqsw = new LinkedList<Object>();

    public String getType() {
        return "http-server";
    }

    public String[] getMethodNames() {
        return TileEntityHTTPD.methods;
    }

    public Object[] callMethod(IComputerAccess computer, int method,
            Object[] args) throws Exception {

        String mn = methods[method];

        if (mn.equals("isActive")) return new Object[] { SLPPlug.httpdEnabled };
        if (mn.equals("respond")) {
            
            Object pso = this.reqsw.get((int) ((Double) args[0]).doubleValue());
            
            if (pso instanceof PrintStream) {
                PrintStream ps = (PrintStream) pso;
                
                ps.print(args[1]);
                ps.close();
            }
        }
        if (mn.equals("start")) {
            SLPHTTPServer.services.put(computer.getID(), new Object[] {
                    computer, this });
        }
        if (mn.equals("stop")) {
            SLPHTTPServer.services.remove(computer.getID());
        }

        return null;
    }

    public boolean canAttachToSide(int side) {
        return true;
    }

    public void attach(IComputerAccess computer) {
    }

    public void detach(IComputerAccess computer) {
    }

}
