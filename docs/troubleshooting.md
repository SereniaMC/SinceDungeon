---
layout: page
title: Troubleshooting
---

## Server Freezes After Provider Overwrite Logs

Symptoms:

```text
[SinceDungeon] [API] Reward System overwritten by: DefaultRewardSystem
[SinceDungeon] [API] Party System overwritten by: DefaultPartyProvider
[SinceDungeon] [API] Instancing System overwritten by: DefaultInstanceProvider
```

Cause:

- Startup was blocked while waiting for async template loading.

Fix:

- Use a build that loads database, cooldowns, lives, and templates asynchronously.
- Check logs for `[Startup] SinceDungeon finished loading data.`
- Increase `startup.async-timeout-seconds` if needed.

## Dungeon Data Is Still Loading

Message:

```text
Dungeon data is still loading. Please try again in a moment.
```

Cause:

- A player tried to join before startup data finished loading.

Fix:

- Wait for the startup ready log.
- Check database connectivity.
- Check template YAML errors.

## Dungeon Does Not Appear in Tab Completion

Check:

- The dungeon file exists in `plugins/SinceDungeon/dungeons/`.
- The filename is `<id>.yml`.
- `public: true` for regular members. Admins with `SinceDungeon.admin` can tab-complete private dungeons too.
- `template-world` is present.
- The server has been restarted or the plugin files have been reloaded.

## Member Cannot Join a Dungeon

If the dungeon has `public: false`, this is expected. Private dungeons are blocked for regular members and are intended
for admin testing or hidden content.

## Dungeon Fails to Start

Check:

- The template world folder exists.
- The player has enough lives.
- Entry conditions pass.
- The dungeon is not on cooldown.
- `max-players` is not exceeded.
- Required item settings are valid.

## Mythic Actions Do Not Spawn Mobs

Check:

- MythicMobs is installed and enabled.
- The internal MythicMob name is correct.
- The location chunk can load.
- The mob level is valid.

## Rewards Are Missing

Check:

- `rewards.solo-tiers` or `rewards.party-tiers` is configured.
- Completion time fits a tier.
- `rewards.pool` contains valid entries.
- Reward `type` matches a registered processor.
- MMOItems or MythicMobs item dependencies are installed if needed.

## Players Lose Items

Check:

- `save-and-restore-stats`.
- `keep-inventory-on-death`.
- Multiverse Inventories bypass permission settings.
- Whether inventory overflow drops at the saved location.

## Commands Are Blocked Inside Dungeons

Check:

```yaml
dungeon:
  gameplay:
    block-commands: true
    allowed-commands:
      - "/party"
      - "/dungeon"
```

Add any command that should remain usable during dungeon runs.

## Cross-Server Mode Does Not Work

Cross-server mode is experimental. Check:

- `cross-server.enabled: true`.
- Redis host, port, password, and channel match on all servers.
- Proxy server names match the proxy config.
- `bungee-channel` matches your proxy type.
- Plugin messaging is available.

## Holograms Do Not Appear, Premium

Check:

- Premium is enabled after Core.
- The hologram was created with `/sdp hologram create`.
- The configured category is valid.
- Database contains leaderboard entries.
- The hologram world is loaded and `hologram-leaderboard.view-range` is high enough.

## Folia Dungeon Instances Fail to Create

Folia cannot create or load Bukkit worlds at runtime. Use Premium `SCHEMATIC` mode and preload the configured shared
world before the plugin starts. Core validates template-world copy dungeons during load, editor save, and join;
unsupported Folia setups are blocked before the plugin tries to create a runtime world.

## Schematic Dungeon Is Empty

Check:

- WorldEdit or FastAsyncWorldEdit is installed.
- `instancing.mode: "SCHEMATIC"`.
- The schematic file exists in `plugins/SinceDungeon-PremiumAddon/schematics/`.
- The schematic filename matches the dungeon `template-world`.
- On Folia, the shared world is already loaded.

## Premium Schematic Mode Does Not Enable

Check:

- Premium is installed.
- Core is installed.
- WorldEdit or FastAsyncWorldEdit is installed.
- `instancing.mode` is `SCHEMATIC`.
- On Folia, the shared world is already loaded.

## Placeholder Conditions Always Fail

Check:

- PlaceholderAPI is installed.
- The placeholder returns a value.
- Condition syntax is correct.

Example:

```yaml
check: "%player_level%;>=;10"
```

## MMOItems Rewards Fail

Check:

- MMOItems is installed.
- Item type and ID are correct.
- Use the correct format:

```text
MMOITEMS:<type>:<id>:<amount>
```

or reward:

```yaml
type: "MMOITEM"
value: "SWORD:SILVER_LANCE:1"
```

## Database Fails To Initialize

Check:

- SQLite file permissions.
- MySQL host, port, database, username, and password.
- Hikari pool timeout.
- Server firewall.

If database startup fails, the plugin disables itself instead of running half-loaded.

## GUI Editor Input Does Nothing

Check:

- Player has `SinceDungeon.admin`.
- Player is not typing commands while in input mode.
- Type `cancel` to leave input mode.

## Advanced YAML Path Is Not Added

Use:

```text
path=value
```

Example:

```text
settings.max-players=8
```

Do not type only the path.
