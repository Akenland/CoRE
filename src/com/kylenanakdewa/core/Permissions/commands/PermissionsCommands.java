package com.kylenanakdewa.core.permissions.commands;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.permissions.sets.PermissionSet;
import com.kylenanakdewa.core.permissions.PermissionsManager;

/**
 * Handles commands (including their tab-completions) for the CoRE Permissions
 * system.
 *
 * @author Kyle Nanakdewa
 */
public final class PermissionsCommands implements TabExecutor {

	/** The CoRE Permissions system that owns this command executor. */
	private final PermissionsManager permissionsManager;

	public PermissionsCommands(PermissionsManager permissionsManager) {
		this.permissionsManager = permissionsManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Admin Multi-Check
		if (!permissionsManager.performAdminMultiCheck(sender)) {
			Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
					+ " failed security check (permissions command: " + String.join(" ", args) + ").");
			return Error.NO_PERMISSION.displayChat(sender);
		}

		// All perms commands require at least one arg
		if (args.length == 0)
			return Error.INVALID_ARGS.displayActionBar(sender);

		// Permission set info
		if (args[0].equalsIgnoreCase("set") && args.length == 2) {
			String setName = args[1];

			// Check permissions
			if (!sender.hasPermission("core.permissions.set-info")) {
				Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR + " failed security check (set info for "
						+ setName + ").");
				return Error.NO_PERMISSION.displayChat(sender);
			}

			// Check that set exists
			PermissionSet set = permissionsManager.getPermissionSet(setName);
			if (set == null) {
				Utils.sendActionBar(sender, CommonColors.ERROR + "Permission set not found.");
				return false;
			}

			// Display info prompt
			set.getInfoPrompt().display(sender);

			return true;
		}

		// Player info
		if (args[0].equalsIgnoreCase("player") && args.length == 2) {
			String playerName = args[1];

			// Check permissions
			if (!sender.hasPermission("core.permissions.player-info")) {
				Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR
						+ " failed security check (player info for " + playerName + ").");
				return Error.NO_PERMISSION.displayChat(sender);
			}

			// Get player
			Player player = Utils.getPlayer(playerName);
			if (player == null) {
				return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
			}

			// Display info prompt
			permissionsManager.getPlayer(player).getPermissionsInfoPrompt().display(sender);

			return true;
		}

		// Player revoke
		if (args[0].equalsIgnoreCase("revoke") && args.length == 2) {
			String playerName = args[1];

			// Check permissions
			if (!sender.hasPermission("core.permissions.revoke")) {
				Utils.notifyAdminsError(sender.getName() + CommonColors.ERROR + " failed security check (revoking for "
						+ playerName + ").");
				return Error.NO_PERMISSION.displayChat(sender);
			}

			// Get player
			Player player = Utils.getPlayer(playerName);
			if (player == null) {
				return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
			}

			// Revoke permissions
			permissionsManager.getPlayer(player).revokePermissions();

			return true;
		}

		return Error.INVALID_ARGS.displayActionBar(sender);
	}

	//// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		// Base command
		if (args.length == 1) {
			List<String> completions = new ArrayList<String>();
			if (sender.hasPermission("core.permissions.set-info"))
				completions.add("set");
			if (sender.hasPermission("core.permissions.player-info"))
				completions.add("player");
			if (sender.hasPermission("core.permissions.revoke"))
				completions.add("revoke");
			return completions;
		}

		// Permission set info
		if (args[0].equalsIgnoreCase("set") && args.length == 2 && sender.hasPermission("core.permissions.set-info")) {
			List<String> completions = new ArrayList<String>();
			for (PermissionSet set : permissionsManager.getPlayer((Player) sender).getAllSets()) {
				completions.add(set.getName());
			}
			return completions;
		}

		// Player info/revoke
		if (args.length == 2
				&& ((args[0].equalsIgnoreCase("player") && sender.hasPermission("core.permissions.player-info"))
						|| (args[0].equalsIgnoreCase("revoke") && sender.hasPermission("core.permissions.revoke")))) {
			return null;
		}

		return Arrays.asList("");
	}
}
