## Enderpearl Backport

**Enderpearl Backport** brings the modern Ender Pearl mechanics introduced in **Minecraft 1.21.2+** to earlier Fabric versions.

The goal of this mod is **full behavioral parity with vanilla 1.21.2+ Ender Pearls**, including chunk loading, cross-dimension teleportation, and persistence — features that do **not exist** in vanilla versions below 1.21.2.

### Version-Aware Architecture
The mod is built with a **clean version-bridge architecture**:

- Core logic is version-agnostic.
- Minecraft-specific code (teleporting, registries, persistence) is isolated per version.
- Supporting a new Minecraft version usually requires **only adding a new version folder**, not rewriting the mod.

Currently supported:
- **Minecraft 1.20.x**
- **Minecraft 1.21.x (up to 1.21.1)**

---

## Technical Overview

- Pearls are tracked per **player UUID**.
- Chunk loading uses a **reference-counted system** to avoid premature unloads.
- Cross-dimension teleportation is handled via a version-specific bridge.
- All persistence uses vanilla world data (`PersistentState`).
- No gameplay logic is duplicated between versions.


## Loader & Requirements

- **Mod Loader:** NeoForge
- **Java:** 21+
- **Minecraft:** 1.20.x – 1.21.1

---

## For Mod Developers

The project is structured to make future ports easy:

```
src/
├─ main/ # shared, version-independent logic
├─ v120/ # Minecraft 1.20.x implementations
└─ v121/ # Minecraft 1.21.x implementations
```


To add support for a new version:
1. Create a new `vXYZ` folder.
2. Implement version-specific adapters only.
3. No changes to core logic required.

---

## License
[Creative Commons Attribution 4.0 International Public License](https://github.com/Son1kXDev/EnderpearlBackport/tree/main?tab=License-1-ov-file)
