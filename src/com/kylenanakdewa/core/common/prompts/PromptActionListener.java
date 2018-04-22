package com.kylenanakdewa.core.common.prompts;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Event handler for prompt actions. Handles "prompt" and "command" actions.
 * @author Kyle Nanakdewa
 */
public final class PromptActionListener implements Listener {

	// Prompt Actions
	@EventHandler
	public void onPromptAction(PromptActionEvent event){

		// Prompt action - send off a new prompt
		if(event.isType("prompt")){
			Prompt.getFromPluginFolder(event.getAction()).display(event.getPlayer());
		}

		// Command action - send a command from the player
		if(event.isType("command")){
			event.getPlayer().performCommand(event.getAction());
		}

		// Console command action - send a command from the console
		if(event.isType("consolecommand")){
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getAction());
		}

	}
}