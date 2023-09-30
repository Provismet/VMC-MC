package com.provismet.vmcmc.vmc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
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

    public static OSCMessage createBlendShape (String name, float value) {
        return new OSCMessage("/VMC/Ext/Blend/Val", Arrays.asList(name, value));
    }

    public static void sendBlendShape (String name, float value) {
        try {
            portOut.send(createBlendShape(name, value));
        }
        catch (Exception e) {
            ClientVMC.LOGGER.error("Failed to send message: ", e);
        }
    }

    public static OSCMessage createBone (String name, List<Float> XYZ, List<Float> Quaternion) {
        throw new NotImplementedException(); // TODO: Add bone sending.
    }

    public static void sendBone (String name, List<Float> XYZ, List<Float> Quaternion) {
        try {
            portOut.send(createBone(name, XYZ, Quaternion));
        }
        catch (Exception e) {

        }
    }

    public static OSCMessage createBlendApply () {
        return new OSCMessage("/VMC/Ext/Blend/Apply");
    }

    public static void sendBlendApply () {
        try {
            portOut.send(createBlendApply());
        }
        catch (Exception e) {

        }
    }

    public static void sendBundle (OSCPacket... packets) {
        sendBundle(Arrays.asList(packets));
    }

    public static void sendBundle (List<OSCPacket> packets) {
        OSCBundle bundle = new OSCBundle(packets);

        try {
            portOut.send(bundle);
            ClientVMC.LOGGER.info("Sent bundle.");
        }
        catch (Exception e) {
            ClientVMC.LOGGER.error("Failed to send OSC bundle:", e);
        }
    }
}
