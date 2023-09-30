package com.provismet.vmcmc.vmc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.transport.OSCPortOut;
import com.provismet.vmcmc.ClientVMC;

public class PacketSender {
    private static OSCPortOut portOut;

    public static void initPort (int port) {
        try {
            portOut = new OSCPortOut(new InetSocketAddress(InetAddress.getLocalHost(), port));
        }
        catch (IOException e) {
            ClientVMC.LOGGER.error("Failed to create port:", e);
        }
    }

    public static void initPort () {
        try {
            portOut = new OSCPortOut();
        }
        catch (IOException e) {
            ClientVMC.LOGGER.error("Failed to create port:", e);
        }
    }

    public static boolean isValid () {
        return portOut != null;
    }

    public static void sendBlendShape (String name, float value) {
        List<Object> args = new ArrayList<>(2);
        args.add(value);
        args.add(name);
        OSCMessage message = new OSCMessage("/VMC/Ext/Blend/Val", args);

        try {
            portOut.send(message);
        }
        catch (Exception e) {

        }
    }

    public static void sendBone (String name, float value) {

    }

    public static void sendBlendApply () {
        try {
            portOut.send(new OSCMessage("/VMC/Ext/Blend/Apply"));
        }
        catch (Exception e) {

        }
    }
}
