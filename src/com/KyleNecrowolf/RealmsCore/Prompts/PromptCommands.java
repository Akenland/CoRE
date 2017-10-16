package com.KyleNecrowolf.RealmsCore.Prompts;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;

public final class PromptCommands implements TabExecutor {

	//// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//// Help command
		if(command.getName().equalsIgnoreCase("help")){
			// No args, show prompt help.main
			if(args.length==0) new Prompt("help", "main").display(sender);

			// One arg, show prompt help.<arg>
			else new Prompt("help", args[0]).display(sender);

			return true;
		}


		//// Trigger command - used for prompt actions
		if(args.length==3 && args[0].equalsIgnoreCase("trigger") && sender instanceof Player){

			// Make sure sender has valid actions they can take, and that the code matches
			if(!Prompt.playerCode.containsKey(sender.getName()) || !Prompt.playerCode.get(sender.getName()).equals(Integer.parseInt(args[2]))){
				Utils.sendActionBar(sender, Utils.errorText+"This prompt is no longer valid!");
				return true;
			}
			
			// Get the action that they clicked
			String action = Prompt.playerActions.get(sender.getName()).get(Integer.parseInt(args[1]));
			String[] splitAction = action.split("_", 2);

			// Call the event so plugins can respond to this action
			Bukkit.getServer().getPluginManager().callEvent(new PromptActionEvent((Player)sender, splitAction));
			Bukkit.getLogger().info(sender.getName()+" used prompt action: "+action);

			return true;
		}
		

		//// Sending a prompt to a player
		// Old command, kept for command block support
		if(args.length==4 && args[0].equalsIgnoreCase("sendprompt") && sender.hasPermission("realms.prompts.sendprompt")){
			sender.sendMessage(Utils.infoText+"This command has been replaced, please use /prompts display [player] <prompt name>");
			Player targetPlayer = Utils.getPlayer(args[1]);
			new Prompt(args[2],args[3]).display(targetPlayer);
			Utils.sendActionBar(sender, Utils.messageText+"Sent prompt to "+targetPlayer.getDisplayName());
			return true;
		}
		
		// New command
		if(args.length>=2 && args[0].equalsIgnoreCase("display")){
			if(!sender.hasPermission("realms.prompts.display")) return Error.NO_PERMISSION.displayChat(sender);

			// If two args, display prompt (combined name) to self
			if(args.length==2){
				new Prompt(args[1]).display(sender);
				return true;
			}

			// If three args, display prompt (combined name) to another player
			if(args.length==3){
				// Make sure player is valid
				Player targetPlayer = Utils.getPlayer(args[1]);
				if(targetPlayer==null){
					return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
				}

				new Prompt(args[2]).display(targetPlayer);
				return true;
			}
		}
		

		return Error.INVALID_ARGS.displayActionBar(sender);
	}


	//// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		//// Main command
		if(args.length<=1){
			return Arrays.asList("display");
		}


		//// Display command - don't show completions on third arg (which must be combined prompt name)
		if(args.length>2 && args[0].equalsIgnoreCase("display")){
			return Arrays.asList("");
		}


		return null;
	}
}