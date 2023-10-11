package com.provismet.vmcmc.vmc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.transport.OSCPortOut;
import com.provismet.vmcmc.ClientVMC;

/**
 * A singleton class that provides utility in creating and sending messages using VMC.
 * 
 * This singleton is guaranteed to exist after the vmc-mc mod has been initialised on the client.
 */
public class PacketSender {
    public static final String LOCALHOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 39540;

    private static OSCPortOut portOut;

    /**
     * Initialises an OSC port at the specified location.
     * @param host The host name or IP. (Use 127.0.0.1 to connect to the same PC.)
     * @param port The port number.
     */
    public static void initPort (String host, int port) {
        if (portOut != null) {
            try {
                portOut.close();
            }
            catch (IOException e) {
                ClientVMC.LOGGER.error("Failed to close port:", e);
            }
        }

        try {
            portOut = new OSCPortOut(InetAddress.getByName(host), port); // Use 127.0.0.1 and NOT InetAddress.getLocalHost(), the latter does not work.
            ClientVMC.LOGGER.info("Created VMC socket at " + host + ":" + port);
        }
        catch (IOException e) {
            ClientVMC.LOGGER.error("Failed to create port:", e);
        }
    }

    /**
     * 
     * @return Whether or not the PacketSender has a valid port.
     */
    public static boolean isValid () {
        return portOut != null;
    }

    /**
     * 
     * @param name The name of the BlendShape.
     * @param value The floating point value of the BlendShape.
     * @return An OSC Message pointing to {@code /VMC/Ext/Blend/Val} with the contents of [name, value].
     */
    public static OSCMessage createBlendShape (String name, float value) {
        return new OSCMessage("/VMC/Ext/Blend/Val", Arrays.asList(name, value));
    }

    /**
     * Creates and then immediately sends a BlendShape of the form [name, value].
     * Relies on {@link PacketSender#createBlendShape(String, float)}.
     * @param name The name of the BlendShape.
     * @param value The floating point value of the BlendShape.
     */
    public static void sendBlendShape (String name, float value) {
        try {
            portOut.send(createBlendShape(name, value));
        }
        catch (Exception e) {
            ClientVMC.LOGGER.error("Failed to send message: ", e);
        }
    }

    /**
     * DO NOT USE. This isn't working right now, not sure why.
     * @param name
     * @param XYZ_Quaternion
     * @return An OSC Message pointing to {@code /VMC/Ext/Bone/Pos} with the contents of [name, x, y, z, qx, qy, qz, qw].
     */
    public static OSCMessage createBone (String name, List<Float> XYZ_Quaternion) {
        List<Object> args = new ArrayList<>(XYZ_Quaternion);
        args.add(0, name);
        return new OSCMessage("/VMC/Ext/Bone/Pos", args);
    }

    public static void sendBone (String name, List<Float> XYZ_Quaternion) {
        try {
            portOut.send(createBone(name, XYZ_Quaternion));
        }
        catch (Exception e) {

        }
    }

    /**
     * 
     * @return An OSC Message that sends to {@code /VMC/Ext/Blend/Apply}.
     */
    public static OSCMessage createBlendApply () {
        return new OSCMessage("/VMC/Ext/Blend/Apply");
    }

    /**
     * Create and immediately sends a message to {@code /VMC/Ext/Blend/Apply}.
     * Relies on {@link PacketSender#createBlendApply()}
     */
    public static void sendBlendApply () {
        try {
            portOut.send(createBlendApply());
        }
        catch (Exception e) {

        }
    }

    /**
     * Multi-parameter version of {@link PacketSender#sendBundle(List)}.
     * This version takes as many {@link OSCPacket} parameters as needed.
     * @param packets A number of packets.
     */
    public static void sendBundle (OSCPacket... packets) {
        sendBundle(Arrays.asList(packets));
    }

    /**
     * Sends a list of packets (messages or other possible content) through the port.
     * @param packets The list of packets to send.
     */
    public static void sendBundle (List<OSCPacket> packets) {
        OSCBundle bundle = new OSCBundle(packets);

        try {
            portOut.send(bundle);
        }
        catch (Exception e) {
            ClientVMC.LOGGER.error("Failed to send OSC bundle:", e);
        }
    }
}
