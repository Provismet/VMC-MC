package com.provismet.vmcmc.vmc;

/**
 * Used for action/event callbacks that communicate BlendShapes.
 * BlendStores are a layer of abstraction that allows instant events to be read as states with lingering values.
 * 
 * BlendShapes are intended for states that can be read every tick, and therefore don't fit well with event callbacks.
 */
public class BlendStore {
    private float currentValue;
    private int remainingDuration;
    private final float baseValue;
    private final float activationValue;
    private final float decay;
    private final int duration;

    /**
     * @param initialValue The base value of the BlendStore.
     * @param activationValue The BlendStore's value with be set to this when activated.
     * @param decay The number of steps the value takes towards the base after each {@link BlendStore#get()} call.
     * @param duration The number of {@link BlendStore#get()} calls until value decay.
     */
    public BlendStore (float initialValue, float activationValue, float decay, int duration) {
        this.currentValue = initialValue;
        this.remainingDuration = duration;
        this.baseValue = initialValue;
        this.activationValue = activationValue;
        this.decay = decay;
        this.duration = duration;
    }

    /**
     * Retrieves the current value of the BlendStore then applies duration and decay effects.
     * This is called every tick.
     * 
     * @return The current value of the BlendStore.
     */
    public float get () {
        float temp = this.currentValue;
        if (this.currentValue != this.baseValue) {
            if (this.remainingDuration <= 0) {
                if (Math.abs(this.currentValue - this.baseValue) <= this.decay) this.currentValue = this.baseValue;
                else if (this.currentValue > this.baseValue) this.currentValue -= this.decay;
                else this.currentValue += this.decay;
            }
            else --this.remainingDuration;
        }
        return temp;
    }

    /**
     * Retrieves the current value of the BlendStore without changing any values.
     * @return The current value of the BlendStore.
     */
    public float read () {
        return this.currentValue;
    }

    /**
     * Actives the BlendStore. This should be called in the associated event callback.
     */
    public void activate () {
        this.currentValue = this.activationValue;
        this.remainingDuration = this.duration;
    }
}
