package com.kylenanakdewa.core.Extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

public final class ItemCommands implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("core.item") || !(sender instanceof Player)) {
			return Error.NO_PERMISSION.displayChat(sender);
		}

		// Must have 1 or 2 args
		if (args.length != 1 && args.length != 2) {
			return Error.INVALID_ARGS.displayActionBar(sender);
		}

		// First arg - material name
		Material material = Material.matchMaterial(args[0]);
		if (material == null) {
			return Error.ITEM_NOT_FOUND.displayActionBar(sender);
		}

		// Second arg - amount
		int amount = 1;
		if (args.length == 2) {
			String amountArg = args[1];
			try {
				amount = Integer.parseInt(amountArg);
			} catch (NumberFormatException e) {
				return Error.INVALID_ARGS.displayActionBar(sender);
			}
		}

		// Give item to player
		((Player) sender).getInventory().addItem(new ItemStack(material, amount));
		Utils.sendActionBar(sender,
				amount + " " + material.name().replace("_", " ").toLowerCase() + " added to inventory");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (sender.hasPermission("core.item")) {
			// First arg - material name
			if (args.length == 1) {
				String materialArg = args[0].toUpperCase();

				// If no arg was entered, add all item names as completions
				if (materialArg.length() == 0) {
					List<String> completions = new ArrayList<String>();
					for (Material type : Material.values()) {
						completions.add(type.name());
					}
					return completions;
				}

				List<String> matches = new ArrayList<String>();
				List<String> partialMatches = new ArrayList<String>();
				for (Material type : Material.values()) {
					// Show closest matches at the top
					if (type.name().startsWith(materialArg)) {
						matches.add(type.name());
					}
					// Show partial matches below that
					else if (type.name().contains(materialArg)) {
						partialMatches.add(type.name());
					}
				}
				matches.addAll(partialMatches);
				return matches;
			}

			// Second arg - amount
			if (args.length == 2) {
				return Arrays.asList("16", "32", "64", "128", "256", "512", "576", "1728");
			}
		}

		return Arrays.asList("");
	}
}