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
import net.minecraft.util.math.MathHelper;

/**
 * A basic registry that stores the callbacks to generate BlendShapes and Bones.
 */
public class CaptureRegistry {
    private static final HashMap<Identifier, Function<MinecraftClient, Float>> BLEND_REGISTRY = new HashMap<>();
    private static final HashMap<Identifier, Function<MinecraftClient, List<Float>>> BONE_REGISTRY = new HashMap<>();

    /**
     * Registers a callback that generates a BlendShape. Minecraft identifiers are used to softly-enforce unique names.
     * 
     * It is recommend, but not required, that callbacks have their output bound between 0 and 1.
     * 
     * @param identifier The ID for the BlendShape. Note: This will be converted into a string when being sent over OSC.
     * @param callback A function that uses the client to output a float.
     */
    public static void registerBlendShape (Identifier identifier, Function<MinecraftClient, Float> callback) {
        BLEND_REGISTRY.put(identifier, callback);
    }

    /**
     * Registers a callback (see {@link CaptureRegistry#registerBlendShape(Identifier, Function)}) with a string instead of an Identifier.
     * @param path The name of the callback. This will be converted into an ID under the vmc-mc default namespace.
     * @param callback A function that uses the client to output a float.
     */
    public static void registerBlendShape (String path, Function<MinecraftClient, Float> callback) {
        registerBlendShape(ClientVMC.identifier(path), callback);
    }

    /**
     * NOTE: BONES ARE CURRENTLY NON-FUNCTIONAL. USE BLENDSHAPES INSTEAD.
     * 
     * Registers a callback that generates a Bone.
     * Bones are defined as 7 floats (a 3D coordinate and a quaternion).
     * {@code [x coordinate, y coordinate, z coordinate, quaternion-x, quaternion-y, quaternion-z, quaternion-w]}
     * 
     * @param identifier The ID for the Bone. Note: This will be converted into a string when being sent over OSC.
     * @param callback A function that uses the client to output a list of floats.
     */
    public static void registerBone (Identifier identifier, Function<MinecraftClient, List<Float>> callback) {
        BONE_REGISTRY.put(identifier, callback);
    }

    /**
     * NOTE: BONES ARE CURRENTLY NON-FUNCTIONAL. USE BLENDSHAPES INSTEAD.
     * 
     * Registers a callback see {@link CaptureRegistry#registerBone(Identifier, Function)} with a string instead of an Identifier.
     * Bones are defined as 7 floats (a 3D coordinate and a quaternion).
     * {@code [x coordinate, y coordinate, z coordinate, quaternion-x, quaternion-y, quaternion-z, quaternion-w]}
     * 
     * @param path The name of the callback. This will be converted into an ID under the vmc-mc default namespace.
     * @param callback A function that uses the client to output a list of floats.
     */
    public static void registerBone (String path, Function<MinecraftClient, List<Float>> callback) {
        registerBone(ClientVMC.identifier(path), callback);
    }

    /**
     * Registers the standard vmc-mc callbacks. This is called during mod initialisation.
     */
    public static void registerStandardEvents () {
        // Your block light (light gained from torches, etc).
        registerBlendShape("block_light", client -> {
			int lightmap = client.getEntityRenderDispatcher().getLight(client.player, client.getTickDelta());
            float blockLight = (lightmap >> 4) & 0xF;
			return blockLight / 15f;
		});

        // This refers to the strength of your sky exposure. It's the same value as from the F3 menu and is NOT the same as actual brightness (but DOES impact it).
        registerBlendShape("sky_light", client -> {
			int lightmap = client.getEntityRenderDispatcher().getLight(client.player, client.getTickDelta());
            float skyLight = (lightmap >> 20) & 0xF;
			return skyLight / 15f;
		});

        // The actual light level of the area. Use this if you want brightness that includes blocks and the sky.
        registerBlendShape("internal_light", client -> {
            // This section is just a copy of World.getAmbientDarkness() because it does not tick the calculations on the render thread (only outputs 0).
            double rainGradient = 1.0 - (double)(client.world.getRainGradient(1.0f) * 5.0f) / 16.0;
            double thunderGradient = 1.0 - (double)(client.world.getThunderGradient(1.0f) * 5.0f) / 16.0;
            double skyAngleMath = 0.5 + 2.0 * MathHelper.clamp((double)MathHelper.cos(client.world.getSkyAngle(1.0f) * ((float)Math.PI * 2)), -0.25, 0.25);
            int ambientDarkness = (int)((1.0 - rainGradient * thunderGradient * skyAngleMath) * 11.0);

            float internalLight = client.world.getLightLevel(client.player.getBlockPos(), ambientDarkness);
            return internalLight / 15f;
        });

        registerBlendShape("relative_health", client -> {
            return client.player.getHealth() / client.player.getMaxHealth();
        });

        registerBlendShape("water_height", client -> {
            return (float)client.player.getFluidHeight(FluidTags.WATER);
        });

        registerBlendShape("lava_height", client -> {
            return (float)client.player.getFluidHeight(FluidTags.LAVA);
        });

        registerBlendShape("time_of_day", client -> {
            float time = client.player.getWorld().getTimeOfDay();
            while (time >= 24000f) {
                time -= 24000f;
            }
            return time / 24000f;
        });

        registerBlendShape("rain_amount", client -> {
            if (client.player.getWorld().isThundering()) return 1f;
            else if (client.player.getWorld().isRaining()) return 0.5f;
            else return 0f;
        });

        registerBlendShape("on_fire", client -> {
            return client.player.isOnFire() ? 1f : 0f;
        });

        registerBlendShape("experience", client -> {
            return (float)client.player.experienceLevel + client.player.experienceProgress;
        });

        registerBlendShape("is_wet", client -> {
            return client.player.isWet() ? 1f : 0f;
        });

        registerBlendShape("sneaking", client -> {
            return client.player.isSneaking() ? 1f : 0f;
        });

        registerBlendShape("sleeping", client -> {
            return client.player.isSleeping() ? 1f : 0f;
        });

        registerBlendShape("alive", client -> {
            return client.player.isAlive() ? 1f : 0f;
        });

        registerBlendShape("relative_food_level", client -> {
            return (float)client.player.getHungerManager().getFoodLevel() / 20f;
        });

        /* HOW DO BONES?
        registerBone("head", client -> {
            float yaw = client.player.getHeadYaw();
            float pitch = client.player.getPitch();
            float roll = client.player.getRoll();

            float qx = MathHelper.sin(roll / 2) * MathHelper.cos(pitch / 2) * MathHelper.cos(yaw / 2) - MathHelper.cos(roll / 2) * MathHelper.sin(pitch / 2) * MathHelper.sin(yaw / 2);
            float qy = MathHelper.cos(roll / 2) * MathHelper.sin(pitch / 2) * MathHelper.cos(yaw / 2) + MathHelper.sin(roll / 2) * MathHelper.cos(pitch / 2) * MathHelper.sin(yaw / 2);
            float qz = MathHelper.cos(roll / 2) * MathHelper.cos(pitch / 2) * MathHelper.sin(yaw / 2) - MathHelper.sin(roll / 2) * MathHelper.sin(pitch / 2) * MathHelper.cos(yaw / 2);
            float qw = MathHelper.cos(roll / 2) * MathHelper.cos(pitch / 2) * MathHelper.cos(yaw / 2) + MathHelper.sin(roll / 2) * MathHelper.sin(pitch / 2) * MathHelper.sin(yaw / 2);

            return Arrays.asList(0f, 0f, 0f, qx, qy, qz, qw);
        });

        registerBone("velocity", client -> {
            return Arrays.asList((float)client.player.getVelocity().getX(), (float)client.player.getVelocity().getY(), (float)client.player.getVelocity().getZ(), 0f, 0f, 0f, 0f);
        });
        */
    }

    /**
     * Executes all registered callbacks and sends them through the {@link PacketSender} port.
     * @param client The Minecraft client.
     */
    public static void iterate (MinecraftClient client) {
        if (client.player == null || !PacketSender.isValid()) return;

        List<OSCPacket> messages = new ArrayList<>(BLEND_REGISTRY.size() + BONE_REGISTRY.size());

        BLEND_REGISTRY.forEach((id, callback) -> {
            messages.add(PacketSender.createBlendShape(id.toString(), callback.apply(client)));
        });
        messages.add(PacketSender.createBlendApply());
        PacketSender.sendBundle(messages);

        BONE_REGISTRY.forEach((id, callback) -> {
            PacketSender.sendBone(id.toString(), callback.apply(client));
        });
    }

    public enum CaptureType {
        BLEND_SHAPE,
        BONE
    }
}
