package com.provismet.vmcmc.vmc;

import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class CaptureRegistry {
    private static final HashMap<Identifier, Pair<CaptureType, Function<MinecraftClient, Float>>> REGISTRY = new HashMap<>();

    public static void register (Identifier path, CaptureType type, Function<MinecraftClient, Float> callback) {
        CaptureRegistry.REGISTRY.put(path, new Pair<>(type, callback));
    }

    public static void iterate (MinecraftClient client) {
        if (client.player == null && PacketSender.isValid()) return;

        REGISTRY.forEach((id, callbackInfo) -> {
            float output = callbackInfo.getRight().apply(client);

            switch (callbackInfo.getLeft()) {
                case BLEND_SHAPE:
                    PacketSender.sendBlendShape(id.toString(), output);
                    break;
            
                case BONE:
                    PacketSender.sendBone(id.toString(), output);
                    break;

                default:
                    break;
            }
        });

        PacketSender.sendBlendApply();
    }

    public enum CaptureType {
        BLEND_SHAPE,
        BONE
    }
}
