<div align="center">

# Virtual Motion Capture for Minecraft
[![](https://img.shields.io/jitpack/version/com.github.Provismet/VMC-MC?style=flat-square&logo=jitpack&color=F6F6F6)](https://jitpack.io/#Provismet/VMC-MC) [![](https://img.shields.io/modrinth/dt/ub8B8TcT?style=flat-square&logo=modrinth&color=F6F6F6)](https://modrinth.com/mod/virtual-motion-capture-for-minecraft) [![](https://img.shields.io/curseforge/dt/922189?style=flat-square&logo=curseforge&color=F6F6F6)](https://www.curseforge.com/minecraft/mc-mods/vmc-mc)

</div>

This mod uses (and bundles) the JavaOSC library to implement a VMC layer in Minecraft so vtuber models can be manipulated via in-game actions and events.

This mod is made specifically for use with [Inochi Session](https://github.com/Inochi2D/inochi-session), but has been tested with [VMC Protocol Monitor](https://github.com/gpsnmeajp/VMCProtocolMonitor) and should work universally.  
The VMC protocol (and by extension: Open Sound Control) is used almost universally by vtubing applications - this mod *should* in principle be compatible with most standard vtubing software.

## Setup
By default the mod will send data over `127.0.0.1:35940` (localhost, port 35940).  
After the first launch, a config file will be created under `config/vmc-mc.json`. If you wish to change the IP or port (for example, if you use multiple PCs to stream/record) then change the IP and port as necessary in this file. The changes will be read on the next launch.  
You may also change the port and IP via the mod's setting menu (available only if you have Mod Menu and Cloth Config).

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

## Dependency Implementation and Addons
### Adding VMC-MC To Your Workspace
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
    modImplementation 'com.github.Provismet:VMC-MC:0.4.0'
}
```
Optionally, the tag may be replaced with `$(project.vmcmc_version)`, where this value is defined in your gradle.properties.

### Extending VMC-MC
VMC-MC creates an [entrypoint](https://fabricmc.net/wiki/documentation:entrypoint) that other mods may use for easy extension.  
Create a class that implements the `VmcApi` interface. Code within the `onInitializeVMC()` function will be called at the end of VMC-MC's setup. Use this function to register additional BlendShapes or VMC related client events.

In your `fabric.mod.json` add an entrypoint labeled `vmc-mc` to the entrypoints object, for example:
```json
"entrypoints": {
	"client": [
	    "path.to.your.client.initializer"
	],
    "vmc-mc": [
	    "path.to.your.vmc.initializer"
    ]
}
```

Use `CaptureRegistry.registerBlendShape(...)` and `CaptureRegistry.registerBlendStore(...)` to register additional outputs.  
See `CaptureRegistry.registerStandardEvents()` for examples.
