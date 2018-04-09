package com.kylenanakdewa.core.common.prompts;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.ConfigAccessor;
import com.kylenanakdewa.core.common.Utils;

/**
 * A prompt that can be displayed to a {@link CommandSender}, showing them questions and allowing them to choose answers.
 * @author Kyle Nanakdewa
 */
public class Prompt {

	/** The questions in this prompt, in the order they will be displayed. */
	private List<String> questions;
	/** Whether the questions in this prompt will be displayed in a random order, instead of in sequence. */
	private boolean randomQuestions;
	/** The answers in this prompt, in the order they will be displayed. */
	private List<Answer> answers;


	/**
	 * Loads a prompt from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to load prompt data from
	 * @deprecated use {@link #getFromConfig(ConfigurationSection)} instead
	 */
	@Deprecated
	public Prompt(ConfigurationSection config){
		// Make sure file exists
		if(config==null) return;

		// Load the data from file
		this.questions = config.getStringList("questions");
		this.randomQuestions = config.getBoolean("randomQuestions");
		setAnswers(config.getStringList("answers"), config.getStringList("actions"), config.getStringList("conditions"));
	}

	/**
	 * Loads a prompt from RealmsCore's prompts folder. Prefixing the fileName with "tag:" will look in RealmsStory tags instead.
	 * @param fileName the name of the file to load from
	 * @param promptName the name of the prompt within that file
	 * @deprecated use {@link #getFromPluginFolder(String, String)} instead
	 */
	@Deprecated
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

		if(file==null) Utils.notifyAdminsError("Could not find prompt "+fileName+"."+promptName);

		file = file.getConfigurationSection(promptName);
		if(file!=null){
			// Load the data from file
			this.questions = file.getStringList("questions");
			this.randomQuestions = file.getBoolean("randomQuestions");
			setAnswers(file.getStringList("answers"), file.getStringList("actions"), file.getStringList("conditions"));
		} else {
			Utils.notifyAdminsError("Could not find prompt "+fileName+"."+promptName);
		}
	}
	/**
	 * Loads a prompt from RealmsCore's prompts folder. Prefixing the fileName with "tag:" will look in RealmsStory tags instead.
	 * @param combinedName the name of the file and prompt, in the format fileName.promptName
	 * @see Prompt#Prompt(String, String)
	 * @deprecated use {@link #getFromPluginFolder(String)} instead
	 */
	@Deprecated
	public Prompt(String combinedName){
		this(combinedName.split("\\.",2)[0], combinedName.split("\\.",2)[1]);
	}

	/**
	 * Creates an empty prompt.
	 */
	public Prompt(){

	}

	/**
	 * Retrieves a prompt from a {@link ConfigurationSection}.
	 * <p>
	 * Prompt data will be retrieved from the following paths:
	 * <ul>
	 * <li>questions (string list)
	 * <li>random-questions (boolean)
	 * <li>answers (string list)
	 * <li>actions (string list)
	 * <li>conditions (string list)
	 * </ul>
	 * All values are optional.
	 * @param config the {@link ConfigurationSection} to load prompt data from
	 * @return the prompt, or null if the config was null
	 * @throws IllegalArgumentException if the answers/actions/conditions lists are not the same length
	 */
	public static Prompt getFromConfig(ConfigurationSection config){
		Prompt prompt = new Prompt();

		if(config!=null){
			// Load the data from file
			prompt.setQuestions(config.getStringList("questions"));
			prompt.setRandomQuestions(config.getBoolean("randomQuestions"));
			prompt.setAnswers(config.getStringList("answers"), config.getStringList("actions"), config.getStringList("conditions"));
		} else return null;

		return prompt;
	}

	/**
	 * Retrieves a prompt from the plugin's prompts folder.
	 * @param fileName the name of the file to load from
	 * @param promptName the name of the prompt within that file
	 * @return the prompt, or null if none was found
	 */
	public static Prompt getFromPluginFolder(String fileName, String promptName){
		fileName = fileName.toLowerCase();
		promptName = promptName.toLowerCase();

		// Load the file
		ConfigurationSection file = new ConfigAccessor("prompts\\"+fileName+".yml").getConfig().getConfigurationSection(fileName);
		if(file!=null){
			// Load the prompt
			file = file.getConfigurationSection(promptName);
			if(file!=null) return getFromConfig(file);
		}

		// If file is null
		Utils.notifyAdminsError("Could not find prompt "+fileName+"."+promptName);
		return null;
	}
	/**
	 * Retrieves a prompt from the plugin's prompts folder.
	 * @param combinedName the name of the file and prompt, in the format fileName.promptName
	 * @return the prompt, or null if none was found
	 * @see Prompt#getFromPluginFolder(String, String)
	 */
	public static Prompt getFromPluginFolder(String combinedName){
		String[] splitName = combinedName.split("\\.",2);
		if(splitName.length==2)
			return getFromPluginFolder(splitName[0], splitName[1]);

		// If string cannot be split
		Utils.notifyAdminsError("Invalid prompt name "+combinedName);
		return null;
	}


	/**
	 * Represents an answer that the player can choose from, in a Prompt.
	 */
	public class Answer {

		private String answerText;
		private String action;
		private String condition;

		/**
		 * Creates a new answer.
		 * @param answer the text for this answer
		 * @param action the action to run when this answer is clicked
		 * @param condition the condition on which to show this answer, or null to always show
		 */
		private Answer(String answerText, String action, String condition){
			this.answerText = ChatColor.translateAlternateColorCodes('&', answerText);
			this.action = action;
			this.condition = condition;
		}

		/**
		 * Gets the text to display for this answer.
		 * @return the display text for this answer
		 */
		public String getText(){
			return answerText;
		}
		/**
		 * Replaces a variable in the answer text.
		 * @param target the text to be replaced
		 * @param replacement the new text
		 * @return this answer, for chaining calls
		 * @see String#replace(CharSequence, CharSequence)
		 */
		public Answer replaceText(String target, String replacement){
			answerText = answerText.replace(target, replacement);
			return this;
		}

		/**
		 * Gets the action for this answer.
		 * @return the action string for this answer
		 */
		public String getAction(){
			return action;
		}
		/**
		 * Replaces a variable in the action string.
		 * @param target the text to be replaced
		 * @param replacement the new text
		 * @return this answer, for chaining calls
		 * @see String#replace(CharSequence, CharSequence)
		 */
		public Answer replaceAction(String target, String replacement){
			action = action.replace(target, replacement);
			return this;
		}

		/**
		 * Evaluates whether an {@link Entity} meets the Story {@link com.kylenanakdewa.realmsstory.tags.Condition} to see this answer.
		 * If there is no condition, this will return true. If RealmsStory is not installed, this will return false.
		 * @param entity the entity to evaluate this answer's condition against
		 * @return true if this entity should see this answer
		 */
		public boolean visibleTo(Entity entity){
			// If no condition, this always returns true
			if(condition==null || condition.isEmpty()) return true;

			// If Story is not installed, return false
			Plugin storyPlugin = Bukkit.getPluginManager().getPlugin("RealmsStory");
			if(storyPlugin==null || !storyPlugin.isEnabled()) return false;

			// Evaluate the condition
			return new com.kylenanakdewa.realmsstory.tags.Condition(condition).eval(entity);
		}

		/**
		 * Gets the formatted answer text.
		 * @return the display text for this answer, exactly as it should appear in-game
		 */
		private String getFormattedAnswerText(){
			return CommonColors.INFO+"-> " + CommonColors.MESSAGE+answerText;
		}
		/**
		 * Gets the raw JSON string for this answer, as it would be used in /tellraw.
		 * @param code the random code of this prompt, used to trigger correct action
		 * @return the raw JSON string
		 */
		private String getRawJson(int code){
			/** The action type, as used in Minecraft's raw JSON. */
			String actionType;
			/** The action value, as used in Minecraft's raw JSON. */
			String actionValue;

			/** Text displayed when hovering over this answer. */
			String hoverText = CommonColors.MESSAGE+"Click to select option";

			if(action.startsWith("url_")){
				actionType = "open_url";
				actionValue = action.split("_",2)[1];
			} else {
				actionType = "run_command";
				actionValue = "/conversations trigger "+answers.indexOf(this)+" "+code;
			}

			return "[{\"text\":\""+getFormattedAnswerText()+"\",\"clickEvent\":{\"action\":\""+actionType+"\",\"value\":\""+actionValue+"\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""+hoverText+"\"}}}]";
		}
		/**
		 * Sends this answer to a CommandSender, either the raw JSON or normal message, depending on type of sender.
		 * @param target the {@link CommandSender} to display to
		 * @param code the random code for verifying the prompt response
		 */
		private void sendAnswer(CommandSender target, int code){
			// If player, use raw JSON
			if(target instanceof Player) Utils.sendRawJson((Player)target, getRawJson(code));

			// If not a player, send normal message
			else target.sendMessage(getFormattedAnswerText());
		}
	}

	/** The answer lists for CommandSenders. */
	private static final HashMap<CommandSender, SenderAnswerList> senderAnswerLists = new HashMap<CommandSender, SenderAnswerList>();
	/**
	 * The answers usable by a {@link CommandSender}.
	 */
	private static final class SenderAnswerList {

		/** The answers in this answer list. */
		private List<Answer> answers;
		/** The random code of this answer list, so the right prompt is triggered. */
		private int code;

		private SenderAnswerList(){

		}

		/**
		 * Updates the answer list.
		 * @param newAnswers the new answers
		 * @return a random code that will be used to identify the sent prompt
		 */
		private int update(List<Answer> newAnswers){
			this.answers = newAnswers;

			// Random code
			code = ThreadLocalRandom.current().nextInt(100);
			return code;
		}

		/** Gets the random code. */
		private int getCode(){
			return code;
		}
		/** Gets the answer list. */
		private List<Answer> getAnswers(){
			return answers;
		}
	}
	/**
	 * Updates the saved answer list for a CommandSender.
	 * @param sender the CommandSender (usually a player)
	 * @param newAnswers the new answers
	 * @return a random code that will be used to identify the sent prompt
	 */
	private static int updateSenderAnswerList(CommandSender sender, List<Answer> newAnswers){
		SenderAnswerList answerList = new SenderAnswerList();
		int code = answerList.update(newAnswers);
		senderAnswerLists.put(sender, answerList);
		return code;
	}
	/**
	 * Gets the available answers for a CommandSender.
	 * @param sender the CommandSender to retrieve for
	 * @param code the random code from the prompt
	 * @return the answer list if the code is correct, otherwise null
	 */
	static List<Answer> getSenderAnswerList(CommandSender sender, int code){
		SenderAnswerList answerList = senderAnswerLists.get(sender);
		return answerList!=null&&code==answerList.getCode() ? answerList.getAnswers() : null;
	}


	//// Prompt Editing methods
	/**
	 * Adds a question to this Prompt.
	 * @param question the question string to add
	 */
	public void addQuestion(String question){
		// Create list, if needed
		if(questions==null) questions = new ArrayList<String>();

		questions.add(ChatColor.translateAlternateColorCodes('&', question));
	}
	/**
	 * Gets the questions in this Prompt.
	 * @return all questions (in the order they will be displayed), or null if questions were not set
	 */
	public List<String> getQuestions(){
		return questions;
	}
	/**
	 * Sets the questions in this Prompt. The previous questions will be overwritten.
	 * @param questions the new questions
	 */
	public void setQuestions(List<String> questions){
		// Convert color codes
		questions.forEach(question -> ChatColor.translateAlternateColorCodes('&', question));

		this.questions = questions;
	}

	/**
	 * Checks if questions will be displayed randomly, instead of in order.
	 * @return true if questions will be displayed at random
	 */
	public boolean isRandomQuestions(){
		return randomQuestions;
	}
	/**
	 * Sets if questions will be displayed randomly, instead of in order.
	 * @param random true if questions will be displayed at random
	 */
	public void setRandomQuestions(boolean random){
		randomQuestions = random;
	}

	/**
	 * Adds an answer to this Prompt. If a condition is specified, this answer will only be displayed to targets that meet it.
	 * @param answerText the answer string to add
	 * @param action the action to run when this answer is clicked
	 * @param condition the conditions on which to show this answer, or null to always show
	 */
	public void addAnswer(String answerText, String action, String condition){
		// Create list, if needed
		if(answers==null) answers = new ArrayList<Answer>();

		answers.add(new Answer(answerText, action, condition));
	}
	/**
	 * Adds an answer to this Prompt.
	 * @param answer the answer string to add
	 * @param action the action to run when this answer is clicked
	 */
	public void addAnswer(String answer, String action){
		addAnswer(answer, action, null);
	}
	/**
	 * Gets the answers in this Prompt.
	 * @return all answers (in the order they will be displayed), or null if answers were not set
	 */
	public List<Answer> getAnswers(){
		return answers;
	}
	/**
	 * Sets the answers for this prompt to the specified answer texts, actions, and conditions.
	 * Each list must be the same length. Any existing answers will be overwritten.
	 * @param answerTexts the text for each answer
	 * @param actions the action to run when each answer is clicked
	 * @param conditions the condition on which to show each answer, or null to always show
	 * @throws IllegalArgumentException if lists are not the same length
	 */
	private void setAnswers(List<String> answerTexts, List<String> actions, List<String> conditions){
		// Make sure all lists are the same length
		final int listLength = answerTexts.size();
		if(listLength!=actions.size() || (conditions!=null && !conditions.isEmpty() && listLength!=conditions.size())) throw new IllegalArgumentException("Invalid list length for prompt answers");

		// Create the answer list
		answers = new ArrayList<Answer>(listLength);
		for(int i = 0; i<listLength; i++){
			answers.add(new Answer(answerTexts.get(i), actions.get(i), (conditions==null || conditions.isEmpty()) ? null : conditions.get(i)));
		}
	}
	/**
	 * Sets the answers for this prompt to the specified answer texts and actions.
	 * Each list must be the same length. Any existing answers will be overwritten.
	 * @param answerTexts the text for each answer
	 * @param actions the action to run when each answer is clicked
	 * @throws IllegalArgumentException if lists are not the same length
	 */
	public void setAnswers(List<String> answers, List<String> actions){
		setAnswers(answers, actions, null);
	}


	//// Prompt Display methods
	/**
	 * Displays the questions of this prompt to a {@link CommandSender}.
	 * @param target the {@link CommandSender} to display to
	 */
	private void displayQuestions(CommandSender target){
		// If randomQuestion, pick one question line randomly
		if(randomQuestions){
			String questionLine = questions.get(ThreadLocalRandom.current().nextInt(questions.size()));
			target.sendMessage(questionLine);
		}
		// Otherwise, display each line of the question
		else for (String questionLine : questions) target.sendMessage(questionLine);
	}
	/**
	 * Displays the answers of this prompt to a CommandSender.
	 * @param sender the {@link CommandSender} to display to
	 */
	private void displayAnswers(CommandSender target){

		/** The answers visible to this target. */
		List<Answer> targetAnswers = answers;
		// If target is an entity, remove answers which they do not meet conditions for
		if(target instanceof Entity) answers.removeIf(answer -> !answer.visibleTo((Player)target));

		// Send action bar message, prompting target, if any answers have actions
		targetAnswers.forEach(answer -> {
			if(answer.getAction()!=null && !answer.getAction().isEmpty()) Utils.sendActionBar(target, "Choose an option");
		});

		// Store answers/actions for this sender
		int code = updateSenderAnswerList(target, targetAnswers);

		// Display the answers
		targetAnswers.forEach(answer -> {
			answer.sendAnswer(target, code);
		});
	}

	/**
	 * Displays this prompt to a {@link CommandSender}.
	 * @param target the {@link CommandSender} to display to
	 * @param prefix an optional prefix for all questions
	 * @deprecated the prefix stuff should be moved into a subclass, in Story
	 */
	public void display(CommandSender target, String prefix){

		// Make the prefix gray
		if(prefix!=null) prefix = ChatColor.GRAY + prefix + ChatColor.GRAY;

		displayQuestions(target);
		displayAnswers(target);
	}

	/**
	 * Displays this prompt to a {@link CommandSender}.
	 * @param sender the {@link CommandSender} to display to
	 */
	public void display(CommandSender sender){
		display(sender, "");
	}
}
