package com.provismet.vmcmc.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.loader.api.FabricLoader;

public class ModMenuHook implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory () {
        if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return ClothVMC::build;
        }
        else return parent -> null;
    }
}
