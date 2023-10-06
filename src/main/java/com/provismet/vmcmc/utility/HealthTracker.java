package com.provismet.vmcmc.utility;

import com.provismet.vmcmc.ClientVMC;
import com.provismet.vmcmc.vmc.CaptureRegistry;

public class HealthTracker {
    private static float previousHealth = 0;

    public static void update (float newValue) {
        if (newValue < previousHealth) CaptureRegistry.getBlendStore(ClientVMC.identifier("damage_taken")).activate();
        previousHealth = newValue;
    }
}
