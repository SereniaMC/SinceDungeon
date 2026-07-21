package net.danh.sinceDungeon.actions.impl;

import net.danh.sinceDungeon.SinceDungeon;
import net.danh.sinceDungeon.actions.DungeonAction;
import net.danh.sinceDungeon.actions.Tickable;
import net.danh.sinceDungeon.hooks.MythicMobsHook;
import net.danh.sinceDungeon.models.DungeonGame;
import net.danh.sinceDungeon.utils.ItemBuilder;
import net.danh.sinceDungeon.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class RandomWaveAction extends DungeonAction implements Tickable {

    private final int amount;
    private final List<Vector> locations;
    private final List<MobOption> mobPool;
    private final boolean scaleWithParty;
    private final List<String> customDrops;
    private final int spawnDelay;
    private final double spawnRadius;

    private final Map<UUID, Entity> spawnedMobs = new HashMap<>();
    private final Map<UUID, String> mobDisplayNames = new HashMap<>();
    private final Deque<PendingSpawn> pendingSpawns = new ArrayDeque<>();
    private int tickCounter = 0;
    private int nextSpawnAt = 0;

    public RandomWaveAction(int amount, List<Vector> locations, List<MobOption> mobPool, boolean scaleWithParty, List<String> customDrops) {
        this(amount, locations, mobPool, scaleWithParty, customDrops, 0, 0.0);
    }

    public RandomWaveAction(int amount, List<Vector> locations, List<MobOption> mobPool, boolean scaleWithParty, List<String> customDrops, int spawnDelay, double spawnRadius) {
        this.amount = amount;
        this.locations = locations;
        this.mobPool = mobPool;
        this.scaleWithParty = scaleWithParty;
        this.customDrops = customDrops;
        this.spawnDelay = Math.max(0, spawnDelay);
        this.spawnRadius = Math.max(0.0, spawnRadius);
    }

    public static List<MobOption> parseMobPool(List<String> raw) {
        List<MobOption> pool = new ArrayList<>();
        for (String entry : raw) {
            try {
                String[] parts = entry.replace(" ", "").split(":");
                if (parts.length < 3) continue;
                boolean isMythic = parts[0].equalsIgnoreCase("MYTHIC");
                String id = parts[1];
                double weight = Double.parseDouble(parts[2]);
                int level = parts.length >= 4 ? Integer.parseInt(parts[3]) : 1;
                if (weight > 0) pool.add(new MobOption(isMythic, id, level, weight));
            } catch (Exception ignored) {
                String warnMsg = SinceDungeon.getPlugin().getLanguageManager().getString(
                        "admin.log.random_wave_parse_error",
                        "[RandomWave] Failed to parse mob pool entry: <entry> — expected format: VANILLA:<EntityType>:<weight> or MYTHIC:<MobId>:<weight>[:<level>]"
                );
                SinceDungeon.getPlugin().getLogger().warning(warnMsg.replace("<entry>", entry));
            }
        }
        return pool;
    }

    @Override
    public void trackChildEntity(UUID uuid, Location loc, String internalName) {
        super.trackChildEntity(uuid, loc, internalName);
        Entity ent = Bukkit.getEntity(uuid);
        if (ent != null) spawnedMobs.put(uuid, ent);
        if (internalName != null) mobDisplayNames.put(uuid, internalName);
    }

    private MobOption pickRandom() {
        if (mobPool.isEmpty()) return null;
        double totalWeight = mobPool.stream().mapToDouble(MobOption::weight).sum();
        double roll = Math.random() * totalWeight;
        double cumulative = 0;
        for (MobOption opt : mobPool) {
            cumulative += opt.weight();
            if (roll <= cumulative) return opt;
        }
        return mobPool.get(mobPool.size() - 1);
    }

    private Location findSafeSpawn(Location original) {
        Location check = original.clone();
        for (int i = 0; i < 5; i++) {
            if (check.getBlock().getType().isSolid()) {
                check.add(0, 1, 0);
                break;
            }
            check.subtract(0, 1, 0);
        }
        for (int i = 0; i < 5; i++) {
            Block block = check.getBlock();
            Block head = check.clone().add(0, 1, 0).getBlock();
            if (!block.getType().isSolid() && !head.getType().isSolid()) return check;
            check.add(0, 1, 0);
        }
        return original;
    }

    private Location randomSpawnLoc(DungeonGame game, Vector vec) {
        double offsetX, offsetZ;
        if (spawnRadius > 0) {
            double angle = Math.random() * 2 * Math.PI;
            double dist = Math.random() * spawnRadius;
            offsetX = dist * Math.cos(angle);
            offsetZ = dist * Math.sin(angle);
        } else {
            offsetX = (Math.random() - 0.5) * 1.5;
            offsetZ = (Math.random() - 0.5) * 1.5;
        }
        return findSafeSpawn(game.resolveLocation(vec, 0.5 + offsetX, 0, 0.5 + offsetZ));
    }

    private Entity spawnMob(DungeonGame game, Location loc, MobOption opt) {
        try {
            String pName = SinceDungeon.getPlugin().getConfigFile().getString("particles.mob_spawn", "CAMPFIRE_COSY_SMOKE");
            Particle pType;
            try {
                pType = Particle.valueOf(pName.toUpperCase());
            } catch (Exception e) {
                pType = Particle.CAMPFIRE_COSY_SMOKE;
            }

            String sName = SinceDungeon.getPlugin().getConfigFile().getString("sounds.mob_spawn", "entity.zombie.break_wooden_door");
            Sound sType = SoundUtils.getSound(sName);

            if (opt.isMythic()) {
                if (!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) return null;
                if (!MythicMobsHook.isValidMythicMob(opt.id())) return null;

                Entity e = MythicMobsHook.spawnMythicMob(loc, opt.id(), opt.level());
                if (e instanceof LivingEntity le) {
                    le.setRemoveWhenFarAway(false);
                    le.setPersistent(true);

                    game.getWorld().spawnParticle(pType, loc.clone().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);
                    if (sType != null) game.getWorld().playSound(loc, sType, 0.5f, 0.8f);

                    return e;
                }
            } else {
                EntityType type;
                try {
                    type = EntityType.valueOf(opt.id().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return null;
                }
                loc.getChunk().load(true);
                Entity e = game.getWorld().spawnEntity(loc, type);
                if (e instanceof LivingEntity le) {
                    le.setRemoveWhenFarAway(false);
                    le.setPersistent(true);

                    game.getWorld().spawnParticle(pType, loc.clone().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);
                    if (sType != null) game.getWorld().playSound(loc, sType, 0.5f, 0.8f);

                    return e;
                } else if (e != null) {
                    e.remove();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void doSpawn(DungeonGame game, PendingSpawn ps) {
        Entity ent = spawnMob(game, ps.loc(), ps.opt());
        if (ent != null) {
            UUID uid = ent.getUniqueId();
            spawnedMobs.put(uid, ent);
            mobDisplayNames.put(uid, ps.opt().id());
            this.spawnedEntities.add(uid);
            this.activeEntities.add(ent);
        }
    }

    @Override
    public String getObjectiveText() {
        String base = SinceDungeon.getPlugin().getLanguageManager().getString("objective.random_wave", "<red>Eliminate all enemies <gray>(Remaining: <remain>)");
        return base.replace("<remain>", String.valueOf(spawnedMobs.size() + pendingSpawns.size()));
    }

    @Override
    public void start(DungeonGame game) {
        if (mobPool.isEmpty() || locations.isEmpty()) {
            this.completed = true;
            return;
        }

        int finalAmount = scaleWithParty ? this.amount * game.getParticipants().size() : this.amount;
        if (finalAmount <= 0) finalAmount = 1;

        for (Vector vec : locations) {
            for (int i = 0; i < finalAmount; i++) {
                MobOption opt = pickRandom();
                if (opt == null) continue;
                pendingSpawns.add(new PendingSpawn(randomSpawnLoc(game, vec), opt));
            }
        }

        if (pendingSpawns.isEmpty()) {
            this.completed = true;
            return;
        }

        int totalCount = pendingSpawns.size();

        if (spawnDelay <= 0) {
            int count = 0;
            while (!pendingSpawns.isEmpty()) {
                PendingSpawn ps = pendingSpawns.poll();
                Entity ent = spawnMob(game, ps.loc(), ps.opt());
                if (ent != null) {
                    UUID uid = ent.getUniqueId();
                    spawnedMobs.put(uid, ent);
                    mobDisplayNames.put(uid, ps.opt().id());
                    this.spawnedEntities.add(uid);
                    this.activeEntities.add(ent);
                    count++;
                }
            }
            if (count == 0) {
                this.completed = true;
            } else {
                game.sendActionMessage(this, "init", "action.random_wave_start", "<amount>", String.valueOf(count));
            }
        } else {
            game.sendActionMessage(this, "init", "action.random_wave_start", "<amount>", String.valueOf(totalCount));
        }
    }

    @Override
    public void onTick(DungeonGame game) {
        if (completed) return;

        if (!pendingSpawns.isEmpty() && tickCounter >= nextSpawnAt) {
            doSpawn(game, pendingSpawns.poll());
            nextSpawnAt = tickCounter + spawnDelay;
        }
        tickCounter++;

        spawnedMobs.entrySet().removeIf(entry -> {
            Entity ent = entry.getValue();
            if (ent.isDead()) return true;
            if (!ent.isValid()) {
                Location lastLoc = ent.getLocation();
                if (lastLoc.getWorld() != null && !lastLoc.isChunkLoaded()) {
                    return false;
                }
                return true;
            }
            return false;
        });

        if (spawnedMobs.isEmpty() && pendingSpawns.isEmpty()) {
            this.completed = true;
            game.sendActionMessage(this, "complete", "action.random_wave_complete");
        }
    }

    @Override
    public void onEvent(DungeonGame game, Event event) {
        if (event instanceof EntityDeathEvent e) {
            UUID uid = e.getEntity().getUniqueId();
            if (spawnedMobs.remove(uid) != null) {
                handleCustomDrops(e.getEntity().getLocation());
                mobDisplayNames.remove(uid);
                int remaining = spawnedMobs.size() + pendingSpawns.size();
                if (remaining == 0) {
                    this.completed = true;
                    game.sendActionMessage(this, "complete", "action.random_wave_complete");
                } else {
                    game.sendActionMessage(this, "progress", "action.random_wave_remain", "<amount>", String.valueOf(remaining));
                }
            }
        }
    }

    @Override
    public void cleanup(DungeonGame game) {
        super.cleanup(game);
        spawnedMobs.clear();
        mobDisplayNames.clear();
        pendingSpawns.clear();
    }

    private void handleCustomDrops(Location loc) {
        if (customDrops == null || customDrops.isEmpty()) return;
        for (String dropStr : customDrops) {
            try {
                String[] split = dropStr.split(";");
                if (split.length < 2) continue;
                String itemData = split[0].trim();
                double chance = Double.parseDouble(split[1].trim());

                if (Math.random() * 100.0 <= chance) {
                    ItemStack item = ItemBuilder.parseDynamicItem(itemData);
                    if (item != null) loc.getWorld().dropItemNaturally(loc, item);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private record PendingSpawn(Location loc, MobOption opt) {
    }

    public record MobOption(boolean isMythic, String id, int level, double weight) {
    }
}
