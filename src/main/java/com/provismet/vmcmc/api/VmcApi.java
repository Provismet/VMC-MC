package com.provismet.vmcmc.api;

/**
 * Entrypoint for compatibility with VMC-MC.
 * Mods that wish to add functionality to this mod should implement this interface and list the class as a "vmc-mc" entrypoint in fabric.mod.json.
 */
public interface VmcApi {
    /**
     * An initializer only run on the client-side.
     * 
     * Adding blendshapes, VMC related client-ticks, and hooks to blendstores should be handled within this method.
     */
    void onInitializeVMC ();
}
