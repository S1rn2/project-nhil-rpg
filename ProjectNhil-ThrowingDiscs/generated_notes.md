# Project Nhil RPG for Minecraft Java

## Overview

Project Nhil turns ordinary Minecraft wandering into a proper action adventure. You gain Nhil experience for exploring ruins, salvaging old masonry, fighting monsters and surviving their attacks. Experience raises your Nhil level, which raises your maximum health, and every few levels your character earns a new title. Progress is saved per player, so it follows you through relogging, server restarts and death.

## What Changed in This Iteration

- Levels now use a rising experience curve instead of a flat cost, and characters earn adventuring titles: Wanderer, Delver, Ruin Seeker, Relic Hunter, Nhil Warden and Nhil Ascendant.
- Some hostile monsters now spawn as glowing Nhil Elites with more health and more attack power. Killing one grants a large experience reward and often a Rune Tonic.
- A new Rune Altar block lets you trade one Rune Shard for a solid chunk of Nhil experience, with a chime and enchant particles.
- A new Rune Tonic drink restores a little health and grants short Regeneration and Resistance. It is craftable and also appears in ruin and structure chests.
- The Nhil Blade can now release a short-range rune wave that damages nearby enemies, with a cooldown so it cannot be spammed.
- Taking a heavy blow from a monster has a chance to teach you something, and the on-screen readout now shows a slim experience bar and a "Max" marker at level cap.

## Features

### Progress and titles
Experience comes from discovering landmarks, salvaging ruin blocks, defeating monsters, being struck hard by monsters, using the Rune Altar and using the Wayfinder Compass. Early levels are quick; later levels ask for more. Each level adds one point of maximum health permanently.

### Ranks
Six ranks unlock across the twenty levels. The rank list is readable with a command, and your current rank appears on join and in your progress report.

### Elite monsters
Roughly one in sixteen hostile monsters becomes an elite. Elites have half again as much health, noticeably more attack power, a glowing outline and a purple name. They are marked permanently, so an elite stays an elite after a reload. Elites pay out much more experience and often drop a Rune Tonic.

### Rune Altar
Craft it from a Rune Shard, a Deep Ruin Tile and Amethyst Blocks. Right-click it while holding a Rune Shard to convert the shard into experience. Holding anything else simply reminds you what it wants.

### Rune Tonic
A drinkable remedy made from a Rune Shard, a Glass Bottle and Glow Berries. It restores a small amount of hunger and health and grants short Regeneration and Resistance.

### Nhil Blade rune wave
Right-click with the Nhil Blade to sweep rune energy through nearby creatures. It hits a limited number of targets, scales slightly with your Nhil level, and has a cooldown.

### Wayfinder Compass
Still points you toward the nearest ruin or abandoned site and now also grants experience and records the landmark.

## How to Use

- Explore: walk near a ruin, abandoned site or dungeon and the mod records the landmark and grants experience.
- Salvage: mine Ruin Bricks, Mossy Ruin Bricks, Deep Ruin Tiles, Rune Lamps or Rune Altars for a little experience each.
- Fight: defeat monsters for experience and a chance at a Rune Shard or Rune Tonic.
- Trade in shards: place a Rune Altar and right-click it with Rune Shards.
- Check your status in game with the `nhil` command, or `nhil progress` for your rank, level and experience bar.
- Server operators can list every rank with `nhil ranks`, hand out experience with `nhil grant <amount>`, or give relics with `nhil relic`.

## Tips and Tricks

- Elites are worth hunting. They are glowing and named, so they are easy to spot, and their rewards are far larger than a normal monster's.
- Rune Shards have two uses: crafting gear, or feeding a Rune Altar for experience. Keep a few back for the altar and for the Nhil Blade recipe.
- Rune Tonics are cheap to brew once you have shards, and the Regeneration they grant stacks well with the health bonus from your level.
- The rune wave on the Nhil Blade is best used when you are surrounded; it is capped on targets and has a cooldown, so it is not a replacement for normal attacks.

## Textures and Models

All blocks and items reuse art that already ships with the game, so nothing renders as a missing texture and nothing needs downloading:

- Ruin Brick, Mossy Ruin Brick, Deep Ruin Tile and Rune Lamp use this mod's own existing textures.
- Rune Altar uses the vanilla Deepslate Tiles texture on its sides and bottom, with the vanilla Amethyst Block texture on top.
- Rune Tonic uses the vanilla Honey Bottle texture.
- Rune Shard, Ancient Relic, Wayfinder Compass and Nhil Blade keep the textures this project already shipped.

If you want a distinct look for the altar or the tonic, those are the two easiest places to drop in replacement art.

## Known Limitations

- Elite status is decided when a monster first enters the world. Monsters already standing around when you install this version are not retroactively evaluated.
- The rune wave hits a limited number of nearby creatures rather than everything in range, to keep large fights cheap on the server.
- Landmark discovery still relies on Minecraft's own structure data, so it only recognises the structure types the mod already tracks.
