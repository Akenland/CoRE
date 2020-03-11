package com.kylenanakdewa.core.permissions.multicheck;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.PermissionsManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * The Admin Multi-Check system. Performs additional verification to ensure that
 * only the right players can use admin commands.
 * <p>
 * Part of the CoRE Permissions system.
 *
 * @author Kyle Nanakdewa
 */
public final class AdminMultiCheck {

    /** The CoRE Permissions system that owns this multi-check system. */
    protected final PermissionsManager permissionsManager;

    /** The admins listed in the admins.yml file. */
    private Set<OfflinePlayer> admins = null;

    public AdminMultiCheck(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    /**
     * Gets the file configuration for the admins.yml file.
     */
    private FileConfiguration getAdminsFile() {
        return permissionsManager.getPermissionsConfigFile("admins.yml");
    }

    /**
     * Loads the admins that are defined in the admins.yml file.
     */
    private Set<OfflinePlayer> getListedAdmins() {
        if (admins != null) {
            return admins;
        }

        admins = new HashSet<OfflinePlayer>();
        Set<String> adminNames = new HashSet<String>();

        ConfigurationSection config = getAdminsFile().getConfigurationSection("admins");

        for (String playerName : config.getKeys(false)) {
            UUID uuid = UUID.fromString(config.getString(playerName));
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            // Make sure player exists
            if (player.getName() == null) {
                Utils.notifyAdminsError(playerName + CommonColors.INFO + " (" + player.getUniqueId() + ") "
                        + CommonColors.ERROR
                        + "is listed in Admin Multi-Check, but no player with this UUID exists. Check the admins.yml file.");
            }

            // Make sure name matches
            else if (!player.getName().equals(playerName)) {
                Utils.notifyAdminsError(playerName + CommonColors.INFO + " (" + player.getUniqueId() + ") "
                        + CommonColors.ERROR + "is listed in Admin Multi-Check, but this UUID belongs to "
                        + player.getName() + ". Check the admins.yml file.");
            }

            // Otherwise, this player is verified, add them to set
            else {
                admins.add(player);
                adminNames.add(player.getName());
            }
        }

        Utils.notifyAdmins("Admin Multi-Check loaded " + admins.size() + " admins: " + String.join(", ", adminNames));

        return admins;
    }

    /**
     * Gets a set of online listed admins.
     */
    private Set<Player> getOnlineListedAdmins() {
        Set<Player> onlineAdmins = new HashSet<Player>();

        for (OfflinePlayer player : getListedAdmins()) {
            if (player.isOnline()) {
                onlineAdmins.add(player.getPlayer());
            }
        }

        return onlineAdmins;
    }

    /**
     * Checks if a player is listed in the admins.yml file.
     */
    private boolean isListedAdmin(OfflinePlayer player) {
        // Check if offline player matches
        if (getListedAdmins().contains(player)) {
            return true;
        }

        // Check if online player matches
        if (player.isOnline() && getOnlineListedAdmins().contains(player.getPlayer())) {
            return true;
        }

        // Otherwise return false
        return false;
    }

    /**
     * Returns true if the specified CommandSender is a verified admin.
     */
    public boolean performCheck(CommandSender sender) {
        if (sender instanceof Player) {
            return performCheck((OfflinePlayer) sender);
        }
        if (sender instanceof ConsoleCommandSender) {
            return performCheck((ConsoleCommandSender) sender);
        }
        if (sender instanceof BlockCommandSender) {
            return performCheck((BlockCommandSender) sender);
        }
        if (sender instanceof RemoteConsoleCommandSender) {
            return performCheck((RemoteConsoleCommandSender) sender);
        }

        Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
                + " failed security check (Admin Multi-Check failed: unknown command sender type).");
        return false;
    }

    /**
     * Returns true if the specified player is a verified admin.
     */
    private boolean performCheck(OfflinePlayer player) {
        // Make sure player is online
        if (!player.isOnline()) {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR
                    + " could not be verified by Admin Multi-Check, as they are offline.");
            return false;
        }

        // Make sure player has core.admin permission
        if (!player.getPlayer().hasPermission("core.admin")) {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR
                    + " failed security check (Admin Multi-Check failed: missing core.admin permission).");
            return false;
        }

        // Make sure player is listed in admins.yml file
        if (!isListedAdmin(player)) {
            Utils.notifyAdminsError(player.getName() + CommonColors.ERROR
                    + " failed security check (Admin Multi-Check failed: missing from admins.yml file).");
            return false;
        }

        // If all checks pass, return true
        return true;
    }

    /**
     * Returns true if the console is a verified admin.
     */
    private boolean performCheck(ConsoleCommandSender console) {
        // Make sure config allows console sender
        if (!CoreConfig.allowConsoleCommands) {
            Utils.notifyAdminsError(
                    console.getName() + " failed security check (Admin Multi-Check failed: console is blocked).");
            return false;
        }

        // If all checks pass, return true
        return true;
    }

    /**
     * Returns true if the specified command block is a verified admin.
     */
    private boolean performCheck(BlockCommandSender commandBlockSender) {
        Block block = ((BlockCommandSender) commandBlockSender).getBlock();
        String blockName = commandBlockSender.getName() + CommonColors.INFO + "(" + block.getType() + " at "
                + block.getX() + " " + block.getY() + " " + block.getZ() + ")";
        String command = ((CommandBlock) block.getState()).getCommand();

        // Make sure config allows command blocks
        if (!CoreConfig.allowCommandBlocksCommands) {
            Utils.notifyAdminsError(blockName + CommonColors.ERROR
                    + " failed security check (Admin Multi-Check failed: command blocks are blocked). "
                    + CommonColors.INFO + "Command: " + command);
            return false;
        }

        // If all checks pass, return true
        Utils.notifyAdmins(CommonColors.INFO + blockName + CommonColors.INFO + " used an admin command: " + command);
        return true;
    }

    /**
     * Returns true if the remote console is a verified admin.
     */
    private boolean performCheck(RemoteConsoleCommandSender console) {
        // Make sure config allows remote console sender
        if (!CoreConfig.allowRconCommands) {
            Utils.notifyAdminsError(console.getName()
                    + " failed security check (Admin Multi-Check failed: remote console is blocked).");
            return false;
        }

        // If all checks pass, return true
        Utils.notifyAdmins(CommonColors.INFO + console.getName() + CommonColors.INFO + " used an admin command.");
        return true;
    }

}