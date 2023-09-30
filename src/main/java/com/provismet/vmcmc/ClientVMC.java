package com.provismet.vmcmc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.provismet.vmcmc.vmc.CaptureRegistry;
import com.provismet.vmcmc.vmc.PacketSender;

public class ClientVMC implements ClientModInitializer {
	public static final String MODID = "vmc-mc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static Identifier identifier (String path) {
		return Identifier.of(MODID, path);
	}

	@Override
	public void onInitializeClient () {
		PacketSender.initPort(39541);
		CaptureRegistry.registerStandardEvents();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			CaptureRegistry.iterate(client);
		});
	}
}