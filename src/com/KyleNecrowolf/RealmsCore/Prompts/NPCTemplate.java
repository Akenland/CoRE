package com.KyleNecrowolf.RealmsCore.Prompts;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Utils;

// Represents an NPCTemplate for questing/conversations system
// NOTE: To be moved into RealmsStory
public class NPCTemplate {

	// Data about this NPC template
	private String name; // name of template
	private boolean loaded; // true when data is loaded and ready for use
	
	private List<String> prompts; // list of starting prompts to use
	private List<String> conditions; // conditions on which to show the prompts
	
	
	// Constructor
	public NPCTemplate(String name){
		// Make sure name isn't null
		if(name==null) return;
		
		this.name = name.toLowerCase();
		
		FileConfiguration templateFile = new ConfigAccessor("npctemplates\\"+this.name + ".yml").getConfig();
		if (templateFile.contains(this.name)) {
			// Load the data from file
			this.prompts = templateFile.getStringList(this.name + ".prompts");
			this.conditions = templateFile.getStringList(this.name + ".conditions");

			this.loaded = true;
		}
	}
	
	// Alternative constructor that takes an NPC ID and returns the associated template
	public NPCTemplate(int npcID){
		this(new ConfigAccessor("npclist.yml").getConfig().getString("npcs."+npcID));
	}
	
	
	// Get the appropriate prompt for a player
	public Prompt getPrompt(Player player){
		// If not loaded, return null
		if(!this.loaded) return null;
		
		//TODO - check conditions
		// For now we'll just return the first prompt
		return new Prompt(prompts.get(0));
	}
	
	// Display the appropriate prompt for a player
	public void displayPrompt(Player player, String npcName){
		// If not loaded, return
		if(!this.loaded) return;
		
		// Format the NPC name nicely
		npcName = Utils.messageText+"<"+npcName+Utils.messageText+"> ";
		
		getPrompt(player).display(player, npcName);
	}
}
