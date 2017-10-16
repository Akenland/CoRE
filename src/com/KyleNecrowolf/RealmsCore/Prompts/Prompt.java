package com.KyleNecrowolf.RealmsCore.Prompts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Utils;

public class Prompt {

    //// HashMap for player's actions in a prompt - static/global
	static HashMap<String,Integer> playerCode = new HashMap<String,Integer>();
	static HashMap<String,String> playerPromptPrefix = new HashMap<String,String>();
	static HashMap<String,List<String>> playerActions = new HashMap<String,List<String>>();


	//// Prompt Data
	private final String conversationName;
	private final String promptName;

	private boolean loaded;

	private List<String> question;
	private boolean randomQuestion;
	private List<String> answers;
	private List<String> actions;
	private List<String> conditions;


	//// Constructor
	// Create a prompt from a file, with the conversation (file) name and the prompt name
	public Prompt(String conversationName, String promptName) {
		// Save the information in this object
		this.conversationName = conversationName.toLowerCase();
		this.promptName = promptName.toLowerCase();

		// If conversation name is tag, load tag file
		if(this.conversationName.startsWith("tag:")){
			String tagName = this.conversationName.replace("tag:", "");
			// TODO
			return;
		}

		// Load the specified conversation file
		FileConfiguration conversationFile = new ConfigAccessor("prompts\\"+conversationName+".yml").getConfig();
		if (conversationFile.contains(this.conversationName + "." + this.promptName)) {
			// Load the data from file
			this.question = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".question");
			this.randomQuestion = conversationFile.getBoolean(this.conversationName + "." + this.promptName + ".randomQuestion");
			this.answers = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".answers");
			this.actions = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".actions");
			this.conditions = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".conditions");

			loaded = true;
		}
	}
	
	// Create a prompt from a file, with the combined name (conversationName.promptName)
	public Prompt(String combinedName){
		this(combinedName.split("\\.",2)[0], combinedName.split("\\.",2)[1]);
	}
	
	
	// Create a prompt by suppling the values directly
	public Prompt(List<String> questions, List<String> questionConditions, boolean randomQuestion, List<String> answers, List<String> actions, List<String> answerConditions){
		this.conversationName = "generated";
		this.promptName = "temp";
		this.question = questions;
		this.randomQuestion = randomQuestion;
		this.answers = answers;
		this.actions = actions;
		this.conditions = answerConditions;
		
		loaded = true;
	}
	public Prompt(List<String> question, List<String> answers, List<String> actions, List<String> conditions){
		this.conversationName = "generated";
		this.promptName = "temp";
		this.question = question;
		this.answers = answers;
		this.actions = actions;
		this.conditions = conditions;
		
		loaded = true;
	}
	public Prompt(String question, List<String> answers, List<String> actions, List<String> conditions){
		this(Arrays.asList(question), answers, actions, conditions);
	}

	// Create an empty prompt and supply the values later
	public Prompt(){
		this(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		
		// Prompt must be marked unloaded, as the values may be blank
		loaded = false;
	}


	
	//// Prompt Editing methods

	// Add a question item
	public void addQuestion(String question){
		this.question.add(question);
		loaded = true;
	}

	// Add an answer item
	public void addAnswer(String answer, String action, String condition){
		answers.add(answer);
		actions.add(action);
		conditions.add(condition);
	}
	public void addAnswer(String answer, String action){
		addAnswer(answer, action, null);
	}


	//// Prompt Display methods

	// Display the prompt to a player
	public void display(CommandSender sender, String prefix) {
		// If not loaded, show an error and stop
		if(!this.loaded){
			Utils.sendActionBar(sender, Utils.errorText+"Prompt not found. Ask an Admin for help.");
			Utils.notifyAdminsError("Could not find prompt "+conversationName+"."+promptName+" to display to "+sender.getName());
			return;
		}
		
		// If randomQuestion, pick one question line randomly
		if(randomQuestion){
			String questionLine = question.get(ThreadLocalRandom.current().nextInt(question.size()-1));
			sender.sendMessage(Utils.messageText + prefix + Utils.messageText + ChatColor.translateAlternateColorCodes('&', questionLine));
		} else {
			// Otherwise, display each line of the question (delayed slightly)
			//int delay = 0;
			for (String questionLine : question) {
				String message = Utils.messageText + prefix + Utils.messageText + ChatColor.translateAlternateColorCodes('&', questionLine);
				sender.sendMessage(message);
				/*new BukkitRunnable(){
					@Override
					public void run(){
						sender.sendMessage(message);
					}
				}.runTaskLater(Main.getProvidingPlugin(Main.class), delay);
				delay += 10;*/
			}
		}
		
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

	public void display(CommandSender sender) {
		display(sender, "");
	}
}
