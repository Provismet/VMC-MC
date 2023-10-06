# Virtual Motion Capture for Minecraft
This mod uses (and bundles) the JavaOSC library to implement a VMC layer in Minecraft so vtuber models can be manipulated via in-game actions and events.

This mod is made specifically for use with [Inochi Session](https://github.com/Inochi2D/inochi-session), but has been tested with [VMC Protocol Monitor](https://github.com/gpsnmeajp/VMCProtocolMonitor) and should work universally.  
The VMC protocol (and by extension: Open Sound Control) is used almost universally by vtubing applications - this mod *should* in principle be compatible with most standard vtubing software.

## Setup
By default the mod will send data over `127.0.0.1:35404` (localhost, port 35404).  
After the first launch, a config file will be created under `config/vmc-mc.json`. If you wish to change the IP or port (for example, if you use multiple PCs to stream/record) then change the IP and port as necessary in this file. The changes will be read on the next launch.

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
- air percentage
- water height (uncapped value, experimentation recommended)
- lava height (uncapped value, experimentation recommended)
- experience levels (uncapped value)
- sneaking (boolean)
- sleeping (boolean)
- alive/dead (boolean)
- is wet (boolean)
- on fire (boolean)
- water submerged (boolean)
- lava submerged (boolean)
- crawling (boolean)
- climbing (boolean)
- blocking (boolean)
- glowing (boolean)
- frozen (boolean)
- swimming (boolean)
- sprinting (boolean)
- riding living (boolean)
- riding nonliving (boolean)
- elytra flying (boolean)

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
    modImplementation 'com.github.Provismet:VMC-MC:0.2.0'
}
```
Optionally, the tag may be replaced with `$(project.vmcmc_version)`, where this value is defined in your gradle.properties.

