package net.danh.sinceDungeon.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * Hook for Multiverse-Inventories to dynamically manage inventory sharing for dungeon worlds.
 * Supports both v4 and v5 of Multiverse-Inventories without reflection and safely separates
 * classes to avoid NoClassDefFoundError.
 */
public class MultiverseInventoriesHook {

    public static boolean isEnabled() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
        return plugin != null && plugin.isEnabled();
    }

    private static boolean isV5() {
        try {
            Class.forName("org.mvplugins.multiverse.inventories.MultiverseInventoriesApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void addWorldToGroupsOf(String originWorldName, String dungeonWorldName) {
        if (!isEnabled()) return;
        try {
            if (isV5()) {
                MultiverseInventoriesHookV5.addWorldToGroupsOf(originWorldName, dungeonWorldName);
            } else {
                MultiverseInventoriesHookV4.addWorldToGroupsOf(originWorldName, dungeonWorldName);
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[SinceDungeon] Failed to dynamically integrate with Multiverse-Inventories: " + e.getMessage(), e);
        }
    }

    public static void removeWorldFromGroups(String dungeonWorldName) {
        if (!isEnabled()) return;
        try {
            if (isV5()) {
                MultiverseInventoriesHookV5.removeWorldFromGroups(dungeonWorldName);
            } else {
                MultiverseInventoriesHookV4.removeWorldFromGroups(dungeonWorldName);
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[SinceDungeon] Failed to clean up dynamic world in Multiverse-Inventories: " + e.getMessage(), e);
        }
    }

    public static void removeLeftoverWorldsFromGroups(String prefix) {
        if (!isEnabled()) return;
        try {
            if (isV5()) {
                MultiverseInventoriesHookV5.removeLeftoverWorldsFromGroups(prefix);
            } else {
                MultiverseInventoriesHookV4.removeLeftoverWorldsFromGroups(prefix);
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
