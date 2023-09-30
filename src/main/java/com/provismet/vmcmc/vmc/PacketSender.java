package com.provismet.vmcmc.vmc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.transport.OSCPortOut;
import com.provismet.vmcmc.ClientVMC;

public class PacketSender {
    private static OSCPortOut portOut;

    public static void initPort (int port) {
        try {
            portOut = new OSCPortOut(InetAddress.getLocalHost(), port);
            ClientVMC.LOGGER.info("Created VMC socket at port: " + port);
        }
        catch (IOException e) {
            ClientVMC.LOGGER.error("Failed to create port:", e);
        }
    }

    public static boolean isValid () {
        return portOut != null;
    }

    public static void sendBlendShape (String name, float value) {
        List<Object> args = Arrays.asList(name, value);
        OSCMessage message = new OSCMessage("/VMC/Ext/Blend/Val", args);

        try {
            portOut.send(message);
        }
        catch (Exception e) {
            ClientVMC.LOGGER.error("Failed to send message: ", e);
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
