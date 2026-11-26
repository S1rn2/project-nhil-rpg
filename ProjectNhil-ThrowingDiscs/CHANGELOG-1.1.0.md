# 1.1.0 — Throwing Discs merged into Project Nhil

One mod, one jar, mod id `projectnhilrpgforminecraftjava`. Throwing Discs keeps its own
registry namespace (`throwingdiscs:`), so item ids match the standalone disc mod.
Project Nhil v2 code is untouched except for the three small edits listed below.

## Status: NOT COMPILED
This sandbox cannot reach Mojang or Fabric servers, so nothing here has been built or run in
Minecraft. There is no jar in this package. Build it yourself (steps at the bottom).

## What was checked (static checks only)
- All 69 JSON resource files parse.
- The new Java files parse with no syntax errors (javac, run without the Minecraft jar; every
  remaining error is a missing-Minecraft-class error).
- Every Minecraft/Fabric method name used by the disc code was looked up in the Yarn
  1.21.11 mappings, the Fabric API source, or Project Nhil's own compiled code.
  Caveat: the Yarn and Fabric API copies were the 1.21.11 branch heads, which may be slightly
  newer than the build.4 / 0.141.1 versions this project pins.
- Two real compile errors in the earlier Throwing Discs draft were found and fixed this way
  (`getWorld()` and the NBT read/write methods no longer exist in 1.21.11).

## Added (code present, NOT yet tested in game)
- Seven discs: copper, iron, golden, diamond, netherite, emerald, obsidian, in the Combat tab.
- Throw with right click. The disc flies straight, ricochets off solid blocks (max 6 bounces),
  damages every mob it passes (max 3 hits per mob, 6-tick gap between hits), then homes back
  and is caught on contact.
- Kills by a disc are credited to the thrower (damage source attacker = owner), which is how
  Nhil's existing kill-experience code decides who gets XP. Untested in game.
- Durability: 1 point per completed throw. A disc with no durability left vanishes on catch.
- Persistence: an in-flight disc saves its owner, item and state, so it survives a reload.
  If the owner is offline, dead or in another dimension when it returns, the disc drops as an item.
- Shaped crafting recipe for every tier (ring pattern).
- Performance design: server-only logic; one entity query and at most four block lookups per
  tick per disc; unloaded chunks count as walls so a disc cannot force a chunk load.

## Changed in existing files
1. `PGPlugin.java`: one import and one line, `ThrowingDiscsContent.initialize();`
2. `fabric.mod.json`: second client entrypoint and a longer description.
3. `gradle.properties`: `mod_version` 1.0.0 -> 1.1.0.

## Known limitations
- Flying discs draw as a flat item sprite, not the spinning 3D model from the Bedrock addon.
- Not ported from the Bedrock addon: off-hand / crouch throw, enchanting, smelting and
  smithing recipes (the netherite disc is a plain crafting recipe here).
- On the way home the disc ignores blocks, so it always reaches you.
- A caught disc goes to the first free inventory slot, not the slot it was thrown from.
- Per-mob hit limits reset if the disc is saved and reloaded mid-flight.
- Disc motion smoothness on clients (extrapolation between server updates) has not been seen
  in game and may need tuning.
- The Rune Tonic code comment still says "two hearts of healing" while the code restores hunger
  and applies Regeneration. Comment only, behaviour is correct.
- The large RPG expansion (dungeons, quests, bosses, skill tree, weapons) is NOT part of this
  release. It was discussed and deliberately left for later phases.

## Build
Needs Java 21 and internet access (Gradle and Minecraft download on first run).

    ./gradlew build            (Windows: gradlew.bat build)

Output: `build/libs/projectnhilrpgforminecraftjava-1.1.0.jar`
(ignore the `-sources` jar). Put it in `mods/` with Fabric API for 1.21.11.
Versions pinned in gradle.properties are the ones Nhil v2 already built with:
Yarn 1.21.11+build.4, Loader 0.18.1, Fabric API 0.141.1+1.21.11, Loom 1.14.4, Gradle 9.2.0.

## Correction (after first delivery)
- `ThrowingDiscsContent` used `ItemGroups.COMBAT`, which is private in 1.21.11 (Project Nhil's own
  NhilContent already notes this). It now builds the key from `Identifier.ofVanilla("combat")`.
  The first zip would have failed to compile on that line.
