package com.kylenanakdewa.core.permissions;

import com.kylenanakdewa.core.CorePlugin;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener to events for the CoRE Permissions system.
 * <p>
 * Will listen for player joins and quits.
 *
 * @author Kyle Nanakdewa
 */
final class PermissionsListener implements Listener {

    /** The CoRE Permissions system that owns this listener. */
    private final PermissionsManager permissionsManager;

    /** The CoRE plugin that owns this listener. */
    private final CorePlugin plugin;

    PermissionsListener(PermissionsManager permissionsManager, CorePlugin plugin) {
        this.permissionsManager = permissionsManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerPermissionsHolder player = permissionsManager.getPlayer(event.getPlayer());

        player.setCurrentSetAutomatic();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Revoke permissions
        permissionsManager.getPlayer(event.getPlayer()).revokePermissions();

        // Clean out old players
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> permissionsManager.cleanPlayers());
    }

}