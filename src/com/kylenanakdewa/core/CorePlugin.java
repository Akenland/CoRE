package com.kylenanakdewa.core;

import java.io.File;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.Extras.AFKListener;
import com.kylenanakdewa.core.Extras.EntityCommands;
import com.kylenanakdewa.core.Extras.GameModeCommands;
import com.kylenanakdewa.core.Extras.InvCommands;
import com.kylenanakdewa.core.Extras.ItemCommands;
import com.kylenanakdewa.core.Extras.ListCommands;
import com.kylenanakdewa.core.Extras.MeCommands;
import com.kylenanakdewa.core.Extras.ModerationCommands;
import com.kylenanakdewa.core.Extras.MovementCommands;
import com.kylenanakdewa.core.Extras.MsgCommands;
import com.kylenanakdewa.core.Extras.NotifyCommands;
import com.kylenanakdewa.core.Extras.RemoveCommands;
import com.kylenanakdewa.core.Extras.ResourcePackCommands;
import com.kylenanakdewa.core.Extras.SignColorListener;
import com.kylenanakdewa.core.Extras.TimeWeatherCommands;
import com.kylenanakdewa.core.Extras.WolfiaListener;
import com.kylenanakdewa.core.Extras.WorldCommands;

import com.kylenanakdewa.core.Permissions.PermissionsCommands;

import com.kylenanakdewa.core.characters.players.PlayerCharacterManager;
import com.kylenanakdewa.core.common.prompts.PromptActionListener;
import com.kylenanakdewa.core.common.prompts.PromptCommands;
import com.kylenanakdewa.core.realms.RealmCommandExecutor;
import com.kylenanakdewa.core.realms.RealmProvider;
import com.kylenanakdewa.core.realms.composite.CompositeRealmProvider;
import com.kylenanakdewa.core.realms.configuration.CoreRealmProvider;
import com.kylenanakdewa.core.realms.scoreboard.ScoreboardRealmProvider;

/**
 * Project CoRE for Bukkit
 * <p>
 * A roleplaying framework and server essentials, for small/medium survival and roleplay servers.
 * @author Kyle Nanakdewa
 */
public final class CorePlugin extends JavaPlugin {

	/** This plugin's instance on the running Bukkit server. */
	@Deprecated
	public static CorePlugin plugin;

	/** The RealmProvider in use on this instance of Project CoRE. */
	private RealmProvider realmProvider;


	@Override
	public void onEnable(){
		plugin = this;

		reload();

		//// bStats Metrics
		startMetrics();
	}

	@Override
	public void onDisable(){
		// Cancel all tasks
		getServer().getScheduler().cancelTasks(this);
		// Unregister listeners
		HandlerList.unregisterAll(this);
	}

	/**
	 * Reloads Project CoRE.
	 */
	public void reload(){
		onDisable();

		saveDefaultConfigs();
		CoreConfig.reloadConfig();

		// Set up Realm Providers - delayed so it runs after server startup
		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> setupCompositeRealmProvider());

		// Set up Player Characters
		setupPlayerCharacterManager();

		// Register remaining commands
		registerCommands();

		// Register remaining listeners
		registerListeners();
	}


	/**
	 * Registers commands for Project CoRE.
	 */
	private void registerCommands(){
		// Plugin
		getCommand("core").setExecutor(new CoreCommands(this));

		// Player - moved to setupPlayerCharacterManager()

		// Realm - moved to setupCompositeRealmProvider()

		// Permissions
		this.getCommand("permissions").setExecutor(new PermissionsCommands());

		// Prompts
		{
			PromptCommands promptCommands = new PromptCommands(this);
			getCommand("prompts").setExecutor(promptCommands);
			getCommand("help").setExecutor(promptCommands);
		}


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
	}

	/**
	 * Registers listeners for Project CoRE.
	 */
	private void registerListeners(){
		
		getServer().getPluginManager().registerEvents(new PromptActionListener(), this);
		getServer().getPluginManager().registerEvents(new ModerationCommands(), this);
		getServer().getPluginManager().registerEvents(new EntityCommands(), this);
		getServer().getPluginManager().registerEvents(new MovementCommands(), this);
		getServer().getPluginManager().registerEvents(new ResourcePackCommands(), this);
		getServer().getPluginManager().registerEvents(new SignColorListener(), this);
		if(CoreConfig.afkEnabled) getServer().getPluginManager().registerEvents(new AFKListener(), this);
		if(CoreConfig.enableWolfiaFeatures) getServer().getPluginManager().registerEvents(new WolfiaListener(), this);
	}

	/**
	 * Saves default config files for Project CoRE.
	 */
	private void saveDefaultConfigs(){
		// Core Config
		CoreConfig.saveDefaultConfig();

		// Realms file
		if(!new File(this.getDataFolder(),"realms.yml").exists()) this.saveResource("realms.yml", false);

		// Permissions files
		if(CoreConfig.permsEnabled){
			if(!new File(this.getDataFolder(),"playerperms.yml").exists()) this.saveResource("playerperms.yml", false);
			if(!new File(this.getDataFolder(),"permsets.yml").exists()) this.saveResource("permsets.yml", false);
		}

		// Example prompts
		if(!new File(this.getDataFolder(),"prompts\\example.yml").exists()) this.saveResource("prompts\\example.yml", false);
		if(!new File(this.getDataFolder(),"prompts\\help.yml").exists()) this.saveResource("prompts\\help.yml", false);
		if(!new File(this.getDataFolder(),"prompts\\motd.yml").exists()) this.saveResource("prompts\\motd.yml", false);

		// Resource packs file
		if(!new File(this.getDataFolder(),"packs.yml").exists()) this.saveResource("packs.yml", false);
	}

	/**
	 * Starts bStats Metrics for Project CoRE.
	 */
	private void startMetrics(){
		Metrics metrics = new Metrics(this);
		// Config values
		metrics.addCustomChart(new Metrics.SimplePie("config_allowCommandBlockCommands", () -> CoreConfig.allowCommandBlocksCommands?"Allowed":"Blocked"));
		metrics.addCustomChart(new Metrics.SimplePie("config_allowConsoleCommands", () -> CoreConfig.allowConsoleCommands?"Allowed":"Blocked"));
		metrics.addCustomChart(new Metrics.SimplePie("config_allowRconCommands", () -> CoreConfig.allowRconCommands?"Allowed":"Blocked"));
		metrics.addCustomChart(new Metrics.SimplePie("config_multiCheckAdmins", () -> CoreConfig.multiCheckAdmins?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_permsEnabled", () -> CoreConfig.permsEnabled?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_lockoutNewIPs", () -> CoreConfig.lockoutNewIPs?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_formatChat", () -> CoreConfig.formatChat?"Enabled":"Disabled"));
		// Realm config values
		metrics.addCustomChart(new Metrics.SimplePie("config_playersForNewRealm", () -> CoreConfig.playersForNewRealm+" players"));
		metrics.addCustomChart(new Metrics.SimplePie("config_playersCreateNewRealm", () -> CoreConfig.playersCreateNewRealm?"New realms":"Sub-realms only"));
		metrics.addCustomChart(new Metrics.SimplePie("config_officersCreateSubRealms", () -> CoreConfig.officersCreateSubRealms?"Realm officers only":"Anyone"));
		metrics.addCustomChart(new Metrics.SimplePie("config_useScoreboard", () -> CoreConfig.useScoreboard?"Enabled":"Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("config_blockFriendlyFire", () -> CoreConfig.blockFriendlyFire?"Friendly-fire blocked":"Friendly-fire allowed"));
		metrics.addCustomChart(new Metrics.SimplePie("config_seeFriendlyInvisibles", () -> CoreConfig.seeFriendlyInvisibles?"Members see invisibles":"Invisible to everyone"));
		metrics.addCustomChart(new Metrics.SimplePie("config_nameTagVisibility", () -> CoreConfig.nameTagVisibility));
		metrics.addCustomChart(new Metrics.SimplePie("config_deathMessageVisibility", () -> CoreConfig.deathMessageVisibility));
		metrics.addCustomChart(new Metrics.SimplePie("config_collisionRule", () -> CoreConfig.collisionRule));
		// Online admins
		metrics.addCustomChart(new Metrics.SingleLineChart("players_admins", () -> Utils.getOnlineAdmins().size()));
	}


	/**
	 * Gets the active RealmProvider for this instance of Project CoRE.
	 * The RealmProvider is responsible for managing all Realms for this plugin.
	 * <p>
	 * NOTE: The active provider will change if the plugin is reloaded. For this reason, avoid storing the returned provider.
	 * @return the current RealmProvider
	 */
	public RealmProvider getRealmProvider(){
		return realmProvider;
	}
	/**
	 * Gets the active RealmProvider for this instance of Project CoRE.
	 * The RealmProvider is responsible for managing all Realms for this plugin.
	 * <p>
	 * NOTE: The active provider will change if the plugin is reloaded. For this reason, avoid storing the returned provider.
	 * @return the current RealmProvider
	 */
	public static RealmProvider getServerRealmProvider(){
		return plugin.getRealmProvider();
	}

	/**
	 * Creates a CompositeRealmProvider for this instance of Project CoRE.
	 * The active providers are retrieved from the config.
	 * <p>
	 * Also registers realm commands for the provider.
	 * <p>
	 * Do not call until all plugins have loaded! Otherwise, the providers might not be registered, and exceptions will be thrown.
	 */
	private void setupCompositeRealmProvider(){
		// Register CoRE's included providers
		CompositeRealmProvider.registerProvider("scoreboard", new ScoreboardRealmProvider(getServer().getScoreboardManager().getMainScoreboard()));
		CompositeRealmProvider.registerProvider("core", new CoreRealmProvider(this));


		CompositeRealmProvider compositeProvider = new CompositeRealmProvider();
		CoreConfig.realmLoadSource.forEach(activeProvider -> compositeProvider.activateProvider(activeProvider));
		realmProvider = compositeProvider;

		// Register commands and listener
		getCommand("realm").setExecutor(new RealmCommandExecutor(realmProvider, this));
	}


	/**
	 * Creates a PlayerCharacterManager for this instance of Project CoRE.
	 * <p>
	 * Automatically registers commands and listeners.
	 */
	private void setupPlayerCharacterManager(){
		// Register commands and listener
		getCommand("player").setExecutor(new PlayerCharacterManager(this));
	}
}