---
layout: page
title: Schematic and Folia Setup
description: Complete A-Z workflow for creating, saving, placing, configuring, and testing SinceDungeon schematic dungeons.
---

Core's default `WORLD` provider clones full template world folders at runtime. That works on Paper, but Folia does not
support Bukkit runtime world creation. For Folia, use Premium `SCHEMATIC` shared-world mode with a preloaded shared
world.

This guide explains the full schematic workflow: how to build the map, where to stand when saving the schematic, where
to put the file, how coordinates work, and how to test the dungeon correctly.

## Required Plugins

- SinceDungeon Core
- SinceDungeon Premium Addon
- WorldEdit or FastAsyncWorldEdit

Premium schematic files go here:

```text
plugins/SinceDungeon-PremiumAddon/schematics/
```

Dungeon YAML files still go here:

```text
plugins/SinceDungeon/dungeons/
```

## Core Rule

The dungeon YAML uses local schematic coordinates, not runtime shared-world coordinates.

Use this standard for every schematic dungeon:

```text
Schematic origin: 0,64,0
Player entry:     0,65,0
Paste Y level:    64
```

When saving the schematic with WorldEdit, stand at the local origin before `//copy`:

```text
/tp <your_name> 0 64 0
//copy
//schem save ForgottenCrypt_Template
```

If you copy while standing somewhere else, WorldEdit stores a different clipboard offset. SinceDungeon will still paste
the schematic at `paste-y-level`, but mobs, chests, doors, triggers, checkpoints, and spawn locations can become shifted.

## File Naming

The dungeon file name is the dungeon ID:

```text
plugins/SinceDungeon/dungeons/forgotten_crypt.yml
```

The schematic file name is controlled by `template-world`:

```yaml
template-world: "ForgottenCrypt_Template"
```

Valid schematic files:

```text
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schem
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schematic
```

Do not include `.schem` or `.schematic` in `template-world`.

## Premium Config

Set this in:

```text
plugins/SinceDungeon-PremiumAddon/config.yml
```

Recommended Paper/Folia shared-world config:

```yaml
instancing:
  mode: "SCHEMATIC"
  paste-y-level: 64
  world-settings:
    autosave: false
    time: 6000
    storm: false
    thundering: false
  schematic:
    paste-air: true
    shared-world:
      enabled: true
      name: "SDPremium_Schematic"
      spawn-location: "0,65,0"
      coordinate-y-offset: 0
      region-spacing: 2048
      region-radius: 512
      grid-width: 128
      clear-on-release: true
      clear-min-y: -64
      clear-max-y: 320
```

Important fields:

| Field | Meaning |
|-------|---------|
| `mode` | Must be `SCHEMATIC` to use Premium schematic instancing. |
| `paste-y-level` | Y level where the schematic clipboard origin is pasted. |
| `paste-air` | If `true`, air blocks in the schematic overwrite previous blocks. |
| `shared-world.enabled` | Puts all runs in separated regions inside one shared world. |
| `shared-world.name` | Shared world name. On Folia, this world must already be loaded. |
| `spawn-location` | Default local entry location if the dungeon YAML has `start-location: "NONE"`. |
| `coordinate-y-offset` | Extra Y offset added to YAML coordinates. Keep `0` for normal setup. |
| `region-spacing` | Distance between allocated dungeon regions. |
| `region-radius` | Half-size owned by each dungeon run for routing and cleanup. |
| `clear-on-release` | Clears the region after a dungeon run ends. |

## Folia Requirement

On Folia, the shared world must exist and be loaded before SinceDungeon Premium enables.

SinceDungeon Premium will not create or load this world dynamically on Folia:

```yaml
shared-world:
  name: "SDPremium_Schematic"
```

If the console says the shared world is missing:

1. Create a world named `SDPremium_Schematic`.
2. Configure your server/world manager to load it on startup.
3. Restart the server.
4. Confirm the world is loaded before testing `/dungeon join`.

## Build The Dungeon

Use a normal builder world. It does not need to be the same as the runtime shared world.

Recommended build layout:

- Build the dungeon around local `0,64,0`.
- Put the player entry point at `0,65,0`.
- Keep the entire map inside the configured `region-radius`.
- Keep the whole height inside `clear-min-y` and `clear-max-y`.
- Mark important points while building, then remove temporary markers before saving.

Example coordinate plan:

```text
0,65,0      player entry
10,64,10    first reach objective
15,64,20    mob spawn
20,60,-5    loot chest
0,64,-10    locked gate trigger
0,64,-50    boss room
```

## Save The Schematic

Use WorldEdit or FastAsyncWorldEdit:

1. Select the whole dungeon with `//wand`.
2. Stand at the local origin:

```text
/tp <your_name> 0 64 0
```

3. Copy the selection:

```text
//copy
```

4. Save it:

```text
//schem save ForgottenCrypt_Template
```

5. Put the saved file in:

```text
plugins/SinceDungeon-PremiumAddon/schematics/
```

## Dungeon YAML

Minimal schematic dungeon:

```yaml
template-world: "ForgottenCrypt_Template"
public: true

settings:
  start-location: "0,65,0"
  max-players: 6
  required-lives-to-join: 1
  lives-deducted-per-death: 1
  lives-deducted-on-leave: 0
  lives-deducted-on-fail: 0
  lives-deducted-on-clear: 0
  keep-inventory-on-death: true
  prevent-item-dropping: true
  block-ender-pearls: true
  kick-delay-after-finish: 15
  force-daylight-and-clear-weather: true
  save-and-restore-stats: true
  death-action: "RESPAWN"
  clear-mob-drops: true
  randomize-stages: false
  cooldown-seconds: 1800
  cooldown-on-leave: true

rewards:
  solo-tiers:
    300: 3
    600: 2
    1200: 1
  party-tiers:
    240: 3
    500: 2
    1000: 1
  pool:
    diamond:
      type: "ITEM"
      value: "DIAMOND:3-8"
      chance: 100.0
      name: "<aqua>Diamonds"

stages:
  1:
    chance: 100.0
    actions:
      arrival:
        type: "REACH_LOCATION"
        target: "10,64,10"
        radius: 3.0
```

`settings.start-location` also supports yaw and pitch:

```yaml
settings:
  start-location: "0,65,0,180,0"
```

## Coordinate Rules

Use local coordinates from the builder world:

```yaml
target: "10,64,10"
location: "20,60,-5"
trigger: "0,64,-10"
corner1: "-2,64,-10"
corner2: "2,68,-10"
```

In shared-world mode, SinceDungeon allocates a region and offsets locations internally.

Example:

```yaml
region-spacing: 2048
```

If a run is assigned to region origin `2048,0,0`, then:

```text
YAML target 10,64,10 -> real location 2058,64,10
YAML chest 20,60,-5 -> real location 2068,60,-5
```

Do not paste those runtime coordinates back into YAML. Keep YAML local.

## Region Size

The dungeon must fit inside `region-radius`.

With:

```yaml
region-radius: 512
```

The map should stay within:

```text
X: -512 to 512 from local origin
Z: -512 to 512 from local origin
```

For large dungeons:

```yaml
region-spacing: 4096
region-radius: 1500
```

`region-spacing` should be larger than `region-radius * 2`.

## Recommended Setup Order

1. Build the map in a builder world.
2. Decide the local origin, normally `0,64,0`.
3. Place the entry point, normally `0,65,0`.
4. Mark mob spawns, doors, chests, triggers, boss rooms, checkpoints, and traps.
5. Select the full dungeon with WorldEdit.
6. Stand at `0,64,0`.
7. Run `//copy`.
8. Run `//schem save <template-world>`.
9. Put the schematic in `plugins/SinceDungeon-PremiumAddon/schematics/`.
10. Create `plugins/SinceDungeon/dungeons/<dungeon_id>.yml`.
11. Set `template-world` to the schematic base filename.
12. Add `settings.start-location`.
13. Add stages/actions using local coordinates.
14. Add rewards, lives, cooldowns, entry conditions, and commands.
15. Run `/sincedungeon reload` or `/sdp reload`.
16. Test with `/dungeon join <dungeon_id>`.

## Action Placement

Common layout:

- Entrance: `REACH_LOCATION`
- Combat room: `SPAWN_WAVE`, `RANDOM_WAVE`, or `MYTHIC_WAVE`
- Key path: `LOOT_CHEST` gives `KEY:<id>:1`, then `UNLOCK_DOOR` consumes it
- Door/wall: exact block coordinates for `corner1` and `corner2`
- Boss room: `BOSS_BATTLE` or `MYTHIC_WAVE`
- Checkpoint: Premium `CHECKPOINT`
- Trap room: Premium `DAMAGE_ZONE` or `PROJECTILE_TRAP`
- NPC flow: Premium `NPC_INTERACTION` or `ESCORT_NPC`

Example locked gate:

```yaml
stages:
  2:
    chance: 100.0
    actions:
      find_key:
        type: "LOOT_CHEST"
        location: "20,60,-5"
        items:
          13: "KEY:gate_key:1"
      open_gate:
        type: "UNLOCK_DOOR"
        key_id: "gate_key"
        trigger: "0,64,-10"
        corner1: "-2,64,-10"
        corner2: "2,68,-10"
```

## Test Checklist

Run:

```text
/sincedungeon reload
/dungeon join forgotten_crypt
```

Check:

- The console does not warn that the schematic is missing.
- The dungeon pastes at the correct Y level.
- Players enter at the intended start location.
- Reach objectives complete in the correct place.
- Mobs spawn inside the intended rooms.
- Chests, doors, triggers, walls, levers, checkpoints, and traps use correct block positions.
- Bosses and NPCs are inside the region radius.
- Rewards open after clear.
- The region clears after the run ends.
- A second run starts cleanly.

## Troubleshooting

### Schematic Not Found

- Check the file is in `plugins/SinceDungeon-PremiumAddon/schematics/`.
- Check `template-world` matches the file base name.
- Check the extension is `.schem` or `.schematic`.
- Reload or restart after adding the file.

### Spawn Or Actions Are Offset

The schematic was probably copied from the wrong player position.

Fix:

```text
/tp <your_name> 0 64 0
//copy
//schem save <template-world>
```

Replace the old schematic and test again.

### Schematic Is Too High Or Too Low

Check:

```yaml
instancing:
  paste-y-level: 64
```

The copy-origin Y level and `paste-y-level` should match your coordinate plan.

### Runtime Coordinates Are Huge

That is normal in shared-world mode because regions are separated by `region-spacing`. Do not use those coordinates in
YAML.

### Region Does Not Clean Fully

Check:

- `clear-on-release: true`
- `region-radius` covers the whole map
- `clear-min-y` and `clear-max-y` cover the whole height
- WorldEdit or FAWE is installed and working

## Release Checklist

Before setting a dungeon public:

- `template-world` matches the schematic base filename.
- The schematic was copied while standing at local `0,64,0`, or your chosen origin.
- `paste-y-level` matches the copy-origin Y level.
- `settings.start-location` is inside the schematic and safe.
- Every action coordinate is local to the schematic.
- The whole dungeon fits inside `region-radius`.
- Cleanup bounds include the whole dungeon height.
- Reward pool has at least one valid reward.
- Conditions and required items are tested with a normal player account.
- `/dungeon join <dungeon_id>` works multiple times in a row.
