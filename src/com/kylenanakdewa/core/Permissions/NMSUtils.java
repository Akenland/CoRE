package com.kylenanakdewa.core.permissions;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Utility methods that use net.minecraft.server code.
 *
 * Currently supports 1.16.
 *
 * @author Kyle Nanakdewa
 */
public class NMSUtils {

    /**
     * Sends a packet to the specified player to fix tab-completions for commands.
     * <p>
     * After a player's permissions changes, the list of commands they can
     * tab-complete is not updated immediately. This method sends a packet to the
     * client to force update the tab-completions.
     * <p>
     * See
     * https://www.spigotmc.org/threads/giving-player-permission-doesnt-make-command-autocomplete.450694/#post-3967904
     *
     * @param player the player to send a packet to
     */
    public static void fixCommandTabCompletions(Player player) {
        ((CraftServer) Bukkit.getServer()).getHandle().getServer().getCommandDispatcher()
                .a((((CraftPlayer) player).getHandle()));

    }

}