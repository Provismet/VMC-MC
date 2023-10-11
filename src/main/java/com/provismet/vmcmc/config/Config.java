package com.provismet.vmcmc.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.stream.JsonReader;
import com.provismet.vmcmc.ClientVMC;
import com.provismet.vmcmc.vmc.PacketSender;

import net.minecraft.util.Pair;

public class Config {
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String FILEPATH = "config/vmc-mc.json";

    private static String host = PacketSender.LOCALHOST;
    private static int port = PacketSender.DEFAULT_PORT;

    public static Pair<String,Integer> readJSON () {
        try {
            FileReader reader = new FileReader(FILEPATH);
            JsonReader parser = new JsonReader(reader);

            parser.beginObject();
            while (parser.hasNext()) {
                final String name = parser.nextName();
                switch (name) {
                    case HOST:
                        host = parser.nextString();
                        break;
                
                    case PORT:
                        port = parser.nextInt();
                        break;

                    default:
                        break;
                }
            }
            parser.close();
            return new Pair<>(host, port);
        }
        catch (FileNotFoundException e) {
            ClientVMC.LOGGER.warn("Config not found, creating default config.");
            saveJSON();
            return new Pair<String,Integer>(PacketSender.LOCALHOST, PacketSender.DEFAULT_PORT);
        }
        catch (Exception e) {
            ClientVMC.LOGGER.warn("Config could not be read, using default parameters.", e);
            return new Pair<String,Integer>(PacketSender.LOCALHOST, PacketSender.DEFAULT_PORT);
        }
    }

    public static void saveJSON () {
        try {
            FileWriter writer = new FileWriter(FILEPATH);
            String simpleJSON = String.format("{\n\t\"%s\": \"%s\",\n\t\"%s\": %d\n}",
                HOST, host,
                PORT, port
            );
            writer.write(simpleJSON);
            writer.close();
        }
        catch (Exception e) {
            
        }
    }

    public static String getIP () {
        return host;
    }

    public static int getPort () {
        return port;
    }

    public static void setIP (String newIP) {
        host = newIP;
    }

    public static void setPort (int newPort) {
        if (newPort <= 65535 && newPort > 0) {
            port = newPort;
        }
        else {
            ClientVMC.LOGGER.error("Attempted to set illegal port: " + newPort);
        }
    }
}
