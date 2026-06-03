# Schematic and Instancing Setup

This page explains the complete A-Z workflow for building a dungeon as a schematic, saving it correctly, placing it in
SinceDungeon Premium, configuring the dungeon YAML, and testing that every coordinate works.

## Core Idea

SinceDungeon has two instance providers:

- `WORLD`: Core mode. The plugin clones a complete template world folder for every dungeon run.
- `SCHEMATIC`: Premium mode. The plugin creates or reuses a void world and pastes a `.schem` or `.schematic` file for
  every dungeon run.

For schematic mode, the schematic is not the dungeon ID. The dungeon ID is the YAML file name in
`plugins/SinceDungeon/dungeons/`. The schematic file name is controlled by the dungeon's `template-world` value.

Example:

```yaml
template-world: "ForgottenCrypt_Template"
```

Valid schematic files:

```text
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schem
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schematic
```

## Required Plugins

Install these before using schematic mode:

- SinceDungeon Core
- SinceDungeon Premium Addon
- WorldEdit or FastAsyncWorldEdit

For Folia, use Premium shared-world schematic mode. Folia cannot create or load Bukkit worlds dynamically during a
dungeon run, so the shared schematic world must already be loaded before SinceDungeon Premium enables.

## Recommended Coordinate Standard

Use one coordinate system for every dungeon:

- Dungeon local origin: `0,0,0`
- Recommended dungeon floor Y: `64`
- Recommended schematic paste point: `0,64,0`
- Recommended player entry point: `0,65,0`

With the default Premium config, the schematic is pasted at:

```text
regionX, paste-y-level, regionZ
```

If `paste-y-level` is `64`, the schematic clipboard origin should also be saved from local `0,64,0`.

The clean rule is:

```text
Stand at local 0,64,0 when running //copy.
```

This makes YAML coordinates match the map exactly. If a mob spawns at `10,64,10` in the build world, the YAML should
also use `10,64,10`.

## Why The Copy Position Matters

WorldEdit stores the clipboard relative to the position where the player ran `//copy`. SinceDungeon pastes the clipboard
at the configured paste location. Therefore, the player's `//copy` position becomes the anchor point that SinceDungeon
will align to `paste-y-level`.

Correct setup:

```text
Build world local origin: 0,64,0
Player stands at:         0,64,0
WorldEdit command:        //copy
Premium paste point:      instanceX,64,instanceZ
YAML coordinates:         same local coordinates used while building
```

Wrong setup:

```text
Player stands at: 100,70,-40
WorldEdit command: //copy
YAML coordinates: still written as if the origin was 0,64,0
Result: actions, spawn points, chests, doors, and mobs are offset incorrectly
```

If a schematic was copied from the wrong position, recopy it from the correct origin. Adjusting every YAML coordinate is
possible, but it is slower and easier to break.

## Build World Setup

Create a normal builder world for the dungeon. This world is only for designing and exporting the schematic.

Recommended layout:

- Put the intended schematic origin at `0,64,0`.
- Put the player entry area near `0,65,0`.
- Keep the whole dungeon inside a predictable radius, for example from `-200` to `200` on X/Z.
- Keep the full height inside the shared-world clear bounds, for example from `-64` to `320`.
- Avoid building unrelated structures inside the selected schematic box.
- Use signs, colored blocks, or temporary markers while building, then remove them before saving the schematic.

Example local coordinate plan:

```text
0,65,0      player entry
10,64,10    first reach objective
15,64,20    first mob spawn
20,60,-5    loot chest
0,64,-10    locked gate trigger
0,64,-50    boss room
```

## Save The Schematic With WorldEdit

Use this workflow when the dungeon build is ready.

1. Go to the builder world.
2. Select the entire dungeon with WorldEdit:

```text
//wand
```

Left-click one corner and right-click the opposite corner.

3. Stand exactly at the intended local paste origin:

```text
/tp <your_name> 0 64 0
```

4. Copy the selection:

```text
//copy
```

5. Save the schematic:

```text
//schem save ForgottenCrypt_Template
```

Depending on your WorldEdit or FAWE version, the saved file may be `.schem` or `.schematic`.

6. Move or copy the saved file into:

```text
plugins/SinceDungeon-PremiumAddon/schematics/
```

The final file should be one of:

```text
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schem
plugins/SinceDungeon-PremiumAddon/schematics/ForgottenCrypt_Template.schematic
```

## Premium Config

Enable schematic mode in:

```text
plugins/SinceDungeon-PremiumAddon/config.yml
```

Recommended Paper or Folia shared-world setup:

```yaml
instancing:
  mode: "SCHEMATIC"
  paste-y-level: 64
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

Field behavior:

- `mode`: must be `SCHEMATIC` for Premium schematic instancing.
- `paste-y-level`: Y level where the schematic clipboard origin is pasted.
- `paste-air`: when `true`, air in the schematic overwrites previous blocks.
- `shared-world.enabled`: when `true`, all dungeon runs are pasted into separated regions inside one shared world.
- `shared-world.name`: the shared void world name.
- `shared-world.spawn-location`: default local entry point if the dungeon YAML does not define `settings.start-location`.
- `coordinate-y-offset`: Y offset added to all YAML coordinates. Keep this `0` unless you intentionally need an offset.
- `region-spacing`: distance between allocated dungeon regions.
- `region-radius`: half-size owned by each run for event routing and cleanup.
- `grid-width`: number of regions per row before allocating the next Z row.
- `clear-on-release`: clears the used region when the run ends.
- `clear-min-y` and `clear-max-y`: vertical cleanup bounds.

## Region Size Rules

The schematic and all gameplay must fit inside `region-radius`.

If `region-radius` is `512`, the active dungeon area should stay within:

```text
X: -512 to 512 from the local origin
Z: -512 to 512 from the local origin
```

`region-spacing` should be larger than `region-radius * 2` with extra room between instances.

Recommended examples:

```yaml
region-spacing: 2048
region-radius: 512
```

For very large maps:

```yaml
region-spacing: 4096
region-radius: 1500
```

Do not make `region-radius` smaller than the dungeon. Events outside the radius may not route to the correct game, and
cleanup may miss blocks or entities.

## Folia Shared-World Setup

On Folia, the shared world must be loaded before SinceDungeon Premium enables.

Required behavior:

- The shared world name must match `instancing.schematic.shared-world.name`.
- The world must already exist and be loaded on server startup.
- SinceDungeon Premium will reuse the loaded world.
- SinceDungeon Premium will not create or load the shared world dynamically on Folia.

Recommended name:

```yaml
name: "SDPremium_Schematic"
```

If the console says the shared world is missing, create/preload that world with your server/world management setup, then
restart the server.

## Dungeon YAML Setup

Create the dungeon file:

```text
plugins/SinceDungeon/dungeons/forgotten_crypt.yml
```

Minimum schematic dungeon:

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
    reward_1:
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

Important:

- `template-world` must match the schematic base filename without `.schem` or `.schematic`.
- `settings.start-location` is a local dungeon coordinate.
- Stage action locations are local dungeon coordinates.
- Do not write shared-world runtime coordinates in the dungeon YAML.

## Coordinate Format

Use simple coordinate strings:

```text
x,y,z
```

Examples:

```yaml
target: "10,64,10"
location: "20,60,-5"
trigger: "0,64,-10"
corner1: "-2,64,-10"
corner2: "2,68,-10"
```

`settings.start-location` also supports yaw and pitch:

```yaml
settings:
  start-location: "0,65,0,180,0"
```

When yaw and pitch are omitted, the location still works but the player's facing direction is not customized.

## How Coordinates Are Applied At Runtime

In shared-world mode, SinceDungeon allocates a region for each run. The first slot starts at `0,0`, the next slot starts
at `region-spacing,0`, and later rows continue along Z.

Dungeon YAML stays local. SinceDungeon offsets it internally.

Example with:

```yaml
paste-y-level: 64
coordinate-y-offset: 0
region-spacing: 2048
```

If the run is assigned to region origin `2048,0,0`, then:

```text
YAML target 10,64,10 -> real location 2058,64,10
YAML chest 20,60,-5 -> real location 2068,60,-5
```

Admins should never manually calculate these runtime coordinates for dungeon YAML. Write the local coordinate from the
builder world.

## Recommended Dungeon Setup Order

Use this order for every schematic dungeon:

1. Build the dungeon in a builder world using local coordinates.
2. Decide the local origin, normally `0,64,0`.
3. Place the player entry point, normally `0,65,0`.
4. Mark all important points while building: spawns, doors, chests, triggers, boss room, checkpoints, traps.
5. Select the full dungeon with WorldEdit.
6. Stand at `0,64,0`.
7. Run `//copy`.
8. Run `//schem save <template-world>`.
9. Put the schematic in `plugins/SinceDungeon-PremiumAddon/schematics/`.
10. Create `plugins/SinceDungeon/dungeons/<dungeon-id>.yml`.
11. Set `template-world` to the schematic base file name.
12. Set `settings.start-location`.
13. Add stages and actions using local coordinates.
14. Add rewards, lives, cooldowns, entry conditions, and commands.
15. Run `/sincedungeon reload` or `/sdp reload`.
16. Test with `/dungeon join <dungeon-id>`.

## Using The In-Game Editor

Run:

```text
/dungeon editor
```

Use the editor for common setup tasks:

- Change dungeon settings.
- Add or edit stages.
- Add actions.
- Capture locations from your current position or selected block.
- Save changes back into the dungeon YAML.

For schematic dungeons, capture coordinates in the builder/template coordinate space when possible. If you capture
coordinates inside an active shared-world runtime instance, remember that those are real shared-world coordinates, not
clean local coordinates. Convert them back to local coordinates before saving, or configure from the builder world/YAML.

## Action Placement Guide

Use one objective per action when possible. This keeps debugging simple.

Common schematic patterns:

- Entrance: `REACH_LOCATION` at the first room.
- Combat room: `SPAWN_WAVE`, `RANDOM_WAVE`, or `MYTHIC_WAVE` with mob spawn points inside the room.
- Key path: `LOOT_CHEST` gives `KEY:<id>:1`, then `UNLOCK_DOOR` consumes that key.
- Door or wall: use exact block coordinates for `corner1` and `corner2`.
- Boss room: `BOSS_BATTLE` or `MYTHIC_WAVE`.
- Checkpoint: Premium `CHECKPOINT` near the room start.
- Trap room: Premium `DAMAGE_ZONE` or `PROJECTILE_TRAP`.
- NPC flow: Premium `NPC_INTERACTION` or `ESCORT`.

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

## Reload And Test

After changing config or dungeon files:

```text
/sincedungeon reload
```

If using Premium commands:

```text
/sdp reload
```

Start a test run:

```text
/dungeon join forgotten_crypt
```

Test checklist:

- The schematic file is found with no console warning.
- The dungeon pastes at the expected Y level.
- Players enter at `settings.start-location` or the shared-world `spawn-location`.
- Every reach location completes at the correct spot.
- Mobs spawn in the intended rooms.
- Chests, levers, triggers, doors, and walls use exact block positions.
- Bosses and NPCs are inside the region.
- Rewards open after the dungeon clears.
- The region clears after the run ends.
- A second run starts cleanly with no leftover mobs or blocks.

## Troubleshooting

### Schematic Not Found

Check:

- The file is inside `plugins/SinceDungeon-PremiumAddon/schematics/`.
- The file name matches `template-world`.
- The extension is `.schem` or `.schematic`.
- The server was reloaded or restarted after adding the file.

### Players Spawn In The Wrong Place

Check:

- `settings.start-location` in the dungeon YAML.
- `instancing.schematic.shared-world.spawn-location` in Premium config.
- Whether the schematic was copied while standing at the correct origin.
- Whether `paste-y-level` matches the Y level used when copying.

Recommended fix:

```text
Stand at 0,64,0 -> //copy -> //schem save <template-world>
```

### Actions Are Shifted Away From The Build

This almost always means the schematic was copied from the wrong player position.

Fix:

1. Go back to the builder world.
2. Stand at the intended local origin, normally `0,64,0`.
3. Run `//copy`.
4. Save the schematic again.
5. Replace the old schematic file.
6. Test again.

### Schematic Pastes Too High Or Too Low

Check:

```yaml
instancing:
  paste-y-level: 64
```

If the schematic was copied at `0,64,0`, keep `paste-y-level: 64`. If you intentionally use another anchor Y level,
make the copy position and paste level match your coordinate plan.

### Runtime Coordinates Are Huge

This is normal in shared-world mode. SinceDungeon allocates separated regions using `region-spacing`.

Do not copy those huge runtime coordinates into the dungeon YAML. Keep YAML coordinates local to the schematic.

### Region Does Not Clean Up Fully

Check:

- `clear-on-release: true`
- `region-radius` is large enough for the whole dungeon.
- `clear-min-y` and `clear-max-y` cover the whole vertical build.
- WorldEdit or FAWE is installed and working.

### Folia Says Shared World Is Missing

The shared world is not loaded before SinceDungeon Premium enables.

Fix:

1. Create the world named in `instancing.schematic.shared-world.name`.
2. Make sure the server loads that world on startup.
3. Restart the server.

## Final Release Checklist

Before making a schematic dungeon public:

- `template-world` matches the schematic base filename.
- The schematic was copied while standing at the intended local origin.
- `paste-y-level` matches the copy-origin Y level.
- `settings.start-location` is inside the schematic and safe.
- Every action coordinate is local to the schematic.
- The whole dungeon fits inside `region-radius`.
- Cleanup bounds include the whole dungeon height.
- Rewards have at least one valid reward pool entry.
- Conditions and required items are tested with a normal player account.
- `public: true` is set only after testing is complete.
- `/dungeon join <dungeon-id>` works multiple times in a row.
