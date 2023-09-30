package com.provismet.vmcmc.vmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.illposed.osc.OSCPacket;
import com.provismet.vmcmc.ClientVMC;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class CaptureRegistry {
    private static final HashMap<Identifier, Pair<CaptureType, Function<MinecraftClient, Float>>> REGISTRY = new HashMap<>();

    public static void register (Identifier identifier, CaptureType type, Function<MinecraftClient, Float> callback) {
        CaptureRegistry.REGISTRY.put(identifier, new Pair<>(type, callback));
    }

    public static void register (String path, CaptureType type, Function<MinecraftClient, Float> callback) {
        CaptureRegistry.register(ClientVMC.identifier(path), type, callback);
    }

    public static void registerStandardEvents () {
        CaptureRegistry.register("light", CaptureType.BLEND_SHAPE, client -> {
			float light = client.getEntityRenderDispatcher().getLight(client.player, client.getTickDelta());
			return 15f / light;
		});

        CaptureRegistry.register("relative_health", CaptureType.BLEND_SHAPE, client -> {
            return client.player.getMaxHealth() / client.player.getHealth();
        });

        CaptureRegistry.register("hurt", CaptureType.BLEND_SHAPE, client -> {
            final float DURATION = 5f;
            float elapsed = client.player.getLastAttackedTime() - client.player.age;
            if (elapsed > DURATION) return 0f;
            else return elapsed / DURATION;
        });

        CaptureRegistry.register("in_water", CaptureType.BLEND_SHAPE, client -> {
            return (float)client.player.getFluidHeight(FluidTags.WATER);
        });

        CaptureRegistry.register("in_lava", CaptureType.BLEND_SHAPE, client -> {
            return (float)client.player.getFluidHeight(FluidTags.LAVA);
        });
    }

    public static void iterate (MinecraftClient client) {
        if (client.player == null || !PacketSender.isValid()) return;

        List<OSCPacket> messages = new ArrayList<>(REGISTRY.size());

        REGISTRY.forEach((id, callbackInfo) -> {
            float output = callbackInfo.getRight().apply(client);

            switch (callbackInfo.getLeft()) {
                case BLEND_SHAPE:
                    messages.add(PacketSender.createBlendShape(id.toString(), output));
                    break;
            
                case BONE:
                    break;

                default:
                    break;
            }
        });

        messages.add(PacketSender.createBlendApply());
        PacketSender.sendBundle(messages);
    }

    public enum CaptureType {
        BLEND_SHAPE,
        BONE
    }
}
