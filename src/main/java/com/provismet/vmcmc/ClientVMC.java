package com.provismet.vmcmc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.provismet.vmcmc.api.VmcApi;
import com.provismet.vmcmc.config.Config;
import com.provismet.vmcmc.vmc.CaptureRegistry;
import com.provismet.vmcmc.vmc.PacketSender;

public class ClientVMC implements ClientModInitializer {
	public static final String MODID = "vmc-mc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static Identifier identifier (String path) {
		return new Identifier(MODID, path);
	}

	@Override
	public void onInitializeClient () {
		Pair<String,Integer> portInfo = Config.readJSON();
		PacketSender.initPort(portInfo.getLeft(), portInfo.getRight());
		CaptureRegistry.registerStandardEvents();

		FabricLoader.getInstance().getEntrypointContainers(MODID, VmcApi.class).forEach(entrypoint -> {
			String otherModId = entrypoint.getProvider().getMetadata().getId();
			try {
				entrypoint.getEntrypoint().onInitializeVMC();
			}
			catch (Throwable e) {
				LOGGER.error("Error caused by mod " + otherModId + " during integration.", e);
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			CaptureRegistry.iterate(client);
		});
	}
}