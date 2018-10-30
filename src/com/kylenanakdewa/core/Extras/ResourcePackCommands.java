package com.kylenanakdewa.core.Extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.ConfigAccessor;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.prompts.Prompt;

public final class ResourcePackCommands implements TabExecutor, Listener {

    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Check permissions
        if(!sender.hasPermission("core.pack")) return Error.NO_PERMISSION.displayChat(sender);

        // Check args length
        if(args.length==0){
            displayPacks(sender);
            return true;
        }

        String packName = args[0];

        // Get target player
        final Player targetPlayer = (args.length>=2 && sender.hasPermission("core.pack.others")) ? Utils.getPlayer(args[1]) : (sender instanceof Player ? (Player)sender : null);
        if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

        // Check if setting default
        for(String arg : args){
            if(arg.equalsIgnoreCase("setdefault")){
                setPlayerDefaultPack(targetPlayer, packName);
                Utils.sendActionBar(sender, "Default pack set to "+packName);
                return true;
            }
        }

        // Send the pack
        sendResourcePack(targetPlayer, packName);
        // This message won't be visible when sending pack to self, as the downloading message will immediately replace it
        Utils.sendActionBar(sender, "Sending resource pack "+packName+" to "+targetPlayer.getDisplayName());
        return true;
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // If first arg, return list of packs
        if(args.length==1 && sender.hasPermission("core.pack")) return new ArrayList<String>(getResourcePacks());

        return null;
    }


    //// Join listener
    @EventHandler(priority = EventPriority.MONITOR)
    public void sendResourcePackOnJoin(PlayerJoinEvent event){
        String packName = getPlayerDefaultPack(event.getPlayer());
        if(packName!=null) sendResourcePack(event.getPlayer(), packName);
    }

    //// Pack status listener
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPackStatusChange(PlayerResourcePackStatusEvent event){
        // Send messages in response to pack status
        switch(event.getStatus()){
            case ACCEPTED:
                Utils.sendActionBar(event.getPlayer(), "Downloading resource pack...");
                break;
            case DECLINED:
                Utils.sendActionBar(event.getPlayer(), CommonColors.ERROR+"Resource pack download failed.");
                event.getPlayer().sendMessage(CommonColors.ERROR+"You blocked the resource pack download. Edit this server from your server list to allow resource packs to be downloaded.");
                Bukkit.getLogger().info(event.getPlayer().getName()+" blocked download of resource pack.");
                break;
            case FAILED_DOWNLOAD:
                Utils.sendActionBar(event.getPlayer(), CommonColors.ERROR+"Resource pack download failed.");
                Bukkit.getLogger().info(event.getPlayer().getName()+" failed to download resource pack.");
                break;
            case SUCCESSFULLY_LOADED:
                Utils.sendActionBar(event.getPlayer(), "Resource pack downloaded and applied!");
                Bukkit.getLogger().info(event.getPlayer().getName()+" downloaded and applied a resource pack.");
                break;
        }
    }


    //// Pack Methods
    // Send a resource pack to a player
    private void sendResourcePack(Player player, String packName){
        // Load the pack URL and hash from file
        FileConfiguration packFile = getPackFile();
        String packURL = packFile.getString("packs."+packName+".url");
        //byte[] packHash = packFile.getByteList("packs."+packName+".url").toArray();

        // Make sure pack URL exists
        if(packURL==null){
            Utils.sendActionBar(player, CommonColors.ERROR+"Resource pack not found. Ask an Admin for help.");
            Utils.notifyAdminsError("Could not find resource pack "+packName+" to display to "+player.getDisplayName());
            return;
        }

        // Send the resource pack
        player.setResourcePack(packURL);
    }

    // Get the default resource pack for a player
    private String getPlayerDefaultPack(OfflinePlayer player){
        return PlayerCharacter.getCharacter(player).getData(CorePlugin.plugin).getData().getString("resourcepacks.default");
    }
    // Set the default resource pack for a player
    private void setPlayerDefaultPack(OfflinePlayer player, String packName){
        PlayerCharacter character = PlayerCharacter.getCharacter(player);
        character.getData(CorePlugin.plugin).getData().set("resourcepacks.default", packName);
        character.saveData();
    }

    // Show a list (prompt) of all packs on the server
    private void displayPacks(CommandSender player){
        Prompt prompt = new Prompt();
        prompt.addQuestion(CommonColors.INFO+"--- "+CommonColors.MESSAGE+"Server Resource Packs"+CommonColors.INFO+" ---");
        
        for(String pack : getResourcePacks()) prompt.addAnswer(pack, "command_pack "+pack);

        prompt.display(player);
    }

    // Get the packs.yml file
    private FileConfiguration getPackFile(){
        return new ConfigAccessor("packs.yml").getConfig();
    }

    // Get a list of all packs on the server
    private Set<String> getResourcePacks(){
        return getPackFile().getConfigurationSection("packs").getKeys(false);
    }


    /**
     * Gets a hash for a resource pack.
     */
    private String getPackHash(){
		return null;
    }

}