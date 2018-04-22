package com.kylenanakdewa.core.Extras;

import java.util.ArrayList;
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

    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Check permissions
		if(!sender.hasPermission("core.item")) return Error.NO_PERMISSION.displayChat(sender);

		// If no args or sender is not player, return error
		if(args.length==0 || !(sender instanceof Player)) return Error.INVALID_ARGS.displayActionBar(sender);


		// Item data
		final Material material = Material.matchMaterial(args[0]);
		int amount = 0;
		short damage = 0;
		try{
			amount = (args.length>=2) ? Integer.parseInt(args[1]) : 1;
			damage = (args.length>=3) ? Short.parseShort(args[2]) : 0;
		} catch(NumberFormatException e){}

		// Make sure material is not null
		if(material==null) return Error.ITEM_NOT_FOUND.displayActionBar(sender);
		// Make sure amount is greater than 0
		if(amount<=0 || damage<0) return Error.INVALID_ARGS.displayActionBar(sender);

		// Give item to player
		((Player)sender).getInventory().addItem(new ItemStack(material, amount, damage));
		Utils.sendActionBar(sender, amount+" "+material.name().replace("_", " ").toLowerCase()+" added to inventory");
		return true;
	}


	//// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length==1 && sender.hasPermission("core.item")){
			// Add all item names as completion
			List<String> completions = new ArrayList<String>();
			for(Material type : Material.values()) completions.add(type.name());
			return completions;
		}
		
		return null;
	}
}