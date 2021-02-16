package com.kylenanakdewa.core.permissions;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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

        // Assign permissions on join
        player.setCurrentSetAutomatic();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Revoke permissions
        permissionsManager.getPlayer(event.getPlayer()).revokePermissions(true);

        // Clean out old players
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> permissionsManager.cleanPlayers());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // If player is opped, perform Admin Multi-Check
        if (event.getPlayer().isOp()) {
            // If Admin Multi-Check fails, cancel command, and de-op player
            if (!permissionsManager.performAdminMultiCheck(event.getPlayer())) {
                Utils.notifyAdminsError(event.getPlayer().getName() + CommonColors.ERROR
                        + " failed security check (Admin Multi-Check failed while player was op). CoRE has de-opped this player to protect your server. "
                        + CommonColors.INFO + "Command: " + event.getMessage());
                event.setCancelled(true);
                event.getPlayer().setOp(false);
            }
        }
    }

}