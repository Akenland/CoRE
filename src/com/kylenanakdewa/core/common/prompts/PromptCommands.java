package com.kylenanakdewa.core.common.prompts;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt.Answer;

/**
 * Commands for displaying a {@link Prompt}, and for triggering prompt actions.
 * @author Kyle Nanakdewa
 */
public final class PromptCommands implements TabExecutor {

	/** The Project CoRE plugin instance. */
	private final CorePlugin plugin;

	/**
	 * Creates a command handler for the specified instance of Project CoRE.
	 */
	public PromptCommands(CorePlugin plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//// Help command
		if(command.getName().equalsIgnoreCase("help")){
			// No args, show prompt help.main - One arg, show prompt help.<arg>
			Prompt helpPrompt = args.length==0 ? Prompt.getFromPluginFolder("help", "main") : Prompt.getFromPluginFolder("help", args[0]);

			if(helpPrompt==null){
				Utils.sendActionBar(sender, CommonColors.ERROR+"Help topic not found.");
				return false;
			}

			helpPrompt.display(sender);
			return true;
		}


		//// Trigger command - used for prompt actions
		if(args.length==3 && args[0].equalsIgnoreCase("trigger") && sender instanceof Player){

			// Make sure sender has valid actions they can take, and that the code matches
			List<Answer> answerList = Prompt.getSenderAnswerList(sender, Integer.parseInt(args[2]));
			if(answerList==null){
				Utils.sendActionBar(sender, CommonColors.ERROR+"This prompt is no longer valid!");
				return true;
			}

			// Get the action that they clicked
			String action = answerList.get(Integer.parseInt(args[1])).getAction();

			// Call the event so plugins can respond to this action
			Bukkit.getServer().getPluginManager().callEvent(new PromptActionEvent((Player)sender, action));
			plugin.getLogger().info(sender.getName()+" used prompt action: "+action);

			return true;
		}


		//// Sending a prompt to a player
		if(args.length>=2 && args[0].equalsIgnoreCase("display")){
			if(!sender.hasPermission("core.prompts.display")) return Error.NO_PERMISSION.displayChat(sender);

			Prompt prompt = null;
			CommandSender target = null;

			// If two args, display prompt (combined name) to self
			if(args.length==2){
				prompt = Prompt.getFromPluginFolder(args[1]);
				target = sender;
			}
			// If three args, display prompt (combined name) to another player
			else if(args.length==3){
				prompt = Prompt.getFromPluginFolder(args[2]);
				target = Utils.getPlayer(args[1]);
			}
			else return Error.INVALID_ARGS.displayActionBar(sender);

			// Make sure prompt and target are valid
			if(prompt==null){
				Utils.sendActionBar(sender, CommonColors.ERROR+"Prompt not found.");
				return false;
			}
			if(target==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

			// Display the prompt
			prompt.display(target);
			return true;
		}


		return Error.INVALID_ARGS.displayActionBar(sender);
	}


	//// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		//// Main command
		if(!command.getName().equalsIgnoreCase("help") && args.length<=1){
			return Arrays.asList("display");
		}


		//// Display command - don't show completions on third arg (which must be combined prompt name)
		if(args.length>2 && args[0].equalsIgnoreCase("display")){
			return Arrays.asList("");
		}


		return null;
	}
}