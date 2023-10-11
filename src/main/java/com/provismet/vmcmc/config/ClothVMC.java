package com.provismet.vmcmc.config;

import com.provismet.vmcmc.vmc.PacketSender;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClothVMC {
    public static Screen build (Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create();
        builder.setParentScreen(parent);
        builder.setTitle(Text.translatable("title.vmcmc.config"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.vmcmc.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startStrField(Text.translatable("entry.vmcmc.general.ip"), Config.getIP())
            .setDefaultValue(PacketSender.LOCALHOST)
            .setTooltip(Text.translatable("tooltip.vmcmc.general.ip"))
            .setSaveConsumer(newValue -> Config.setIP(newValue))
            .build()
        );

        general.addEntry(entryBuilder.startIntField(Text.translatable("entry.vmcmc.general.port"), Config.getPort())
            .setDefaultValue(PacketSender.DEFAULT_PORT)
            .setTooltip(Text.translatable("tooltip.vmcmc.general.port"))
            .setSaveConsumer(newValue -> Config.setPort(newValue))
            .build()
        );

        builder.setSavingRunnable(() -> {
            Config.saveJSON();
            PacketSender.initPort(Config.getIP(), Config.getPort());
        });

        return builder.build();
    }
}
