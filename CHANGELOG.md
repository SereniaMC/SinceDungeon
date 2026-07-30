# Changelog

## Unreleased

- Added proactive Folia validation for Core template-world/world-copy dungeons during template load, join, and editor
  save, with guidance that Premium `SCHEMATIC` shared-world mode works on Paper and Folia.
- Documented outcome-based life costs (`lives-deducted-on-leave`, `lives-deducted-on-fail`,
  `lives-deducted-on-clear`) and clarified their interaction with per-death costs and cooldown-on-leave.
- Updated Premium hologram docs for native TextDisplay leaderboard holograms and added default line spacing/view range
  config keys.

### Fixed

- Fixed a critical bug in Paper 1.20+ where joining a dungeon would spawn players in a randomly generated world instead of the dungeon template (caused by incorrect instance folder targeting).
- Enforced private dungeon visibility: regular members cannot join `public: false` dungeons, while admins can
  tab-complete and join them for testing.
- Routed command rewards through `SchedulerCompat` so reward command execution no longer calls the Bukkit scheduler
  directly.
- Replaced raw `printStackTrace()` calls with contextual plugin logger output.
- Closed default config resource streams after auto-update checks.
- Cleared reward session cleanup task references during shutdown to avoid stale static task handles.
- Added missing Premium hologram message keys used by `HologramManager`.
- Hardened Premium hologram updates by snapshotting config on the server thread, fetching leaderboard data
  asynchronously, and rendering holograms on the owning location scheduler.

### Added

- Added `settings.regenerate-default-templates` option in `config.yml` (default `false`). Setting this to false stops the plugin from automatically re-creating default template files (e.g. `example_dungeon.yml`) when server owners delete them.
- Added API overload `joinDungeon(Player, String, boolean)` for controlled private dungeon joins by integrations.
- Documented Premium schematic shared-world setup for Paper and Folia.
- Documented Premium `NPC_INTERACTION` action.
- Added GitHub Pages wiki under `docs/` with deployment workflow.

### Notes

- Premium `SCHEMATIC` shared-world mode works on Paper and Folia. Folia cannot create or load Bukkit worlds at runtime,
  so Folia deployments must preload the configured shared world before plugin startup.
