package com.KyleNecrowolf.RealmsCore.Prompts;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class PromptActionListener implements Listener {

    // Prompt Actions
	@EventHandler
	public void onPromptAction(PromptActionEvent event){
            
        // Prompt action - send off a new prompt
		if(event.isType("prompt")){
		    Player player = event.getPlayer();
			new Prompt(event.getAction()).display((Player) player, Prompt.playerPromptPrefix.get(player.getName()));
		}
            
            
		// Command action - send a command from the player
		if(event.isType("command")){
			event.getPlayer().performCommand(event.getAction());
        }
        
	}
}