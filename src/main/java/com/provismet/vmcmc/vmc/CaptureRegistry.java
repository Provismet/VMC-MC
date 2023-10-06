package com.provismet.vmcmc.vmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.illposed.osc.OSCPacket;
import com.provismet.vmcmc.ClientVMC;
import com.provismet.vmcmc.utility.HealthTracker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;

/**
 * A basic registry that stores the callbacks to generate BlendShapes and Bones.
 */
@Environment(value=EnvType.CLIENT)
public class CaptureRegistry {
    private static final HashMap<String, Function<MinecraftClient, Float>> BLEND_REGISTRY = new HashMap<>();
    private static final HashMap<String, Function<MinecraftClient, List<Float>>> BONE_REGISTRY = new HashMap<>();
    private static final HashMap<String, BlendStore> BLENDSTORE_REGISTRY = new HashMap<>();

    public static BlendStore getBlendStore (Identifier identifier) {
        return BLENDSTORE_REGISTRY.get(identifier.toString());
    }

    public static boolean containsKey (String key) {
        return BLEND_REGISTRY.containsKey(key) || BLENDSTORE_REGISTRY.containsKey(key) || BONE_REGISTRY.containsKey(key);
    }

    public static boolean containsKey (Identifier key) {
        return containsKey(key.toString());
    }

    /**
     * Registers a callback that generates a BlendShape. Minecraft identifiers are used to softly-enforce unique names.
     * 
     * It is recommend, but not required, that callbacks have their output bound between 0 and 1.
     * 
     * @param identifier The ID for the BlendShape. Note: This will be converted into a string when being sent over OSC.
     * @param callback A function that uses the client to output a float.
     */
    public static void registerBlendShape (Identifier identifier, Function<MinecraftClient, Float> callback) {
        if (containsKey(identifier)) ClientVMC.LOGGER.error("Duplicate BlendShape register attempt: " + identifier.toString());
        else BLEND_REGISTRY.put(identifier.toString(), callback);
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
     * Registers a {@link BlendStore} for output.
     * @param identifier The identifier of the BlendStore.
     * @param blendStore The BlendStore to receive input from.
     */
    public static void registerBlendStore (Identifier identifier, BlendStore blendStore) {
        if (containsKey(identifier)) ClientVMC.LOGGER.error("Duplicate BlendStore register attempt: " + identifier.toString());
        else BLENDSTORE_REGISTRY.put(identifier.toString(), blendStore);
    }

    public static void registerBlendStore (String path, BlendStore blendStore) {
        registerBlendStore(ClientVMC.identifier(path), blendStore);
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
        if (containsKey(identifier)) ClientVMC.LOGGER.error("Duplicate Bone register attempt: " + identifier.toString());
        else BONE_REGISTRY.put(identifier.toString(), callback);
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

            // Manually calculate light this way because it accounts for mods that modify the player's block-light value.
            float internalLight;
            int lightmap = client.getEntityRenderDispatcher().getLight(client.player, client.getTickDelta());
            float blockLight = (lightmap >> 4) & 0xF;
            float skyLight = ((lightmap >> 20) & 0xF) - ambientDarkness;
            internalLight = blockLight > skyLight ? blockLight : skyLight;
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

        registerBlendShape("water_submerged", client -> {
            return client.player.isSubmergedInWater() ? 1f : 0f;
        });

        registerBlendShape("lava_submerged", client -> {
            return client.player.isSubmergedIn(FluidTags.LAVA) ? 1f : 0f;
        });

        registerBlendShape("time_of_day", client -> {
            float time = client.world.getTimeOfDay();
            while (time >= 24000f) {
                time -= 24000f;
            }
            return time / 24000f;
        });

        registerBlendShape("rain_amount", client -> {
            if (client.world.isThundering()) return 1f;
            else if (client.world.isRaining()) return 0.5f;
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

        registerBlendShape("crawling", client -> {
            return client.player.isCrawling() ? 1f : 0f;
        });

        registerBlendShape("climbing", client -> {
            return client.player.isClimbing() ? 1f : 0f;
        });

        registerBlendShape("blocking", client -> {
            return client.player.isBlocking() ? 1f : 0f;
        });

        registerBlendShape("glowing", client -> {
            return client.player.isGlowing() ? 1f : 0f;
        });

        registerBlendShape("frozen", client -> {
            return client.player.isFrozen() ? 1f : 0f;
        });

        registerBlendShape("swimming", client -> {
            return client.player.isSwimming() ? 1f : 0f;
        });

        registerBlendShape("sprinting", client -> {
            return client.player.isSprinting() ? 1f : 0f;
        });

        registerBlendShape("riding_living", client -> {
            return client.player.getVehicle() instanceof LivingEntity ? 1f : 0f;
        });

        registerBlendShape("riding_nonliving", client -> {
            if (client.player.getVehicle() == null) return 0f;
            return client.player.getVehicle() instanceof LivingEntity ? 0f : 1f;
        });

        registerBlendShape("elytra_flying", client -> {
            return client.player.isFallFlying() ? 1f : 0f;
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

        registerBlendShape("relative_air_level", client -> {
            float airLevel = client.player.getAir() > 0 ? client.player.getAir() : 0f;
            return airLevel / (float)client.player.getMaxAir();
        });

        registerBlendShape("hotbar", client -> {
            float slot = client.player.getInventory().selectedSlot;
            return (slot + 0.1f) / 10f;
        });

        registerBlendShape("exposed_to_sky", client -> {
            return client.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, client.player.getBlockPos()).getY() > client.player.getEyeY() ? 0f : 1f;
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

        registerBlendStore("attack_player", new BlendStore(0f, 1f, 0.05f, 50));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator() == false && entity instanceof PlayerEntity) {
                getBlendStore(ClientVMC.identifier("attack_player")).activate();
            }
			return ActionResult.PASS;
		});

        registerBlendStore("attack_hostile", new BlendStore(0f, 1f, 0.05f, 50));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator() == false && entity instanceof HostileEntity) {
                getBlendStore(ClientVMC.identifier("attack_hostile")).activate();
            }
			return ActionResult.PASS;
		});

        registerBlendStore("attack_living", new BlendStore(0f, 1f, 0.05f, 50));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator() == false && entity instanceof LivingEntity) {
                getBlendStore(ClientVMC.identifier("attack_living")).activate();
            }
			return ActionResult.PASS;
		});

        registerBlendStore("damage_taken", new BlendStore(0f, 1f, 0.05f, 50));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) HealthTracker.update(client.player.getHealth());
        });
    }

    /**
     * Executes all registered callbacks and sends them through the {@link PacketSender} port.
     * @param client The Minecraft client.
     */
    public static void iterate (MinecraftClient client) {
        if (client.player == null || client.world == null || !PacketSender.isValid()) return;

        List<OSCPacket> messages = new ArrayList<>(BLEND_REGISTRY.size() + BONE_REGISTRY.size());

        BLEND_REGISTRY.forEach((id, callback) -> {
            messages.add(PacketSender.createBlendShape(id, callback.apply(client)));
        });
        BLENDSTORE_REGISTRY.forEach((id, blendstore) -> {
            messages.add(PacketSender.createBlendShape(id, blendstore.get()));
        });

        messages.add(PacketSender.createBlendApply());
        PacketSender.sendBundle(messages);

        BONE_REGISTRY.forEach((id, callback) -> {
            PacketSender.sendBone(id, callback.apply(client));
        });
    }
}
