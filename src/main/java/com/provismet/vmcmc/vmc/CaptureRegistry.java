package com.provismet.vmcmc.vmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.illposed.osc.OSCPacket;
import com.provismet.vmcmc.ClientVMC;
import com.provismet.vmcmc.config.Config;
import com.provismet.vmcmc.utility.HealthTracker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * A basic registry that stores the callbacks to generate BlendShapes and Bones.
 */
@Environment(value=EnvType.CLIENT)
public class CaptureRegistry {
    private static final HashMap<String, Function<Minecraft, Float>> BLEND_REGISTRY = new HashMap<>();
    private static final HashMap<String, Function<Minecraft, List<Float>>> BONE_REGISTRY = new HashMap<>();
    private static final HashMap<String, BlendStore> BLENDSTORE_REGISTRY = new HashMap<>();

    public static BlendStore getBlendStore (Identifier identifier) {
        return BLENDSTORE_REGISTRY.get(Config.getBlendName(identifier));
    }

    public static boolean containsKey (String key) {
        return BLEND_REGISTRY.containsKey(key) || BLENDSTORE_REGISTRY.containsKey(key) || BONE_REGISTRY.containsKey(key);
    }

    public static boolean containsKey (Identifier key) {
        return containsKey(Config.getBlendName(key));
    }

    /**
     * Registers a callback that generates a BlendShape. Minecraft identifiers are used to softly-enforce unique names.
     * <p>
     * It is recommended, but not required, that callbacks have their output bound between 0 and 1.
     * 
     * @param identifier The ID for the BlendShape. Note: This will be converted into a string when being sent over OSC.
     * @param callback A function that uses the client to output a float.
     */
    public static void registerBlendShape (Identifier identifier, Function<Minecraft, Float> callback) {
        if (containsKey(identifier)) ClientVMC.LOGGER.error("Duplicate BlendShape register attempt: {}", Config.getBlendName(identifier));
        else BLEND_REGISTRY.put(Config.getBlendName(identifier), callback);
    }

    /**
     * Registers a callback (see {@link CaptureRegistry#registerBlendShape(Identifier, Function)}) with a string instead of an Identifier.
     * @param path The name of the callback. This will be converted into an ID under the vmc-mc default namespace.
     * @param callback A function that uses the client to output a float.
     */
    public static void registerBlendShape (String path, Function<Minecraft, Float> callback) {
        registerBlendShape(ClientVMC.identifier(path), callback);
    }

    /**
     * Registers a {@link BlendStore} for output.
     * @param identifier The identifier of the BlendStore.
     * @param blendStore The BlendStore to receive input from.
     */
    public static void registerBlendStore (Identifier identifier, BlendStore blendStore) {
        if (containsKey(identifier)) ClientVMC.LOGGER.error("Duplicate BlendStore register attempt: {}", Config.getBlendName(identifier));
        else BLENDSTORE_REGISTRY.put(Config.getBlendName(identifier), blendStore);
    }

    public static void registerBlendStore (String path, BlendStore blendStore) {
        registerBlendStore(ClientVMC.identifier(path), blendStore);
    }

    /**
     * Registers a callback that generates a Bone.
     * Bones are defined as 7 floats (a 3D coordinate and a quaternion).
     * {@code [x coordinate, y coordinate, z coordinate, quaternion-x, quaternion-y, quaternion-z, quaternion-w]}
     *
     * @implNote BONES ARE CURRENTLY NON-FUNCTIONAL. USE BLENDSHAPES INSTEAD.
     *
     * @param identifier The ID for the Bone. Note: This will be converted into a string when being sent over OSC.
     * @param callback A function that uses the client to output a list of floats.
     */
    public static void registerBone (Identifier identifier, Function<Minecraft, List<Float>> callback) {
        if (containsKey(identifier)) ClientVMC.LOGGER.error("Duplicate Bone register attempt: {}", identifier.toString());
        else BONE_REGISTRY.put(identifier.toString(), callback);
    }

    /**
     * NOTE: BONES ARE CURRENTLY NON-FUNCTIONAL. USE BLENDSHAPES INSTEAD.
     * <p>
     * Registers a callback see {@link CaptureRegistry#registerBone(Identifier, Function)} with a string instead of an Identifier.
     * Bones are defined as 7 floats (a 3D coordinate and a quaternion).
     * {@code [x coordinate, y coordinate, z coordinate, quaternion-x, quaternion-y, quaternion-z, quaternion-w]}
     * 
     * @param path The name of the callback. This will be converted into an ID under the vmc-mc default namespace.
     * @param callback A function that uses the client to output a list of floats.
     */
    public static void registerBone (String path, Function<Minecraft, List<Float>> callback) {
        registerBone(ClientVMC.identifier(path), callback);
    }

    /**
     * Registers the standard vmc-mc callbacks. This is called during mod initialisation.
     */
    @SuppressWarnings("DataFlowIssue")
    public static void registerStandardEvents () {
        // Your block light (light gained from torches, etc).
        registerBlendShape("block_light", client -> {
			int lightmap = client.getEntityRenderDispatcher().getPackedLightCoords(client.player, client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
            float blockLight = (lightmap >> 4) & 0xF;
			return blockLight / 15f;
		});

        // This refers to the strength of your sky exposure. It's the same value as from the F3 menu and is NOT the same as actual brightness (but DOES impact it).
        registerBlendShape("sky_light", client -> {
			int lightmap = client.getEntityRenderDispatcher().getPackedLightCoords(client.player, client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
            float skyLight = (lightmap >> 20) & 0xF;
			return skyLight / 15f;
		});

        // The actual light level of the area. Use this if you want brightness that includes blocks and the sky.
        registerBlendShape("internal_light", client -> {
            // Manually calculate light this way because it accounts for mods that modify the player's block-light value.
            float internalLight;
            int lightmap = client.getEntityRenderDispatcher().getPackedLightCoords(client.player, client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
            float blockLight = (lightmap >> 4) & 0xF;
            float skyLight = ((lightmap >> 20) & 0xF) - client.level.getSkyDarken();
            internalLight = Math.max(blockLight, skyLight);
            return internalLight / 15f;
        });

        registerBlendShape("relative_health", client -> client.player.getHealth() / client.player.getMaxHealth());
        registerBlendShape("water_height", client -> (float)client.player.getFluidHeight(FluidTags.WATER));
        registerBlendShape("lava_height", client -> (float)client.player.getFluidHeight(FluidTags.LAVA));
        registerBlendShape("water_submerged", client -> client.player.isUnderWater() ? 1f : 0f);
        registerBlendShape("lava_submerged", client -> client.player.isEyeInFluid(FluidTags.LAVA) ? 1f : 0f);
        registerBlendShape("time_of_day", client -> {
            float time = client.level.getGameTime();
            while (time >= 24000f) {
                time -= 24000f;
            }
            return time / 24000f;
        });
        registerBlendShape("rain_amount", client -> {
            if (client.level.isThundering()) return 1f;
            else if (client.level.isRaining()) return 0.5f;
            else return 0f;
        });
        registerBlendShape("on_fire", client -> client.player.isOnFire() ? 1f : 0f);
        registerBlendShape("experience", client -> (float)client.player.experienceLevel + client.player.experienceProgress);
        registerBlendShape("is_wet", client -> client.player.isInWaterOrRain() ? 1f : 0f);
        registerBlendShape("sneaking", client -> client.player.isShiftKeyDown() ? 1f : 0f);
        registerBlendShape("crawling", client -> client.player.isVisuallyCrawling() ? 1f : 0f);
        registerBlendShape("climbing", client -> client.player.onClimbable() ? 1f : 0f);
        registerBlendShape("blocking", client -> client.player.isBlocking() ? 1f : 0f);
        registerBlendShape("glowing", client -> client.player.isCurrentlyGlowing() ? 1f : 0f);
        registerBlendShape("frozen", client -> client.player.isFullyFrozen() ? 1f : 0f);
        registerBlendShape("swimming", client -> client.player.isSwimming() ? 1f : 0f);
        registerBlendShape("sprinting", client -> client.player.isSprinting() ? 1f : 0f);
        registerBlendShape("riding_living", client -> client.player.getVehicle() instanceof LivingEntity ? 1f : 0f);
        registerBlendShape("riding_nonliving", client -> {
            if (client.player.getVehicle() == null) return 0f;
            return client.player.getVehicle() instanceof LivingEntity ? 0f : 1f;
        });
        registerBlendShape("elytra_flying", client -> client.player.isFallFlying() ? 1f : 0f);
        registerBlendShape("sleeping", client -> client.player.isSleeping() ? 1f : 0f);
        registerBlendShape("alive", client -> client.player.isAlive() ? 1f : 0f);
        registerBlendShape("relative_food_level", client -> (float)client.player.getFoodData().getFoodLevel() / 20f);
        registerBlendShape("relative_air_level", client -> {
            float airLevel = client.player.getAirSupply() > 0 ? client.player.getAirSupply() : 0f;
            return airLevel / (float)client.player.getMaxAirSupply();
        });
        registerBlendShape("hotbar", client -> {
            float slot = client.player.getInventory().getSelectedSlot();
            return (slot + 0.1f) / 10f;
        });
        registerBlendShape("exposed_to_sky", client -> client.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, client.player.blockPosition()).getY() > client.player.getEyeY() ? 0f : 1f);

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
            if (!player.isSpectator() && entity instanceof Player) {
                getBlendStore(ClientVMC.identifier("attack_player")).activate();
            }
			return InteractionResult.PASS;
		});

        registerBlendStore("attack_hostile", new BlendStore(0f, 1f, 0.05f, 50));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!player.isSpectator() && entity instanceof Monster) {
                getBlendStore(ClientVMC.identifier("attack_hostile")).activate();
            }
			return InteractionResult.PASS;
		});

        registerBlendStore("attack_living", new BlendStore(0f, 1f, 0.05f, 50));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!player.isSpectator() && entity instanceof LivingEntity) {
                getBlendStore(ClientVMC.identifier("attack_living")).activate();
            }
			return InteractionResult.PASS;
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
    public static void iterate (Minecraft client) {
        if (client.player == null || client.level == null || !PacketSender.isValid()) return;

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
