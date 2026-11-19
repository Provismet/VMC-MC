package com.provismet.vmcmc.config;

import com.provismet.vmcmc.vmc.PacketSender;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothVMC {
    public static Screen build (Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create();
        builder.setParentScreen(parent);
        builder.setTitle(Component.translatable("title.vmcmc.config"));

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.vmcmc.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startStrField(Component.translatable("entry.vmcmc.general.ip"), Config.getIP())
            .setDefaultValue(PacketSender.LOCALHOST)
            .setTooltip(Component.translatable("tooltip.vmcmc.general.ip"))
            .setSaveConsumer(Config::setIP)
            .build()
        );

        general.addEntry(entryBuilder.startIntField(Component.translatable("entry.vmcmc.general.port"), Config.getPort())
            .setDefaultValue(PacketSender.DEFAULT_PORT)
            .setTooltip(Component.translatable("tooltip.vmcmc.general.port"))
            .setSaveConsumer(Config::setPort)
            .build()
        );

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("entry.vmcmc.general.compat_identifiers"), Config.shouldUseCompatibilityIds())
            .setDefaultValue(false)
            .setTooltip(Component.translatable("tooltip.vmcmc.general.compat_identifiers"))
            .setSaveConsumer(Config::setCompatibilityIdMode)
            .build()
        );

        builder.setSavingRunnable(() -> {
            Config.saveJSON();
            PacketSender.initPort(Config.getIP(), Config.getPort());
        });

        return builder.build();
    }
}
