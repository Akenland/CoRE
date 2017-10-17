package com.KyleNecrowolf.RealmsCore;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Extras.AFKListener;
import com.KyleNecrowolf.RealmsCore.Extras.EntityCommands;
import com.KyleNecrowolf.RealmsCore.Extras.GameModeCommands;
import com.KyleNecrowolf.RealmsCore.Extras.InvCommands;
import com.KyleNecrowolf.RealmsCore.Extras.ItemCommands;
import com.KyleNecrowolf.RealmsCore.Extras.ListCommands;
import com.KyleNecrowolf.RealmsCore.Extras.MeCommands;
import com.KyleNecrowolf.RealmsCore.Extras.ModerationCommands;
import com.KyleNecrowolf.RealmsCore.Extras.MovementCommands;
import com.KyleNecrowolf.RealmsCore.Extras.MsgCommands;
import com.KyleNecrowolf.RealmsCore.Extras.NotifyCommands;
import com.KyleNecrowolf.RealmsCore.Extras.RemoveCommands;
import com.KyleNecrowolf.RealmsCore.Extras.ResourcePackCommands;
import com.KyleNecrowolf.RealmsCore.Extras.SignColorListener;
import com.KyleNecrowolf.RealmsCore.Extras.TimeWeatherCommands;
import com.KyleNecrowolf.RealmsCore.Extras.WolfiaListener;
import com.KyleNecrowolf.RealmsCore.Extras.WorldCommands;

import com.KyleNecrowolf.RealmsCore.Permissions.PermissionsCommands;

import com.KyleNecrowolf.RealmsCore.Player.PlayerCommands;
import com.KyleNecrowolf.RealmsCore.Player.PlayerListener;
import com.KyleNecrowolf.RealmsCore.Prompts.PromptActionListener;
import com.KyleNecrowolf.RealmsCore.Prompts.PromptCommands;

import com.KyleNecrowolf.RealmsCore.Realm.RealmCommands;

public final class Main extends JavaPlugin {
	
	public static JavaPlugin plugin;
	
	//// Plugin Enabled
	@Override
	public void onEnable(){
		plugin = this;


		//// Main Commands
		// Plugin
		this.getCommand("realmscore").setExecutor(new RealmsCoreCommands());

		// Player
		this.getCommand("player").setExecutor(new PlayerCommands());

		// Realm
		this.getCommand("realm").setExecutor(new RealmCommands());

		// Permissions
		this.getCommand("permissions").setExecutor(new PermissionsCommands());
		
		// Prompts
		this.getCommand("prompts").setExecutor(new PromptCommands());
		this.getCommand("help").setExecutor(new PromptCommands());


		//// Extra commands
		// Server utilities
		this.getCommand("world").setExecutor(new WorldCommands());
		// TODO plugins

		// Moderation commands
		this.getCommand("kick").setExecutor(new ModerationCommands());
		this.getCommand("ban").setExecutor(new ModerationCommands());
		this.getCommand("tempban").setExecutor(new ModerationCommands());
		this.getCommand("mute").setExecutor(new ModerationCommands());
		this.getCommand("unmute").setExecutor(new ModerationCommands());
		
		this.getCommand("inventory").setExecutor(new InvCommands());
		this.getCommand("enderchest").setExecutor(new InvCommands());

		// Server cheats
		this.getCommand("time").setExecutor(new TimeWeatherCommands());
		this.getCommand("weather").setExecutor(new TimeWeatherCommands());
		this.getCommand("entity").setExecutor(new EntityCommands());
		this.getCommand("remove").setExecutor(new RemoveCommands());
		this.getCommand("lowerlag").setExecutor(new RemoveCommands());

		// Player cheats
		this.getCommand("gamemode").setExecutor(new GameModeCommands());
		this.getCommand("fly").setExecutor(new MovementCommands());
		this.getCommand("speed").setExecutor(new MovementCommands());
		this.getCommand("item").setExecutor(new ItemCommands());

		// Other
		this.getCommand("list").setExecutor(new ListCommands());
		this.getCommand("notifyadmins").setExecutor(new NotifyCommands());
		this.getCommand("notifyall").setExecutor(new NotifyCommands());
		this.getCommand("afk").setExecutor(new AFKListener());
		this.getCommand("me").setExecutor(new MeCommands());
		this.getCommand("msg").setExecutor(new MsgCommands());
		this.getCommand("pack").setExecutor(new ResourcePackCommands());
		this.getCommand("bladeoflight").setExecutor(new WolfiaListener());


		//// Event Listeners
		// Register event listeners
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new PromptActionListener(), this);
		//if(ConfigValues.enableNpcPrompts) getServer().getPluginManager().registerEvents(new NPCListener(), this);
		getServer().getPluginManager().registerEvents(new ModerationCommands(), this);
		getServer().getPluginManager().registerEvents(new RealmCommands(), this);
		getServer().getPluginManager().registerEvents(new EntityCommands(), this);
		getServer().getPluginManager().registerEvents(new MovementCommands(), this);
		getServer().getPluginManager().registerEvents(new ResourcePackCommands(), this);
		getServer().getPluginManager().registerEvents(new SignColorListener(), this);
		if(ConfigValues.afkEnabled) getServer().getPluginManager().registerEvents(new AFKListener(), this);
		if(ConfigValues.enableWolfiaFeatures) getServer().getPluginManager().registerEvents(new WolfiaListener(), this);


		//// Default files
		// Core Config
		ConfigValues.saveDefaultConfig();

		// Realms file
		if(!new File(this.getDataFolder(),"realms.yml").exists()) this.saveResource("realms.yml", false);

		// Permissions files
		if(ConfigValues.permsEnabled){
			if(!new File(this.getDataFolder(),"playerperms.yml").exists()) this.saveResource("playerperms.yml", false);
			if(!new File(this.getDataFolder(),"permsets.yml").exists()) this.saveResource("permsets.yml", false);
		}

		// Example prompts
		if(!new File(this.getDataFolder(),"prompts\\example.yml").exists()) this.saveResource("prompts\\example.yml", false);
		if(!new File(this.getDataFolder(),"prompts\\help.yml").exists()) this.saveResource("prompts\\help.yml", false);
		if(!new File(this.getDataFolder(),"prompts\\motd.yml").exists()) this.saveResource("prompts\\motd.yml", false);

		// Resource packs file
		if(!new File(this.getDataFolder(),"packs.yml").exists()) this.saveResource("packs.yml", false);


		//// bStats Metrics
		Metrics metrics = new Metrics(this);
		// Config values
		metrics.addCustomChart(new Metrics.SimplePie("config_allowCommandBlockCommands", () -> ConfigValues.allowCommandBlocksCommands?"Allowed":"Blocked"));
		metrics.addCustomChart(new Metrics.SimplePie("config_allowConsoleCommands", () -> ConfigValues.allowConsoleCommands?"Allowed":"Blocked"));
		metrics.addCustomChart(new Metrics.SimplePie("config_allowRconCommands", () -> ConfigValues.allowRconCommands?"Allowed":"Blocked"));
		metrics.addCustomChart(new Metrics.SimplePie("config_multiCheckAdmins", () -> ConfigValues.multiCheckAdmins?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_permsEnabled", () -> ConfigValues.permsEnabled?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_lockoutNewIPs", () -> ConfigValues.lockoutNewIPs?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_formatChat", () -> ConfigValues.formatChat?"Enabled":"Disabled"));
		// Realm config values
		metrics.addCustomChart(new Metrics.SimplePie("config_playersForNewRealm", () -> ConfigValues.playersForNewRealm+" players"));
		metrics.addCustomChart(new Metrics.SimplePie("config_playersCreateNewRealm", () -> ConfigValues.playersCreateNewRealm?"New realms":"Sub-realms only"));
		metrics.addCustomChart(new Metrics.SimplePie("config_officersCreateSubRealms", () -> ConfigValues.officersCreateSubRealms?"Realm officers only":"Anyone"));
		metrics.addCustomChart(new Metrics.SimplePie("config_realmLoadSource", () -> ConfigValues.realmLoadSource));
		metrics.addCustomChart(new Metrics.SimplePie("config_useScoreboard", () -> ConfigValues.useScoreboard?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_blockFriendlyFire", () -> ConfigValues.blockFriendlyFire?"Friendly-fire blocked":"Friendly-fire allowed"));
		metrics.addCustomChart(new Metrics.SimplePie("config_seeFriendlyInvisibles", () -> ConfigValues.seeFriendlyInvisibles?"Members see invisibles":"Invisible to everyone"));
		metrics.addCustomChart(new Metrics.SimplePie("config_nameTagVisibility", () -> ConfigValues.nameTagVisibility));
		metrics.addCustomChart(new Metrics.SimplePie("config_deathMessageVisibility", () -> ConfigValues.deathMessageVisibility));
		metrics.addCustomChart(new Metrics.SimplePie("config_collisionRule", () -> ConfigValues.collisionRule));
		// Online admins
		metrics.addCustomChart(new Metrics.SingleLineChart("players_admins", () -> Utils.getOnlineAdmins().size()));
	}
	
	/*
	//// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		String command = cmd.getName().toLowerCase();

		// Check permissions if command is from a player
		if(sender instanceof Player && !sender.hasPermission("realms."+command)){
			return Error.NO_PERMISSION.displayChat(sender);
		}

		
		switch(command){
		// Hello command
		case "realmscore":
			sender.sendMessage(Utils.messageText+"RealmsCore "+getDescription().getVersion()+" by Kyle Necrowolf");
			return true;


		// List online admins
		case "listadmins":
			sender.sendMessage(Utils.messageText+"Online admins: "+Utils.listOnlineAdmins());
			return true;

		// Notify admins
		case "notifyadmins":
			Utils.notifyAdmins(String.join(" ", args));
			return true;
			
		
		// Player info
		case "playerinfo":
			if(args.length==1){
				OfflinePlayer player = Utils.getPlayer(args[0], true);
				if(player==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
				return new PlayerData(player).displayInfo(sender);
			}

			
		// Set nickname
		case "setnick":
			return Utils.setPlayerData(sender, "displayname", args);
		// Set title
		case "settitle":
			return Utils.setPlayerData(sender, "title", args);
		// Set realm
		case "setrealm":
			return Utils.setPlayerData(sender, "realm", args);
		
		
		// Realm command
		case "realm":
			if(args.length==2 && args[0].equalsIgnoreCase("info")) return new Realm(args[1]).displayInfo(sender);
			return Error.INVALID_ARGS.displayActionBar(sender);
		
		
		Reload chat formats
		case "chatreload":
			EventListener.chatFormats.clear();
			Utils.sendActionBar(sender, "Reloaded chat formats");
			return true;

			
		//// OLD PERMISSION COMMANDS
		
		// Display player sets
		case "playersets":
			if(args.length == 0) return OldPermissions.displayPlayerSets(sender, sender.getName());
			if(args.length == 1 && sender.hasPermission("realms.playersets.others")) return OldPermissions.displayPlayerSets(sender, args[0]);
			return Error.INVALID_ARGS.displayActionBar(sender);
			
		// Display set perms
		case "permset":
			if(args.length == 1){return OldPermissions.displaySetPerms(sender, args[0]);}
			return Error.INVALID_ARGS.displayActionBar(sender);
			
		// Change permission set
		case "changeset":
			return OldPermissions.changePlayerSet(sender, args);
			
		// Apply temp perm set
		case "tempset":
			return OldPermissions.changePlayerSetTemp(sender, args);
		
		// Apply default/utility/cheat set
		case "default":
			//sender.sendMessage(Common.infoText+"Switched to default permissions.");
			return OldPermissions.changePlayerSetByType(sender, "default");
		case "utility":
			//sender.sendMessage(Common.infoText+"Switched to utility permissions.");
			return OldPermissions.changePlayerSetByType(sender, "utility");
		case "cheat":
			//sender.sendMessage(Common.infoText+"Switched to cheat permissions. Use this only when needed.");
			return OldPermissions.changePlayerSetByType(sender, "cheat");
		
		// View perms on a player
		case "viewperms":
			return OldPermissions.displayPlayerPerms(sender, args);
			
		// Revoke perms from a player
		case "revokeperms":
			if(args.length == 1){return OldPermissions.revokePlayerPerms(args[0]);}
			return Error.INVALID_ARGS.displayActionBar(sender);
		


		// OLD Gamemode changing
		
		case "gamemode":
			return EssentialCommands.changeGameMode(sender, args);
		case "survival":
			return EssentialCommands.changeGameMode(sender, new String[] {"survival"});
		case "creative":
			return EssentialCommands.changeGameMode(sender, new String[] {"creative"});
		case "adventure":
			return EssentialCommands.changeGameMode(sender, new String[] {"adventure"});
		case "spectator":
			return EssentialCommands.changeGameMode(sender, new String[] {"spectator"});
		
			
			
		// World commands
		case "world":
			return EssentialCommands.onWorldCommand(sender, args);
		
		
		// Help command
		case "help":
			if(args.length==0 && sender instanceof Player) new Prompt("help","menu").display((Player) sender);
			if(args.length==1 && sender instanceof Player) new Prompt("help", args[0]).display((Player) sender);
			return true;


		// Command not set up
		default:
			sender.sendMessage(Utils.errorText+"This command is not set up in RealmsCore. Yell at Kyle if you want it fixed.");
			return false;
		}
	}
	*/
}
