package com.kylenanakdewa.core.permissions;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

final class PermissionsCommands implements TabExecutor {

	/** The CoRE Permissions system that owns this command executor. */
	private final PermissionsManager permissionsManager;

	PermissionsCommands(PermissionsManager permissionsManager) {
		this.permissionsManager = permissionsManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// If permissions system is disabled, return an error
		if (!CoreConfig.permsEnabled) {
			if (!sender.hasPermission("core.admin"))
				return Error.NO_PERMISSION.displayChat(sender);
			sender.sendMessage(CommonColors.ERROR
					+ "CoRE Permissions are not enabled on this server. Use another plugin to manage permissions, or enable permissions in the config. See http://plugins.akenland.com for help.");
			return true;
		}

		//// First check if it's a alias for set switching
		//// (default/utility/cheat/switchset)
		if (sender instanceof Player && sender.hasPermission("core.permissions.setswitch")) {
			if (label.equalsIgnoreCase("default"))
				return new PlayerPerms((Player) sender).applyDefaultSet();
			if (label.equalsIgnoreCase("utility"))
				return new PlayerPerms((Player) sender).applyUtilitySet();
			if (label.equalsIgnoreCase("cheat"))
				return new PlayerPerms((Player) sender).applyCheatSet();
			if (label.toLowerCase().contains("set") && args.length == 1)
				return new PlayerPerms((Player) sender).switchSet(permissionsManager.getPermissionSet(args[0]));
		}

		// All perms commands require at least one arg, so return invalid args error if
		// there aren't any
		if (args.length == 0)
			return Error.INVALID_ARGS.displayActionBar(sender);

		//// Manage a set
		if (args[0].equalsIgnoreCase("set")) {

			// If second arg is info, show info about the set
			if (args.length == 3 && args[1].equalsIgnoreCase("info")) {
				if (!sender.hasPermission("core.permissions.set"))
					return Error.NO_PERMISSION.displayChat(sender);

				permissionsManager.getPermissionSet(args[2]).getInfoPrompt().display(sender);
				return true;
			}

			// If two args, attempt to switch sending player to that set
			if (args.length == 2 && sender instanceof Player && sender.hasPermission("core.permissions.setswitch")) {
				return new PlayerPerms((Player) sender).switchSet(permissionsManager.getPermissionSet(args[1]));
			}

			// If three args, attempt to switch another player to a set
			if (args.length == 3 && sender.hasPermission("core.permissions.setswitch.others")) {
				// Double-check admin status
				if (!PermsUtils.isDoubleCheckedAdmin(sender)) {
					Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
							+ " failed security check (command: /permissions " + String.join(" ", args) + ").");
					return Error.NO_PERMISSION.displayChat(sender);
				}

				// Make sure target player exists
				Player targetPlayer = Utils.getPlayer(args[1]);
				if (targetPlayer == null)
					return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
				if (targetPlayer.equals(sender)) {
					sender.sendMessage(CommonColors.ERROR
							+ "You must use this command to switch your own permissions: /set <set name>");
					return Error.INVALID_ARGS.displayActionBar(sender);
				}

				return new PlayerPerms(targetPlayer).switchSet(permissionsManager.getPermissionSet(args[2]), sender);
			}
		}

		//// Manage a player
		if (args.length >= 2 && args[0].equalsIgnoreCase("player")) {

			// These commands require double-checked admin status
			if (!PermsUtils.isDoubleCheckedAdmin(sender)) {
				Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
						+ " failed security check (command: /permissions " + String.join(" ", args) + ").");
				return Error.NO_PERMISSION.displayChat(sender);
			}

			// Make sure second arg is a player
			Player targetPlayer = Utils.getPlayer(args[1]);
			if (targetPlayer == null) {
				return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
			}

			// If third arg is set, switch player's set
			if (args.length == 4 && args[2].equalsIgnoreCase("set")) {
				// Check permissions
				if (!sender.hasPermission("core.permissions.setswitch.others")) {
					Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR + " failed security check (switching "
							+ targetPlayer.getName() + " to " + args[3] + " permissions).");
					return Error.NO_PERMISSION.displayChat(sender);
				}
				if (targetPlayer.equals(sender)) {
					sender.sendMessage(CommonColors.ERROR
							+ "You must use this command to switch your own permissions: /set <set name>");
					return Error.INVALID_ARGS.displayActionBar(sender);
				}

				return new PlayerPerms(targetPlayer).switchSet(permissionsManager.getPermissionSet(args[3]), sender);
			}

			// If third arg is revoke, revoke player's permissions
			if (args.length == 3 && args[2].equalsIgnoreCase("revoke")) {
				// Check permissions
				if (!sender.hasPermission("core.permissions.revoke")) {
					Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
							+ " failed security check (revoking permissions from " + targetPlayer.getName() + ").");
					return Error.NO_PERMISSION.displayChat(sender);
				}

				return new PlayerPerms(targetPlayer).revokePermissions();
			}

			// Otherwise, show info on the player
			if (sender.hasPermission("core.permissions.player")) {
				return new PlayerPerms(targetPlayer).displayPermissionInfo(sender);
			}

			return Error.NO_PERMISSION.displayChat(sender);
		}

		return Error.INVALID_ARGS.displayActionBar(sender);
	}

	//// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		//// Main command (no complete args)
		if (args.length == 1) {
			return Arrays.asList("set", "player");
		}

		//// Set command
		if (args.length == 2 && args[0].equalsIgnoreCase("set")) {

			// Info command, list available sets
			if (args.length == 3 && args[1].equalsIgnoreCase("info")) {
				List<String> completions = new ArrayList<String>();

				// List of permission sets this player might switch to
				for (PermissionSet set : new PlayerPerms((Player) sender).getAllSets()) {
					completions.add(set.getName());
				}

				return completions;
			}

			// Changing another player's set, list that player's sets
			if (args.length == 3 && sender.hasPermission("core.permissions.setswitch.others")) {
				Player targetPlayer = Utils.getPlayer(args[1]);
				if (targetPlayer == null) {
					Error.PLAYER_NOT_FOUND.displayActionBar(sender);
					return Arrays.asList("");
				}

				List<String> completions = new ArrayList<String>();

				// List of permission sets this player might switch to
				for (PermissionSet set : new PlayerPerms(targetPlayer).getAllSets()) {
					completions.add(set.getName());
				}

				return completions;
			}

			if (args.length == 2) {
				// Info command
				List<String> completions = new ArrayList<String>();
				completions.add("info");

				// List of permission sets this player might switch to
				/*
				 * for(PermissionSet set : new PlayerPerms((Player) sender).getAllSets()){
				 * completions.add(set.getName()); }
				 */

				// If this player has the right perms, list other players they can change perms
				// of
				if (sender.hasPermission("core.permissions.setswitch.others")) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						completions.add(player.getName());
					}
				}

				return completions;
			}
		}

		//// Player command
		if (args.length == 3 && args[0].equalsIgnoreCase("player")) {
			return Arrays.asList("info", "set", "revoke");
		}

		return Arrays.asList("");
	}
}
