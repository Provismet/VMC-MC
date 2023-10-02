package com.provismet.vmcmc.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.stream.JsonReader;
import com.provismet.vmcmc.vmc.PacketSender;

import net.minecraft.util.Pair;

public class Config {
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String FILEPATH = "config/vmc-mc.json";

    public static Pair<String,Integer> getPortInfo () {
        try {
            FileReader reader = new FileReader(FILEPATH);
            JsonReader parser = new JsonReader(reader);

            String host = PacketSender.LOCALHOST;
            int port = PacketSender.DEFAULT_PORT;

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
            try {
                FileWriter writer = new FileWriter(FILEPATH);
                String simpleJSON = String.format("{\n\t\"%s\": \"%s\",\n\t\"%s\": %d\n}",
                    HOST, PacketSender.LOCALHOST,
                    PORT, PacketSender.DEFAULT_PORT
                );
                writer.write(simpleJSON);
                writer.close();
            }
            catch (Exception e2) {
                
            }
            return new Pair<String,Integer>(PacketSender.LOCALHOST, PacketSender.DEFAULT_PORT);
        }
        catch (Exception e) {
            return new Pair<String,Integer>(PacketSender.LOCALHOST, PacketSender.DEFAULT_PORT);
        }
    }
}
