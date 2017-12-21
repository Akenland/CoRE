package com.KyleNecrowolf.RealmsCore.Realm;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.KyleNecrowolf.RealmsCore.ConfigValues;
import com.KyleNecrowolf.RealmsCore.Common.ConfigAccessor;
import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.KyleNecrowolf.RealmsCore.Prompts.PromptActionEvent;

public final class RealmCommands implements TabExecutor, Listener {
    
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        
        Realm realm;

        // Always make sure first arg is a valid realm
        if(args.length>=1){
            realm =  new Realm(args[0]);
            if(!realm.exists()) Error.REALM_NOT_FOUND.displayActionBar(sender);
        } else return Error.INVALID_ARGS.displayActionBar(sender);


        // One arg - view realm info
		if(args.length==1){
            if(!sender.hasPermission("realms.realm")) return Error.NO_PERMISSION.displayChat(sender);
			return realm.displayInfo(sender);
        }


        // Setting data
        if(args.length>3 && args[1].equalsIgnoreCase("set")){
            if(!sender.hasPermission("realms.realm.set")) return Error.NO_PERMISSION.displayChat(sender);

            if(!realm.exists()) return Error.REALM_NOT_FOUND.displayActionBar(sender);

            // If sender does not have edit access, return error
            if(!sender.hasPermission("realms.realm.set.others") && sender instanceof Player && !realm.isOfficer((Player) sender))
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
                    sender.sendMessage(Utils.messageText+"The official color of the "+realm.getFullName()+Utils.messageText+" is now "+realm.getColor()+realm.getColor().name());
                    return true;
                case "fullname":
                    realm.setFullName(data);
                    sender.sendMessage(Utils.messageText+"The official name of "+realm.getName()+" is now "+realm.getFullName());
                    return true;
                case "tagline":
                    realm.setTagline(data);
                    sender.sendMessage(Utils.messageText+"The tagline of "+realm.getFullName()+Utils.messageText+" is now "+ChatColor.GRAY+ChatColor.ITALIC.toString()+realm.getTagline());
                    return true;
                case "parent":
                    if(!sender.hasPermission("realms.realm.set.others")) return Error.NO_PERMISSION.displayChat(sender);
                    Realm parent = new Realm(data);
                    if(!parent.exists()) return Error.REALM_NOT_FOUND.displayActionBar(sender);
                    realm.setParent(parent);
                    sender.sendMessage(Utils.messageText+"The "+realm.getFullName()+Utils.messageText+" is now a faction of "+parent.getFullName());
                    return true;
                default:
                    return Error.INVALID_ARGS.displayActionBar(sender);
            }
        }


        // Adding/removing officers
        if(args.length>2 && (args[1].equalsIgnoreCase("addofficer") || args[1].equalsIgnoreCase("removeofficer"))){
            if(!sender.hasPermission("realms.realm.officers")) return Error.NO_PERMISSION.displayChat(sender);
            
            if(!realm.exists()) return Error.REALM_NOT_FOUND.displayActionBar(sender);
            
            // If sender does not have edit access, return error
            if(!sender.hasPermission("realms.realm.officers.others") && sender instanceof Player && !realm.isOfficer((Player) sender)) return Error.NO_PERMISSION.displayChat(sender);


            // Check if adding or removing
            final boolean isOfficer = args[1].equalsIgnoreCase("addofficer") ? true : false;
            final String message = isOfficer ? "now" : "no longer";

            // Get players
            List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
            lastArgs.remove(1); lastArgs.remove(0);
            for(String arg : lastArgs){
                OfflinePlayer player = Utils.getPlayer(arg, true);
                // If player is valid, add as realm officer
                if(player!=null){
                    PlayerData data = new PlayerData(player);
                    if(data.getRealm().getName().equals(realm.getName())){
                        data.save("playerdata.realmofficer", isOfficer);
                        sender.sendMessage(data.getDisplayName()+Utils.messageText+" can "+message+" edit information in the "+realm.getFullName()+Utils.messageText+" and all sub-realms.");
                    }
                }
            }

            return true;
        }


        // Setting member titles
        if(args.length>3 && args[1].equalsIgnoreCase("title")){
            if(!sender.hasPermission("realms.realm.titles")) return Error.NO_PERMISSION.displayChat(sender);
            
            if(!realm.exists()) return Error.REALM_NOT_FOUND.displayActionBar(sender);
            
            // If sender does not have edit access, return error
            if(!sender.hasPermission("realms.realm.titles.others") && sender instanceof Player && !realm.isOfficer((Player) sender)) return Error.NO_PERMISSION.displayChat(sender);


            // Get player
            OfflinePlayer player = Utils.getPlayer(args[2], true);
            if(player==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
            PlayerData data = new PlayerData(player);
            if(!data.getRealm().getName().equals(realm.getName())) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
            
            // Merge all remaining args into a single string
            List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
            for(int i=0; i<3; i++) lastArgs.remove(0);
		    String title = ChatColor.translateAlternateColorCodes('&', String.join(" ", lastArgs));

            // Store the data in playerdata
            data.save("playerdata.title", title);

            // Update chat format and display name
            if(player.isOnline()){
                data.loadDisplayName();
                data.loadChatFormat();
            }
            
            sender.sendMessage(data.getDisplayName()+Utils.messageText+"'s title is now "+realm.getColor()+data.getTitle());
            return true;
        }


        // Inviting members
        if(args.length>2 && args[1].equalsIgnoreCase("invite")){
            if(!sender.hasPermission("realms.realm.players") || !(sender instanceof Player)) return Error.NO_PERMISSION.displayChat(sender);

            if(!realm.exists()) return Error.REALM_NOT_FOUND.displayActionBar(sender);

            // If sender does not have edit access, return error
            if(!sender.hasPermission("realms.realm.players.others") && !realm.isOfficer((Player) sender)) return Error.NO_PERMISSION.displayChat(sender);


            PlayerData data = new PlayerData((Player)sender);

            // Get players
            List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
            lastArgs.remove(1); lastArgs.remove(0);
            for(String arg : lastArgs){
                Player player = Utils.getPlayer(arg);
                // If player is valid, invite to realm
                if(player!=null){
                    // Prepare prompt
                    Prompt prompt = new Prompt();
                    String tagline = realm.getTagline()!=null ? ChatColor.GRAY+ChatColor.ITALIC.toString()+" - "+realm.getTagline() : "";
                    prompt.addQuestion(data.getRealm().getColor()+data.getTitle()+" "+data.getDisplayName()+Utils.messageText+" has invited you to join the "+realm.getFullName()+tagline);
                    if(ConfigValues.enableWolfiaFeatures) prompt.addQuestion("- Located in "+realm.getTopParent().getFullName());
                    Realm oldRealm = new PlayerData(player).getRealm();
                    String hasRealmString = (oldRealm.exists() && oldRealm.getName().length()>1) ? ", and leave the "+oldRealm.getFullName()+Utils.messageText+" behind." : ".";
                    prompt.addQuestion("- By pledging allegiance, you agree to become a loyal member of the "+realm.getFullName()+Utils.messageText+hasRealmString);
                    prompt.addAnswer("Pledge allegiance to the "+realm.getFullName()+Utils.messageText+" and join", "realm_join_"+realm.getName());
                    prompt.addAnswer("Decline invitation", "realm_declineinvite_"+sender.getName());
                    prompt.display(player);

                    sender.sendMessage(player.getDisplayName()+Utils.messageText+" was invited to the "+realm.getFullName()+Utils.messageText+".");
                }
            }

            return true;
        }


        // Kick members
        if(args.length>2 && args[1].equalsIgnoreCase("kick")){
            if(!sender.hasPermission("realms.realm.players")) return Error.NO_PERMISSION.displayChat(sender);
            
            if(!realm.exists()) return Error.REALM_NOT_FOUND.displayActionBar(sender);
            
            // If sender does not have edit access, return error
            if(!sender.hasPermission("realms.realm.players.others") && sender instanceof Player && !realm.isOfficer((Player) sender)) return Error.NO_PERMISSION.displayChat(sender);


            // Get players
            List<String> lastArgs = new ArrayList<String>(Arrays.asList(args));
            lastArgs.remove(1); lastArgs.remove(0);
            for(String arg : lastArgs){
                OfflinePlayer player = Utils.getPlayer(arg, true);
                // If player is valid, kick from realm
                if(player!=null){
                    PlayerData data = new PlayerData(player);
                    if(data.getRealm().getName().equals(realm.getName())){
                        data.save("playerdata.realm", null);
                        data.save("playerdata.title", null);
                        data.save("playerdata.realmofficer", null);
                        data.loadChatFormat();
                        sender.sendMessage(data.getDisplayName()+Utils.messageText+" is no longer in the "+realm.getFullName()+Utils.messageText+".");
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
            if(sender.hasPermission("realms.admin")) return new ArrayList<String>(new ConfigAccessor("realms.yml").getConfig().getConfigurationSection("realms").getKeys(false));
            // Otherwise, return own realm, and all child realms
            else if(sender instanceof Player){
                Realm playerRealm = new PlayerData((Player)sender).getRealm();
                List<String> completions = new ArrayList<String>(Arrays.asList(playerRealm.getName()));
                for(Realm realm : playerRealm.getChildRealms()) completions.add(realm.getName());
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
            switch(splitAction[0]){
                // Join faction (by invite)
                case "join":
                    PlayerData data = new PlayerData(event.getPlayer());
                    data.save("playerdata.realm", splitAction[1]);
                    event.getPlayer().sendMessage(Utils.messageText+"You are now a member of "+data.getRealm().getFullName());
                    data.loadChatFormat();
                    return;
                // Decline invite
                case "declineinvite":
                    event.getPlayer().sendMessage(Utils.messageText+"Invitation declined.");
                    Player player = Bukkit.getPlayer(splitAction[1]);
                    if(player!=null) player.sendMessage(event.getPlayer().getDisplayName()+Utils.messageText+" declined your invitation.");
                    return;
                default:
                    return;
            }
        }
    }
}