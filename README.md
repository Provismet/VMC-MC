# Virtual Motion Capture for Minecraft
This mod uses (and bundles) the JavaOSC library to implement a VMC layer in Minecraft so vtuber models can be manipulated via in-game actions and events.

## Default Tracking
Almost all blendshapes sent by VMC-MC are bound to the range of 0 - 1. Some blendshapes are also boolean, only outputting 0 or 1, with no in-between.

By default, the mod sends the following tracking data:
- time of in-game day
- light level
    - block light
    - sky light
    - internal light
        - the actual visual light level that is used for spawn mechanics
- weather (clear/rain/thunder)
- health percentage
- food percentage
- water height (uncapped value, experimentation recommended)
- lava height (uncapped value, experimentation recommended)
- experience levels (uncapped value)
- sneaking (boolean)
- sleeping (boolean)
- alive/dead (boolean)
- is wet (boolean)
- on fire (boolean)

## Dependency
If you want to use this mod as a dependency, it is available via [jitpack.io](https://jitpack.io/#Provismet/VMC-MC/).

Add the following your your repositories (at the top of your build.gradle):
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

Then add the following to your dependencies:
```gradle
dependencies {
    modImplementation 'com.github.Provismet:VMC-MC:0.1.0'
}
```
Optionally, the tag may be replaced with `$(project.vmcmc_version)`, where this value is defined in your gradle.properties.

