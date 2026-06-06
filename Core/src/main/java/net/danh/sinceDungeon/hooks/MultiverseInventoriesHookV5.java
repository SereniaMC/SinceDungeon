package net.danh.sinceDungeon.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiverseInventoriesHookV5 {

    public static void addWorldToGroupsOf(String originWorldName, String dungeonWorldName) {
        org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager groupManager = org.mvplugins.multiverse.inventories.MultiverseInventoriesApi.get().getWorldGroupManager();
        if (groupManager == null) return;
        List<org.mvplugins.multiverse.inventories.profile.group.WorldGroup> groups = groupManager.getGroups();
        if (groups == null) return;
        for (org.mvplugins.multiverse.inventories.profile.group.WorldGroup group : groups) {
            if (group != null && group.containsWorld(originWorldName)) {
                if (!group.containsWorld(dungeonWorldName)) {
                    group.addWorld(dungeonWorldName);
                    groupManager.updateGroup(group);
                    Bukkit.getLogger().info("[SinceDungeon] Added dynamic dungeon world '" + dungeonWorldName + "' to Multiverse-Inventories (v5) group '" + group.getName() + "'.");
                }
            }
        }
    }

    public static void removeWorldFromGroups(String dungeonWorldName) {
        org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager groupManager = org.mvplugins.multiverse.inventories.MultiverseInventoriesApi.get().getWorldGroupManager();
        if (groupManager == null) return;
        List<org.mvplugins.multiverse.inventories.profile.group.WorldGroup> groups = groupManager.getGroups();
        if (groups == null) return;
        for (org.mvplugins.multiverse.inventories.profile.group.WorldGroup group : groups) {
            if (group != null && group.containsWorld(dungeonWorldName)) {
                group.removeWorld(dungeonWorldName);
                groupManager.updateGroup(group);
                Bukkit.getLogger().info("[SinceDungeon] Removed dynamic dungeon world '" + dungeonWorldName + "' from Multiverse-Inventories (v5) group '" + group.getName() + "'.");
            }
        }
    }

    public static void removeLeftoverWorldsFromGroups(String prefix) {
        org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager groupManager = org.mvplugins.multiverse.inventories.MultiverseInventoriesApi.get().getWorldGroupManager();
        if (groupManager == null) return;
        List<org.mvplugins.multiverse.inventories.profile.group.WorldGroup> groups = groupManager.getGroups();
        if (groups == null) return;
        for (org.mvplugins.multiverse.inventories.profile.group.WorldGroup group : groups) {
            if (group == null) continue;
            Collection<String> worlds = group.getApplicableWorlds();
            if (worlds == null) continue;
            List<String> worldsToRemove = new ArrayList<>();
            for (String wName : worlds) {
                if (wName.startsWith("SinceDungeon_") || wName.startsWith(prefix)) {
                    worldsToRemove.add(wName);
                }
            }
            if (!worldsToRemove.isEmpty()) {
                for (String wName : worldsToRemove) {
                    group.removeWorld(wName);
                }
                groupManager.updateGroup(group);
                Bukkit.getLogger().info("[SinceDungeon] Cleaned up leftover dungeon worlds " + worldsToRemove + " from Multiverse-Inventories (v5) group '" + group.getName() + "'.");
            }
        }
    }
}
