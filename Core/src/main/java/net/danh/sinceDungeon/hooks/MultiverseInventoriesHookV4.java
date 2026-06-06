package net.danh.sinceDungeon.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class MultiverseInventoriesHookV4 {

    public static void addWorldToGroupsOf(String originWorldName, String dungeonWorldName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
        if (!(plugin instanceof com.onarandombox.multiverseinventories.MultiverseInventories)) return;
        com.onarandombox.multiverseinventories.MultiverseInventories mvi = (com.onarandombox.multiverseinventories.MultiverseInventories) plugin;
        com.onarandombox.multiverseinventories.profile.WorldGroupManager groupManager = mvi.getGroupManager();
        if (groupManager == null) return;
        List<com.onarandombox.multiverseinventories.WorldGroup> groups = groupManager.getGroups();
        if (groups == null) return;
        for (com.onarandombox.multiverseinventories.WorldGroup group : groups) {
            if (group != null && group.containsWorld(originWorldName)) {
                if (!group.containsWorld(dungeonWorldName)) {
                    group.addWorld(dungeonWorldName);
                    groupManager.updateGroup(group);
                    Bukkit.getLogger().info("[SinceDungeon] Added dynamic dungeon world '" + dungeonWorldName + "' to Multiverse-Inventories (v4) group '" + group.getName() + "'.");
                }
            }
        }
    }

    public static void removeWorldFromGroups(String dungeonWorldName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
        if (!(plugin instanceof com.onarandombox.multiverseinventories.MultiverseInventories)) return;
        com.onarandombox.multiverseinventories.MultiverseInventories mvi = (com.onarandombox.multiverseinventories.MultiverseInventories) plugin;
        com.onarandombox.multiverseinventories.profile.WorldGroupManager groupManager = mvi.getGroupManager();
        if (groupManager == null) return;
        List<com.onarandombox.multiverseinventories.WorldGroup> groups = groupManager.getGroups();
        if (groups == null) return;
        for (com.onarandombox.multiverseinventories.WorldGroup group : groups) {
            if (group != null && group.containsWorld(dungeonWorldName)) {
                group.removeWorld(dungeonWorldName);
                groupManager.updateGroup(group);
                Bukkit.getLogger().info("[SinceDungeon] Removed dynamic dungeon world '" + dungeonWorldName + "' from Multiverse-Inventories (v4) group '" + group.getName() + "'.");
            }
        }
    }

    public static void removeLeftoverWorldsFromGroups(String prefix) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
        if (!(plugin instanceof com.onarandombox.multiverseinventories.MultiverseInventories)) return;
        com.onarandombox.multiverseinventories.MultiverseInventories mvi = (com.onarandombox.multiverseinventories.MultiverseInventories) plugin;
        com.onarandombox.multiverseinventories.profile.WorldGroupManager groupManager = mvi.getGroupManager();
        if (groupManager == null) return;
        List<com.onarandombox.multiverseinventories.WorldGroup> groups = groupManager.getGroups();
        if (groups == null) return;
        for (com.onarandombox.multiverseinventories.WorldGroup group : groups) {
            if (group == null) continue;
            java.util.Set<String> worlds = group.getWorlds();
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
                Bukkit.getLogger().info("[SinceDungeon] Cleaned up leftover dungeon worlds " + worldsToRemove + " from Multiverse-Inventories (v4) group '" + group.getName() + "'.");
            }
        }
    }
}
