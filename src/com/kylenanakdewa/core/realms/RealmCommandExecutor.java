package com.kylenanakdewa.core.realms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.core.common.prompts.PromptActionEvent;

/**
 * Handles execution of commands for a RealmProvider.
 * @author Kyle Nanakdewa
 */
public final class RealmCommandExecutor implements TabExecutor, Listener {

	/** The RealmProvider that this command handler is for. */
	private final RealmProvider provider;

	/**
	 * Creates a command handler for the specified RealmProvider.
	 * Also registers a listener for prompt actions.
	 * @param provider the provider containing the realms to be managed by this command handler
	 * @param plugin the plugin that owns the provider
	 */
	public RealmCommandExecutor(RealmProvider provider, Plugin plugin){
		this.provider = provider;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		PlayerCharacter player = sender instanceof Player ? PlayerCharacter.getCharacter((Player)sender) : null;
		Realm realm = args.length>=1 ? provider.getRealm(args[0]) : null;

		// Check for valid realm
		if(realm==null) return Error.REALM_NOT_FOUND.displayActionBar(sender);


		// One arg - view realm info
		if(args.length==1){
			if(!sender.hasPermission("core.realm")) return Error.NO_PERMISSION.displayChat(sender);

			// Create the prompt
			Prompt prompt = new Prompt();
			prompt.addQuestion(CommonColors.INFO+"--- Realm Info: "+realm.getName()+CommonColors.INFO+" ---");
			if(realm.getTagline()!=null) prompt.addQuestion(" "+ChatColor.GRAY+ChatColor.ITALIC.toString()+realm.getTagline());
			if(realm.getParentRealm()!=null && (!CoreConfig.enableWolfiaFeatures || realm.getParentRealm()!=realm.getTopParentRealm())) prompt.addQuestion("- Faction of the "+realm.getParentRealm().getName());
			if(CoreConfig.enableWolfiaFeatures && realm.getTopParentRealm()!=null) prompt.addQuestion("- Located in "+realm.getTopParentRealm().getName());

			Collection<PlayerCharacter> members = realm.getOnlineCharacters();
			if(!members.isEmpty()){
				prompt.addQuestion(CommonColors.INFO+"-- Online players --");
				members.forEach(member -> prompt.addAnswer(member.getFormattedName(), "command_player "+member.getUsername()));
			}

			prompt.display(sender);
			return true;
		}


		// Setting data
		if(args.length>3 && args[1].equalsIgnoreCase("set")){
			if(!sender.hasPermission("core.realm.edit")) return Error.NO_PERMISSION.displayChat(sender);

			// If sender does not have edit access, return error
			if(sender instanceof Player && !provider.isOfficer(player))
				return Error.NO_PERMISSION.displayChat(sender);

			String key = args[2];

			// Merge all remaining args into a single string
			List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
			for(int i=0; i<3; i++) lastArgs.remove(0);
			String data = ChatColor.translateAlternateColorCodes('&', String.join(" ", lastArgs));
		
			// Store data in realm file
			switch(key){
				case "color":
					realm.setColor(ChatColor.valueOf(data));
					realm.getOnlineCharacters().forEach(character -> character.updateDisplayName());
					sender.sendMessage(CommonColors.MESSAGE+"The official color of the "+realm.getName()+CommonColors.MESSAGE+" is now "+realm.getColor()+realm.getColor().name());
					return true;
				case "fullname":
					String oldName = realm.getName();
					realm.setName(data);
					sender.sendMessage(CommonColors.MESSAGE+"The official name of "+oldName+" is now "+realm.getName());
					return true;
				case "tagline":
					realm.setTagline(data);
					sender.sendMessage(CommonColors.MESSAGE+"The tagline of "+realm.getName()+CommonColors.MESSAGE+" is now "+ChatColor.GRAY+ChatColor.ITALIC.toString()+realm.getTagline());
					return true;
				case "parent":
					if(!sender.hasPermission("core.realm.globalofficer")) return Error.NO_PERMISSION.displayChat(sender);
					Realm parent = provider.getRealm(data);
					if(parent==null) return Error.REALM_NOT_FOUND.displayActionBar(sender);
					realm.setParentRealm(parent);
					realm.getOnlineCharacters().forEach(character -> character.updateDisplayName());
					sender.sendMessage(CommonColors.MESSAGE+"The "+realm.getName()+CommonColors.MESSAGE+" is now a faction of the "+parent.getName());
					return true;
				default:
					return Error.INVALID_ARGS.displayActionBar(sender);
			}
		}


		// Adding/removing officers
		if(args.length>2 && (args[1].equalsIgnoreCase("addofficer") || args[1].equalsIgnoreCase("removeofficer"))){
			if(!sender.hasPermission("core.realm.officers")) return Error.NO_PERMISSION.displayChat(sender);

			// If sender does not have edit access, return error
			if(sender instanceof Player && !provider.isOfficer(player))
				return Error.NO_PERMISSION.displayChat(sender);


			// Check if adding or removing
			final boolean isOfficer = args[1].equalsIgnoreCase("addofficer") ? true : false;
			final String message = isOfficer ? "now" : "no longer";

			// Get players
			List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
			lastArgs.remove(1); lastArgs.remove(0);
			for(String arg : lastArgs){
				OfflinePlayer target = Utils.getPlayer(arg, true);
				// If player is valid, add as realm officer
				if(target!=null){
					PlayerCharacter character = PlayerCharacter.getCharacter(target);
					if(provider.getCharacterRealm(character).equals(realm)){
						provider.setOfficer(character, isOfficer);
						sender.sendMessage(character.getName()+CommonColors.MESSAGE+" can "+message+" edit the "+realm.getName()+CommonColors.MESSAGE+" and all sub-realms.");
						if(target.isOnline()) ((Player)target).sendMessage("You can "+message+" edit the "+realm.getName()+"!");
					}
				}
			}

			return true;
		}


		// Setting member titles
		if(args.length>3 && args[1].equalsIgnoreCase("title")){
			if(!sender.hasPermission("core.realm.edit")) return Error.NO_PERMISSION.displayChat(sender);

			// If sender does not have edit access, return error
			if(sender instanceof Player && !provider.isOfficer(player))
				return Error.NO_PERMISSION.displayChat(sender);


			// Get player
			OfflinePlayer target = Utils.getPlayer(args[2], true);
			if(target==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
			PlayerCharacter character = PlayerCharacter.getCharacter(target);
			if(!provider.getCharacterRealm(character).equals(realm)) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

			// Merge all remaining args into a single string
			List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
			for(int i=0; i<3; i++) lastArgs.remove(0);
			String title = ChatColor.translateAlternateColorCodes('&', String.join(" ", lastArgs));

			// Set character's title
			character.setTitle(title);

			sender.sendMessage(character.getName()+CommonColors.MESSAGE+"'s title is now "+character.getTitle());
			if(target.isOnline()) ((Player)target).sendMessage("Your title is now "+character.getTitle());
			return true;
		}


		// Inviting members
		if(args.length>2 && args[1].equalsIgnoreCase("invite")){
			if(!sender.hasPermission("core.realm.players")) return Error.NO_PERMISSION.displayChat(sender);

			// If sender does not have edit access, return error
			if(sender instanceof Player && !provider.isOfficer(player)) return Error.NO_PERMISSION.displayChat(sender);


			// Get players
			List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
			lastArgs.remove(1); lastArgs.remove(0);
			for(String arg : lastArgs){
				Player target = Utils.getPlayer(arg);
				// If player is valid, invite to realm
				if(target!=null){
					// Prepare prompt
					Prompt prompt = new Prompt();
					prompt.addQuestion(CommonColors.INFO+"--- Realm Invitation ---");
					String senderName = sender instanceof Player ? player.getFormattedName()+ChatColor.RESET+" has invited you" : "You have been invited";
					String tagline = realm.getTagline()!=null ? " - "+ChatColor.GRAY+ChatColor.ITALIC+realm.getTagline() : "";
					prompt.addQuestion("- "+senderName+" to join the "+realm.getName()+tagline);
					if(CoreConfig.enableWolfiaFeatures && realm.getTopParentRealm()!=null) prompt.addQuestion("- Located in the "+realm.getTopParentRealm().getName());
					Realm oldRealm = provider.getCharacterRealm(PlayerCharacter.getCharacter(target));
					String hasRealmString = (oldRealm!=null) ? ", and leave the "+oldRealm.getName()+ChatColor.RESET+" behind." : ".";
					prompt.addQuestion("- By pledging allegiance, you agree to become a loyal member of the "+realm.getName()+ChatColor.RESET+hasRealmString);
					prompt.addAnswer("Pledge allegiance to the "+realm.getName()+CommonColors.MESSAGE+" and join", "realm_join_"+realm.getIdentifier());
					prompt.addAnswer("Decline invitation", "realm_declineinvite_"+sender.getName());
					prompt.display(target);

					sender.sendMessage(target.getDisplayName()+CommonColors.MESSAGE+" was invited to the "+realm.getName()+CommonColors.MESSAGE+".");
				}
			}

			return true;
		}


		// Kick members
		if(args.length>2 && args[1].equalsIgnoreCase("kick")){
			if(!sender.hasPermission("core.realm.players")) return Error.NO_PERMISSION.displayChat(sender);
			
			// If sender does not have edit access, return error
			if(sender instanceof Player && !provider.isOfficer(player)) return Error.NO_PERMISSION.displayChat(sender);


			// Get players
			List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
			lastArgs.remove(1); lastArgs.remove(0);
			for(String arg : lastArgs){
				OfflinePlayer target = Utils.getPlayer(arg, true);
				// If player is valid, kick from realm
				if(player!=null){
					PlayerCharacter character = PlayerCharacter.getCharacter(target);
					if(provider.getCharacterRealm(character).equals(realm)){
						character.setRealm(null);
						sender.sendMessage(character.getName()+CommonColors.MESSAGE+" is no longer a member of the "+realm.getName()+CommonColors.MESSAGE+".");
					}
				}
			}

			return true;
		}


		return Error.INVALID_ARGS.displayChat(sender);
	}


	//// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		if(args.length==1){
			// If admin, return all realms
			if(sender.hasPermission("core.admin")){
				List<String> completions = new ArrayList<String>();
				provider.getAllRealms().forEach(realm -> completions.add(realm.getIdentifier()));
				return completions;
			}
			// Otherwise, return own realm, and all child realms
			else if(sender instanceof Player){
				Realm playerRealm = provider.getCharacterRealm(PlayerCharacter.getCharacter((Player) sender));
				if(playerRealm==null) return Arrays.asList("");
				List<String> completions = new ArrayList<String>(Arrays.asList(playerRealm.getIdentifier()));
				playerRealm.getChildRealms().forEach(realm -> completions.add(realm.getIdentifier()));
				return completions;
			}
		}

		if(args.length==2) return Arrays.asList("set", "addofficer", "removeofficer", "invite", "kick", "title");

		if(args.length==3 && args[1].equalsIgnoreCase("set")) return Arrays.asList("color","fullname","tagline","parent");
		if(args.length>=4 && args[1].equalsIgnoreCase("set")){
			if(args[2].equalsIgnoreCase("color")){
				List<String> completions = new ArrayList<String>();
				for(ChatColor color : ChatColor.values()) completions.add(color.name());
				return completions;
			}
			else return Arrays.asList("");
		}

		return null;
	}


	//// Event listener
	@EventHandler
	public void onRealmAction(PromptActionEvent event){
		if(event.isType("realm")){
			String[] splitAction = event.getAction().split("_", 2);
			Realm realm = provider.getRealm(splitAction[1]);
			switch(splitAction[0]){
				// Join faction (by invite)
				case "join":
					PlayerCharacter character = PlayerCharacter.getCharacter(event.getPlayer());
					realm.getOnlinePlayers().forEach(player -> player.sendMessage(character.getName()+" is now a member of the "+realm.getName()));
					character.setRealm(realm);
					event.getPlayer().sendMessage(CommonColors.MESSAGE+"You are now a member of the "+realm.getName());
					return;
				// Decline invite
				case "declineinvite":
					event.getPlayer().sendMessage(CommonColors.MESSAGE+"Invitation declined.");
					Player player = Bukkit.getPlayer(splitAction[1]);
					if(player!=null) player.sendMessage(event.getPlayer().getDisplayName()+CommonColors.MESSAGE+" declined your invitation.");
					return;
				default:
					return;
			}
		}
	}
}