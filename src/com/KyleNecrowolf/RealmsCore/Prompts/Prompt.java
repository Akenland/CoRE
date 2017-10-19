package com.KyleNecrowolf.RealmsCore.Prompts;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Utils;

/**
 * A prompt that can be displayed to a {@link CommandSender}, showing them questions and allowing them to choose answers.
 */
public class Prompt {

    //// HashMap for player's actions in a prompt - static/global
	static HashMap<String,Integer> playerCode = new HashMap<String,Integer>();
	static HashMap<String,String> playerPromptPrefix = new HashMap<String,String>();
	static HashMap<String,List<String>> playerActions = new HashMap<String,List<String>>();


	//// Prompt Data
	private List<String> questions;
	private boolean randomQuestions;
	private List<String> answers;
	private List<String> actions;
	private List<String> conditions;


	//// Constructor
	/**
	 * Loads a prompt from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to load prompt data from
	 */
	public Prompt(ConfigurationSection config){
		// Load the data from file
		this.questions = config.getStringList("questions");
		this.randomQuestions = config.getBoolean("randomQuestions");
		this.answers = config.getStringList("answers");
		this.actions = config.getStringList("actions");
		this.conditions = config.getStringList("conditions");
	}
	/**
	 * Loads a prompt from RealmsCore's prompts folder. Prefixing the fileName with "tag:" will look in RealmsStory tags instead.
	 * @param fileName the name of the file to load from
	 * @param promptName the name of the prompt within that file
	 */
	public Prompt(String fileName, String promptName){
		fileName = fileName.toLowerCase();
		promptName = promptName.toLowerCase();
		ConfigurationSection file;

		// If fileName begins with tag, load tag file
		Plugin realmsStory = Bukkit.getPluginManager().getPlugin("RealmsStory");
		if(fileName.startsWith("tag:") && realmsStory!=null){
			String tagName = fileName.replace("tag:", "");
			file = new ConfigAccessor("tags\\"+tagName+".yml", realmsStory).getConfig().getConfigurationSection("prompts");
		}

		// Else, load from RealmsCore prompts folder
		else{
			file = new ConfigAccessor("prompts\\"+fileName+".yml").getConfig().getConfigurationSection(fileName);
		}
		
		file = file.getConfigurationSection(promptName);
		if(file!=null){
			// Load the data from file
			this.questions = file.getStringList("questions");
			this.randomQuestions = file.getBoolean("randomQuestions");
			this.answers = file.getStringList("answers");
			this.actions = file.getStringList("actions");
			this.conditions = file.getStringList("conditions");
		} else {
			Utils.notifyAdminsError("Could not find prompt "+fileName+"."+promptName);
		}
	}
	/**
	 * Loads a prompt from RealmsCore's prompts folder. Prefixing the fileName with "tag:" will look in RealmsStory tags instead.
	 * @param combinedName the name of the file and prompt, in the format fileName.promptName
	 * @see Prompt#Prompt(String, String)
	 */
	public Prompt(String combinedName){
		this(combinedName.split("\\.",2)[0], combinedName.split("\\.",2)[1]);
	}
	/**
	 * Creates an empty prompt. Data must be added before this prompt can be displayed.
	 */
	public Prompt(){
		this.questions = new ArrayList<String>();
		this.answers = new ArrayList<String>();
		this.actions = new ArrayList<String>();
		this.conditions = new ArrayList<String>();
	}



	//// Prompt Editing methods
	/**
	 * Adds a question to this Prompt.
	 * @param question the question string to add
	 */
	public void addQuestion(String question){
		this.questions.add(question);
	}

	/**
	 * Adds an answer to this Prompt.
	 * @param answer the answer string to add
	 * @param action the action to run when this answer is clicked
	 * @param condition the conditions on which to show this answer
	 */
	public void addAnswer(String answer, String action, String condition){
		answers.add(answer);
		actions.add(action);
		conditions.add(condition);
	}
	/**
	 * Adds an answer to this Prompt.
	 * @param answer the answer string to add
	 * @param action the action to run when this answer is clicked
	 */
	public void addAnswer(String answer, String action){
		addAnswer(answer, action, null);
	}


	//// Prompt Display methods

	/**
	 * Displays this prompt to a {@link CommandSender}.
	 * @param sender the {@link CommandSender} to display to
	 * @param prefix an optional prefix for all questions
	 */
	public void display(CommandSender sender, String prefix){
		// If not loaded, show an error and stop
		if(questions==null || questions.isEmpty()){
			Utils.sendActionBar(sender, Utils.errorText+"Prompt not found. Ask an Admin for help.");
			Utils.notifyAdminsError("Could not find prompt to display to "+sender.getName());
			return;
		}

		// Make the prefix gray
		prefix = ChatColor.GRAY + prefix + ChatColor.GRAY;

		// If randomQuestion, pick one question line randomly
		if(randomQuestions){
			String questionLine = questions.get(ThreadLocalRandom.current().nextInt(questions.size()-1));
			sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', questionLine));
		}
		// Otherwise, display each line of the question
		else for (String questionLine : questions) sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', questionLine));


		// If there are answers, prompt the player and list the options
		if (!answers.isEmpty() && sender instanceof Player){

			// If server is incompatible version, warn player and admins, and do not display answers
			if(!Utils.isLatestVersion()){
				Utils.sendActionBar(sender, Utils.errorText+"This server must be updated to use prompts. Contact the admin.");
				Utils.notifyAdminsError("Could not display prompt to "+sender.getName()+Utils.errorText+", server is running an incompatible version.");
				return;
			}

			Utils.sendActionBar(sender, "Choose an option");

			// Generate a code for returning player input
			int randomCode = ThreadLocalRandom.current().nextInt(100);
			Prompt.playerCode.put(sender.getName(), randomCode);
			
			// Save the possible actions and prefix in a hashmap
			Prompt.playerPromptPrefix.put(sender.getName(), prefix);
			Prompt.playerActions.put(sender.getName(), actions);
			
			// Show each option, formatted with JSON
			for (int i = 0; i < answers.size(); i++) {
				String answer = ChatColor.translateAlternateColorCodes('&', answers.get(i));
				String action = actions.get(i);
				
				// If action exists, send the JSON message
				if(action!=null && action.length()>1){
					String actionType;
					String actionValue;
					
					if(action.startsWith("url_")){
						actionType = "open_url";
						actionValue = action.split("_",2)[1];
					} else {
						actionType = "run_command";
						actionValue = "/conversations trigger "+i+" "+randomCode;
					}

					String json = "[{\"text\":\"-> "+answer+"\",\"color\":\"gray\",\"clickEvent\":{\"action\":\""+actionType+"\",\"value\":\""+actionValue+"\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click to select option\",\"color\":\"gray\"}}}]";
					PromptJsonNMS.sendRawJson((Player) sender, json);
				} else {
					sender.sendMessage(ChatColor.GRAY+"-> "+answer);
				}
			}
		}
		
		// If it's not a player, just list the answers
		else if(!answers.isEmpty()){
			for (int i = 0; i < answers.size(); i++) {
				String answer = ChatColor.translateAlternateColorCodes('&', answers.get(i));
				String action = actions.get(i);
				if(action!=null && action.length()>1) action = "("+action+")";
				
				sender.sendMessage("-> "+answer+" "+action);
			}
		}
	}

	/**
	 * Displays this prompt to a {@link CommandSender}.
	 * @param sender the {@link CommandSender} to display to
	 */
	public void display(CommandSender sender){
		display(sender, "");
	}
}
