package com.KyleNecrowolf.RealmsCore.Prompts;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.event.NPCRightClickEvent;

public final class NPCListener implements Listener {

    // Citizens Integration - TO BE REMOVED AND REPLACED IN REALMSSTORY
	@EventHandler
	public void onNPCClick(NPCRightClickEvent event){
		// Display the prompt according to the template associated with this NPC
		new NPCTemplate(event.getNPC().getId()).displayPrompt(event.getClicker(), event.getNPC().getFullName());
    }
}