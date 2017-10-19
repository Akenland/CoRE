package com.KyleNecrowolf.RealmsCore.Prompts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Utils;

/**
 * A prompt that can be displayed to CommandSenders, showing them "questions" and allowing them to choose answers.
 */
public class Prompt {

    //// HashMap for player's actions in a prompt - static/global
	static HashMap<String,Integer> playerCode = new HashMap<String,Integer>();
	static HashMap<String,String> playerPromptPrefix = new HashMap<String,String>();
	static HashMap<String,List<String>> playerActions = new HashMap<String,List<String>>();


	//// Prompt Data
	private final String conversationName;
	private final String promptName;

	private List<String> questions;
	private boolean randomQuestions;
	private List<String> answers;
	private List<String> actions;
	private List<String> conditions;


	//// Constructor
	/**
	 * Creates a prompt from a ConfigurationSection.
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
		// Save the information in this object
		fileName = fileName.toLowerCase();
		promptName = promptName.toLowerCase();

		// If conversation name is tag, load tag file
		if(this.conversationName.startsWith("tag:")){
			String tagName = this.conversationName.replace("tag:", "");
			// TODO
			return;
		}

		// Load the specified conversation file
		FileConfiguration conversationFile = new ConfigAccessor("prompts\\"+fileName+".yml").getConfig();
		if (conversationFile.contains(this.conversationName + "." + this.promptName)) {
			// Load the data from file
			this.questions = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".question");
			this.randomQuestions = conversationFile.getBoolean(this.conversationName + "." + this.promptName + ".randomQuestion");
			this.answers = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".answers");
			this.actions = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".actions");
			this.conditions = conversationFile.getStringList(this.conversationName + "." + this.promptName + ".conditions");
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
		this.questions = questions;
		this.randomQuestions = randomQuestion;
		this.answers = answers;
		this.actions = actions;
		this.conditions = answerConditions;
		}
	public Prompt(List<String> question, List<String> answers, List<String> actions, List<String> conditions){
		this.conversationName = "generated";
		this.promptName = "temp";
		this.questions = question;
		this.answers = answers;
		this.actions = actions;
		this.conditions = conditions;
	}
	public Prompt(String question, List<String> answers, List<String> actions, List<String> conditions){
		this(Arrays.asList(question), answers, actions, conditions);
	}

	/**
	 * Creates an empty prompt. Data must be added before this prompt can be displayed.
	 */
	public Prompt(){
		this(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
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
	public void addAnswer(String answer, String action){
		addAnswer(answer, action, null);
	}


	//// Prompt Display methods

	/**
	 * Displays this prompt to a {@link CommandSender}.
	 * @param sender the CommandSender to display to
	 * @param prefix an optional prefix for all questions
	 */
	public void display(CommandSender sender, String prefix) {
		// If not loaded, show an error and stop
		if(questions==null || questions.isEmpty()){
			Utils.sendActionBar(sender, Utils.errorText+"Prompt not found. Ask an Admin for help.");
			Utils.notifyAdminsError("Could not find prompt "+conversationName+"."+promptName+" to display to "+sender.getName());
			return;
		}
		
		// If randomQuestion, pick one question line randomly
		if(randomQuestions){
			String questionLine = questions.get(ThreadLocalRandom.current().nextInt(questions.size()-1));
			sender.sendMessage(Utils.messageText + prefix + Utils.messageText + ChatColor.translateAlternateColorCodes('&', questionLine));
		} else {
			// Otherwise, display each line of the question (delayed slightly)
			//int delay = 0;
			for (String questionLine : questions) {
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
