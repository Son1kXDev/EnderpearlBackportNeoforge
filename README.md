## Enderpearl Backport

**Ender Pearl Backport** brings the improved Ender Pearl mechanics from *Minecraft 1.21.2+* back to older versions.
With this mod, Ender Pearls behave exactly like in newer versions — they teleport players across dimensions through portals and persist even after player logout or server restart.

---

### Features:

* **Cross-Dimensional Teleportation**
Ender Pearls can now teleport players between the **Overworld** and **Nether**, correctly determining the target dimension.

* **Persistent Ender Pearls**
If a player logs out while their pearl is in flight — it will **not disappear**. When the player rejoins, the pearl **restores** at the exact same position, with the same velocity and direction.

* **Survives Server Restarts**
Pearl flight data is stored in `config/enderpearlbackport_data.json`, allowing active pearls to be **restored even after a full server restart**.

* **Safe Teleportation Logic**
Automatically searches for a safe teleport spot, avoiding lava, void, and unsafe blocks.

*  **Optimized Chunk Loading**
Uses a lightweight `EnderpearlChunkManager` to temporarily load only the chunks required to ensure pearls don’t freeze or vanish during dimension transfers.


### Technical Details:

* All active pearls are tracked and saved by player UUID.
* When the player rejoins, a new `Enderpearl` entity is spawned with identical position, motion, and owner.
* Saved data is automatically cleared after restoration.
* Persistent data file: `config/enderpearlbackport_data.json`.
