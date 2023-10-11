package com.provismet.vmcmc.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.loader.api.FabricLoader;

public class ModMenuHook implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory () {
        if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return parent -> {
                return ClothVMC.build(parent);
            };
        }
        else return parent -> null;
    }
}
