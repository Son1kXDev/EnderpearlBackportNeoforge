## Enderpearl Backport

**Enderpearl Backport** brings the modern Ender Pearl mechanics introduced in **Minecraft 1.21.2+** to earlier Fabric versions.

The goal of this mod is **full behavioral parity with vanilla 1.21.2+ Ender Pearls**, including chunk loading, cross-dimension teleportation, and persistence — features that do **not exist** in vanilla versions below 1.21.2.

---

## Features

### Cross-Dimensional Teleportation
Ender Pearls can teleport players **across dimensions**, including cases where:
- the pearl enters a portal,
- the pearl lands in a different dimension than the player.

This behavior is **vanilla-accurate** to Minecraft 1.21.2+.

---

### Forced Chunk Loading
While an Ender Pearl is in flight:
- the chunk it occupies is **force-loaded**,
- multiple pearls in the same chunk are **reference-counted**,
- chunks unload **only when the last pearl leaves**.

This prevents pearls from freezing, despawning, or losing momentum.

---

### Persistent Pearls (Logout & Rejoin)
If a player logs out while their Ender Pearl is in flight:
- the pearl is **removed from the world**,
- its exact state (position, velocity, dimension) is saved,
- upon rejoining, the pearl is **fully restored** and continues flying naturally.

---

### World-Safe Persistence
Active Ender Pearls are stored using Minecraft’s native  
`PersistentState` system (per-world data), ensuring:

- safe recovery after server restarts,
- automatic cleanup after restoration,
- no external JSON config files.

---

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

---

## Why This Mod Exists

In vanilla Minecraft versions **below 1.21.2**, Ender Pearls:

- cannot teleport across dimensions  
- do not keep chunks loaded  
- cannot be safely restored  

This mod **backports the exact modern behavior**, without hacks or approximations.

---

## Loader & Requirements

- **Mod Loader:** Fabric
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
