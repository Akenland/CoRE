package com.KyleNecrowolf.RealmsCore.Extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Permissions.PermsUtils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

public final class RemoveCommands implements TabExecutor {

    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        //// Check permissions and admin
        if(!sender.hasPermission("realms.entity") || !PermsUtils.isDoubleCheckedAdmin(sender)){
            Utils.notifyAdminsError(sender.getName()+Utils.errorText+" failed security check (removing entities "+String.join(" ", args)+").");
            return Error.NO_PERMISSION.displayChat(sender);
        }

        //// Get target location -  player location, otherwise main world spawn location
        Location targetLoc = sender instanceof Player ? ((Player)sender).getLocation() : Bukkit.getWorlds().get(0).getSpawnLocation();


        //// If lowerlag, remove monsters and drops
        if(command.getName().equalsIgnoreCase("lowerlag")){
            int entityCount = removeEntities(targetLoc, -1, lowerLag);
            Utils.sendActionBar(sender, "Removed "+entityCount+" entities.");
            return true;
        }


        //// Get entity type to remove
        if(args.length==0) return Error.INVALID_ARGS.displayActionBar(sender);

        Collection<EntityType> types;
        String arg = args[0].toLowerCase();
        switch(arg){
            case "monsters":
                types = monsters; break;
            case "animals":
                types = passiveAnimals; break;
            case "vehicles":
                types =  vehicles; break;
            case "drops":
                types = drops; break;
            case "projectiles":
                types = projectiles; break;
            default:
                return Error.INVALID_ARGS.displayActionBar(sender);
        }

        // Get radius
        int radius = -1;
        try{radius = args.length>=2 ? Integer.parseInt(args[1]) : -1;} catch(NumberFormatException e){}

        // Get entities
        Collection<Entity> targetEntities = getNearbyEntities(targetLoc, radius, types);

        // If radius is large or infinite, or over 200 entities, warn sender
        if(!(args.length==3 && args[2].equalsIgnoreCase("confirm")) && (targetEntities.size()>200 || radius>100)){
            Prompt prompt = new Prompt();
            prompt.addQuestion("You are permanently removing "+targetEntities.size()+" entities. Are you sure?");
            prompt.addAnswer(Utils.errorText+"Remove "+targetEntities.size()+" entities", "command_remove "+arg+" "+radius+" confirm");
            prompt.display(sender);
            return true;
        }

        // Remove the entities
        int entityCount = removeEntities(targetEntities);
        sender.sendMessage(Utils.messageText+"Removed "+entityCount+" entities.");
        return true;
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // If completing first arg on normal command, show available entity types
        if(!command.getName().equalsIgnoreCase("lowerlag") && args.length==1)
            return Arrays.asList("drops", "monsters", "animals", "vehicles", "projectiles");
        
        // Return no completions
        return Arrays.asList("");
    }
    

    //// Methods
    // Common groups of entity types
    private static final Collection<EntityType> monsters;
    private static final Collection<EntityType> netherMonsters;
    private static final Collection<EntityType> ambientAnimals;
    private static final Collection<EntityType> passiveAnimals;
    private static final Collection<EntityType> vehicles;
    private static final Collection<EntityType> drops;
    private static final Collection<EntityType> projectiles;
    private static final Collection<EntityType> lowerLag;

    static {
        netherMonsters = Arrays.asList(EntityType.BLAZE,EntityType.GHAST,EntityType.MAGMA_CUBE,EntityType.PIG_ZOMBIE,EntityType.WITHER_SKELETON);

        monsters = new ArrayList<EntityType>(Arrays.asList(EntityType.CAVE_SPIDER,EntityType.CREEPER,EntityType.ENDERMAN,EntityType.ENDERMITE,EntityType.GUARDIAN,EntityType.HUSK,EntityType.SILVERFISH,EntityType.SKELETON,EntityType.SKELETON_HORSE,EntityType.SLIME,EntityType.SPIDER,EntityType.STRAY,EntityType.VEX,EntityType.WITCH,EntityType.ZOMBIE,EntityType.ZOMBIE_VILLAGER));
        monsters.addAll(netherMonsters);

        ambientAnimals = Arrays.asList(EntityType.BAT,EntityType.SQUID);

        passiveAnimals = new ArrayList<EntityType>(Arrays.asList(EntityType.CHICKEN,EntityType.COW,EntityType.MUSHROOM_COW,EntityType.PIG,EntityType.POLAR_BEAR,EntityType.RABBIT,EntityType.SHEEP));
        passiveAnimals.addAll(ambientAnimals);

        vehicles = Arrays.asList(EntityType.BOAT,EntityType.MINECART,EntityType.MINECART_CHEST,EntityType.MINECART_COMMAND,EntityType.MINECART_FURNACE,EntityType.MINECART_HOPPER,EntityType.MINECART_MOB_SPAWNER,EntityType.MINECART_TNT);

        drops =  Arrays.asList(EntityType.ARROW,EntityType.DROPPED_ITEM,EntityType.EXPERIENCE_ORB,EntityType.LINGERING_POTION,EntityType.AREA_EFFECT_CLOUD,EntityType.PRIMED_TNT,EntityType.SPECTRAL_ARROW,EntityType.TIPPED_ARROW);

        projectiles = Arrays.asList(EntityType.ARROW,EntityType.DRAGON_FIREBALL,EntityType.EGG,EntityType.ENDER_PEARL,EntityType.ENDER_SIGNAL,EntityType.EVOKER_FANGS,EntityType.FIREBALL,EntityType.FIREWORK,EntityType.LINGERING_POTION,EntityType.LLAMA_SPIT,EntityType.PRIMED_TNT,EntityType.SHULKER_BULLET,EntityType.SMALL_FIREBALL,EntityType.SNOWBALL,EntityType.SPECTRAL_ARROW,EntityType.SPLASH_POTION,EntityType.THROWN_EXP_BOTTLE,EntityType.TIPPED_ARROW,EntityType.WITHER_SKULL);

        lowerLag = new ArrayList<EntityType>();
        lowerLag.addAll(monsters); lowerLag.addAll(drops); lowerLag.addAll(projectiles); lowerLag.addAll(ambientAnimals);
    }


    // Get all entities in a radius
    private Collection<Entity> getNearbyEntities(Location location, int radius){
        if(radius<0) return location.getWorld().getEntities();
        return location.getWorld().getNearbyEntities(location, radius, radius, radius);
    }
    // Get specific type(s) of entity
    private Collection<Entity> getNearbyEntities(Location location, int radius, Collection<EntityType> types){
        Collection<Entity> entities = getNearbyEntities(location, radius);
        for(Entity entity : new ArrayList<Entity>(entities)){
            if(!types.contains(entity.getType())) entities.remove(entity);
        }
        return entities;
    }

    // Remove a collection of entities
    private int removeEntities(Collection<Entity> entities){
        for(Entity entity : entities) if(entity.getCustomName()==null && !entity.isCustomNameVisible()) entity.remove();
        return entities.size();
    }
    // Remove entities by type
    private int removeEntities(Location location, int radius, Collection<EntityType> types){
        return removeEntities(getNearbyEntities(location, radius, types));
    }
}