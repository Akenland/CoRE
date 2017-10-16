package com.KyleNecrowolf.RealmsCore;

import org.bukkit.ChatColor;
import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;

public final class ConfigValues {

    //// Config file
    private static ConfigAccessor config = new ConfigAccessor("coreconfig.yml");
    //private static final FileConfiguration config.getConfig() = config.getConfig();

    //// Saving default config file
    static void saveDefaultConfig(){
        config.saveDefaultConfig();
    }
    //// Reloading config
    static void reloadConfig(){
        config.reloadConfig();
    }


    ////// General
    // Whether Wolfia specific functionality is enabled - Blade of Light - default FALSE
    public static boolean enableWolfiaFeatures = config.getConfig().getBoolean("general.wolfia");

    // TEMPORARY - Whether Citizens integration for NPC prompts is enabled - default FALSE
    public static boolean enableNpcPrompts = config.getConfig().getBoolean("general.citizens");

    // Whether /list shows non-admins - default TRUE
    public static boolean listAllPlayers = config.getConfig().getBoolean("general.list-all-players");

    // Whether a list of online admins should be shown to joining players - default TRUE
    public static boolean showAdminListOnJoin = config.getConfig().getBoolean("general.show-admins-on-join");
    // Whether a MOTD prompt should be shown to joining players - default TRUE
    public static boolean showMotdPromptOnJoin = config.getConfig().getBoolean("general.show-motd-on-join");

    // Whether to search displaynames in commands that take player names - default TRUE
    public static boolean searchDisplayNames = config.getConfig().getBoolean("general.search-player-displaynames");
    // Whether to search offline players in commands that take player names - default TRUE
    public static boolean searchOfflineNames = config.getConfig().getBoolean("general.search-offline-players");



    ////// Appearance
    //// Colours
    // Normal message colour - default GRAY
    public static ChatColor messageColor = ChatColor.valueOf(config.getConfig().getString("appearance.colors.message"));
    // Info message colour - default DARK_GRAY
    public static ChatColor infoColor = ChatColor.valueOf(config.getConfig().getString("appearance.colors.info"));
    // Error message colour - default RED
    public static ChatColor errorColor = ChatColor.valueOf(config.getConfig().getString("appearance.colors.error"));
    // Admin colour - default DARK_PURPLE
    public static ChatColor adminColor = ChatColor.valueOf(config.getConfig().getString("appearance.colors.admin"));
    // Join/quit message colour - default YELLOW
    public static ChatColor joinQuitColor = ChatColor.valueOf(config.getConfig().getString("appearance.colors.join-quit"));

    //// Text
    // Admin prefix - default "+"
    public static String adminPrefix = adminColor + ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("appearance.text.admin-prefix"));
    // Admin prefix - default "+"
    public static String adminName = ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("appearance.text.admin-name"));

    // Join message - default "joined the game"
    public static String joinMessage = ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("appearance.text.join-message"));
    // Quit message - default "left the game"
    public static String quitMessage = ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("appearance.text.quit-message"));
    // First join message - default "Welcome to the server!"
    public static String firstJoinMessage = ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("appearance.text.first-join-message"));

    // No permission message - default "You can't use this command! Ask an %sAdmin%s for help."
    public static String noPermMessage = ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("appearance.text.no-permission-message"));


    // Whether action bar messages should be used - default TRUE
    public static boolean useActionBarMessages = config.getConfig().getBoolean("appearance.use-actionbar-messages");



    ////// Permissions
    // Whether permissions is enabled - default TRUE
    public static final boolean permsEnabled = config.getConfig().getBoolean("permissions.enabled");

    // Whether to multi-check admins - default TRUE
    public static final boolean multiCheckAdmins = config.getConfig().getBoolean("permissions.multi-check-players");
    // Whether the console can run permissions commands - default TRUE
    public static final boolean allowConsoleCommands = config.getConfig().getBoolean("permissions.allow-console-admin-commands");
    // Whether command blocks can run permissions commands - default FALSE
    public static final boolean allowCommandBlocksCommands = config.getConfig().getBoolean("permissions.allow-command-block-admin-commands");
    // Whether remote consoles (rcon) can run permissions commands - default FALSE
    public static final boolean allowRconCommands = config.getConfig().getBoolean("permissions.allow-rcon-admin-commands");

    // Whether to automatically lockout players who log in from unrecognized IPs
    public static boolean lockoutNewIPs = config.getConfig().getBoolean("permissions.lockout-unrecognized-ips");



    ////// Players
    // Whether RealmsCore should manage chat formatting - default TRUE
    public static boolean formatChat = config.getConfig().getBoolean("players.use-chat-formatting");

    //// AFK
    // Whether players can be marked AFK - default TRUE
    public static final boolean afkEnabled = config.getConfig().getBoolean("players.afk-enabled");
    // How many seconds before players are marked AFK automatically - default 180 seconds
    public static double afkTime = config.getConfig().getDouble("players.time-before-afk");



    ////// Realms
    // Where realms are loaded from - default "playerdata"
    public static String realmLoadSource = config.getConfig().getString("realms.load-realms-from");
   
    //// Scoreboard
    // Whether scoreboard integration is enabled - default TRUE
    public static final boolean useScoreboard = config.getConfig().getBoolean("realms.use-minecraft-scoreboard");
    
    // Whether friendly-fire is blocked - default FALSE
    public static final boolean blockFriendlyFire = config.getConfig().getBoolean("realms.scoreboard-options.block-friendly-fire");
    // Whether realm members see friendly invisibles - default FALSE
    public static final boolean seeFriendlyInvisibles = config.getConfig().getBoolean("realms.scoreboard-options.see-friendly-invisibles");
    // Whether name tags are visible - default "always"
    public static final String nameTagVisibility = config.getConfig().getString("realms.scoreboard-options.name-tag-visibility");
    // Whether death messages are visible - default "always"
    public static final String deathMessageVisibility = config.getConfig().getString("realms.scoreboard-options.death-message-visibility");
    // Whether players collide - default "always"
    public static final String collisionRule = config.getConfig().getString("realms.scoreboard-options.collision-rule");

    //// Creation
    // Players needed to start a realm - default 1
    public static final int playersForNewRealm = config.getConfig().getInt("realms.players-for-new-realm");
    // Whether players can create new realms, instead of just sub-realms - default TRUE
    public static final boolean playersCreateNewRealm = config.getConfig().getBoolean("realms.players-create-new-realms");
    // Whether only officers can create new sub-realms - default FALSE
    public static final boolean officersCreateSubRealms = config.getConfig().getBoolean("realms.officers-create-sub-realms");
}