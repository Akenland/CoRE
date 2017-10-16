package com.KyleNecrowolf.RealmsCore.Extras;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Permissions.PermsUtils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

// World management commands, including world loading, unloading, and entering
public final class WorldCommands implements TabExecutor {

    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // All commands require permission realms.world
        if(!sender.hasPermission("realms.world")) return Error.NO_PERMISSION.displayChat(sender);


        // No args - display world list
        if(args.length==0){
        	// Get the list of worlds from Bukkit
		    List<World> worlds = Bukkit.getWorlds();
		
		    // Prepare a prompt
            Prompt list = new Prompt();
		    list.addQuestion(Utils.infoText+"--- "+Utils.messageText+"Worlds"+Utils.infoText+" ---");
		
            // Add each world name, with the action to get info
		    for(World world : worlds){
			    list.addAnswer(getWorldColor(world)+world.getName(), "command_world "+world.getName());
		    }
		
		    // Display the prompt to sender
		    list.display(sender);
		
		    return true;
        }


        // Create a world
        if(args.length>=2 && args[0].equalsIgnoreCase("create")){
            // Check permissions
            if(!sender.hasPermission("realms.world.create") || !PermsUtils.isDoubleCheckedAdmin(sender)){
                Utils.notifyAdminsError(sender.getName()+Utils.errorText+" failed security check (creating world "+String.join(" ", args)+").");
                return Error.NO_PERMISSION.displayChat(sender);
            }

            String worldName = args[1];

            // See if the world already exists, and load it instead
            if(new File(Bukkit.getServer().getWorldContainer(), worldName).exists()){
                // World already exists, prompt player to load
                Prompt alreadyLoaded = new Prompt();
                alreadyLoaded.addQuestion("World "+worldName+" already exists.");
                alreadyLoaded.addAnswer("Load world", "command_world load "+worldName);
                alreadyLoaded.display(sender);
                return true;
            }


            // Otherwise, prepare to create world
            WorldCreator world = new WorldCreator(worldName);
            try {
                // Set the seed
                if(args.length>=3) world.seed(Long.parseLong(args[2]));
                // Set environment
                if(args.length>=4) world.environment(Environment.valueOf(args[3]));
                // Set type
                if(args.length>=5) world.type(WorldType.valueOf(args[4]));
            } catch(IllegalArgumentException e){
                return Error.INVALID_ARGS.displayActionBar(sender);
            }


            // Five args needed to generate world
            if(args.length==6 && args[5].equalsIgnoreCase("generatenow")){
                World createdWorld = world.createWorld();
                Prompt newWorld = new Prompt();
                newWorld.addQuestion("Created and loaded world "+createdWorld.getName()+".");
                newWorld.addAnswer("World information", "command_world "+createdWorld.getName());
                newWorld.addAnswer("Teleport to world spawn", "command_world "+createdWorld.getName()+" enter");
                newWorld.display(sender);
                return true;
            }


            // Prepare prompt
            Prompt createNew = new Prompt();
            createNew.addQuestion("Creating new world: "+world.name());
            createNew.addQuestion("- Seed: "+world.seed());
            createNew.addQuestion("- Environment: "+world.environment());
            createNew.addQuestion("- Type: "+world.type());
            createNew.addAnswer("Create world", "command_world create "+world.name()+" "+world.seed()+" "+world.environment().name()+" "+world.type().getName()+" generatenow");
            createNew.display(sender);
            return true;
        }


        // Load existing world
        if(args.length==2 && args[1].equalsIgnoreCase("load")){
            // Check permissions
            if(!sender.hasPermission("realms.world.create") && !sender.hasPermission("realms.world.load")) return Error.NO_PERMISSION.displayChat(sender);

            // Make sure world exists
            if(!new File(Bukkit.getServer().getWorldContainer(), args[0]+"\\level.dat").exists()) return Error.WORLD_NOT_FOUND.displayActionBar(sender);

            World loadedWorld = new WorldCreator(args[0]).createWorld();
            Prompt loadPrompt = new Prompt();
            loadPrompt.addQuestion("Loaded world "+loadedWorld.getName());
            loadPrompt.addAnswer("World information", "command_world "+loadedWorld.getName());
            loadPrompt.addAnswer("Teleport to world spawn", "command_world "+loadedWorld.getName()+" enter");
            loadPrompt.display(sender);
            return true;
        }


        // For all other commands, first arg must be world name
        World world = Bukkit.getWorld(args[0]);
        if(world==null) return Error.WORLD_NOT_FOUND.displayActionBar(sender);
        ChatColor color = getWorldColor(world);


        // Just world name - show info
        if(args.length==1){
            Prompt info = new Prompt();
            info.addQuestion(Utils.infoText+"--- "+Utils.messageText+"World: "+color+world.getName()+Utils.infoText+" ---");
            info.addQuestion("- Seed: "+world.getSeed());
            info.addQuestion("- Type/Environment: "+world.getWorldType()+" "+world.getEnvironment());
            info.addQuestion("- Time/Weather: "+world.getTime()+" ticks, "+ ((world.hasStorm()) ? "raining for another "+world.getWeatherDuration()+" ticks" : "clear"));
            info.addQuestion("- "+world.getPlayers().size()+" players, "+world.getEntities().size()+" entities, "+world.getLoadedChunks().length+" chunks loaded");

            info.addAnswer("Teleport to world spawn", "command_world "+world.getName()+" enter");
            info.addAnswer("Unload world", "command_world "+world.getName()+" unload");

            info.display(sender);
            return true;
        }

        
        // Unload world
        if(args.length==2 && args[1].equalsIgnoreCase("unload")){
            // Check permissions
            if(!sender.hasPermission("realms.world.create") && !sender.hasPermission("realms.world.load")) return Error.NO_PERMISSION.displayChat(sender);

		    Utils.notifyAdmins("Unloaded world "+world.getName());
		    return Bukkit.unloadWorld(world, true);
        }
        
        
        // Teleport to world spawn
        if(args.length>=2 && args[1].equalsIgnoreCase("enter")){
            // Check permissions
            if(!sender.hasPermission("realms.world.enter")) return Error.NO_PERMISSION.displayChat(sender);

            Player targetPlayer = null;
            if(args.length==3)
                targetPlayer = Utils.getPlayer(args[2]);
            else if(sender instanceof Player)
                targetPlayer = (Player) sender;
            if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
		
		    Utils.sendActionBar(sender, "Warping to world "+world.getName());
		    return targetPlayer.teleport(world.getSpawnLocation());
        }
		
        return Error.INVALID_ARGS.displayActionBar(sender);
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        
        // If no permission, return nothing
        if(!sender.hasPermission("realms.world")) return Arrays.asList("");

        // If first arg, return world list, plus "create"
        if(args.length==1){
            List<String> completions = new ArrayList<String>();
            for(World world : Bukkit.getWorlds()){
                completions.add(world.getName());
            }
            if(sender.hasPermission("realms.world.create")) completions.add("create");

            return completions;
        }

        // If second arg, and not create, list options
        if(args.length==2 && !args[0].equalsIgnoreCase("create")){
            return Arrays.asList("enter", "load", "unload");
        }

        // If creating world, list worldgen options
        if(args.length>1 && args[0].equalsIgnoreCase("create")){
            if(args.length==2) return Arrays.asList("0");
            if(args.length==3){
                List<String> completions = new ArrayList<String>();
                for(Environment env : Environment.values()){
                    completions.add(env.name());
                }
                return completions;
            }
            if(args.length==4){
                List<String> completions = new ArrayList<String>();
                for(WorldType type : WorldType.values()){
                    completions.add(type.name());
                }
                return completions;
            }
        }
        
        return null;
    }


    //// Extra methods
    private static ChatColor getWorldColor(World world){
        // Colour the name based on dimension
        switch(world.getEnvironment()){
            case NORMAL:
                return ChatColor.DARK_GREEN;
            case NETHER:
                return ChatColor.DARK_RED;
            case THE_END:
                return ChatColor.YELLOW;
            default:
                return Utils.messageText;
        }
    }
}