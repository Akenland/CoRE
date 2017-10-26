package com.KyleNecrowolf.RealmsCore.Extras;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Permissions.PermsUtils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

public final class EntityCommands implements TabExecutor, Listener {
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        //// Check permissions and admin
        if(!sender.hasPermission("realms.entity") || !PermsUtils.isDoubleCheckedAdmin(sender)){
            Utils.notifyAdminsError(sender.getName()+Utils.errorText+" failed security check (managing entity "+String.join(" ", args)+").");
            return Error.NO_PERMISSION.displayChat(sender);
        }


        //// No args - toggle entity inspector
        if(args.length==0 && sender instanceof Player){
            UUID uuid = ((Player)sender).getUniqueId();
            if(inspectingPlayers.contains(uuid)){
                inspectingPlayers.remove(uuid);
                Utils.sendActionBar(sender, "Entity inspector disabled.");
            } else {
                inspectingPlayers.add(uuid);
                Utils.sendActionBar(sender, "Entity inspector enabled. Right-click entities to see their information.");
            }
            return true;
        }


        //// At least one arg - view/edit an entity
        if(args.length>=1){
            // Target world is the world player is in, or default world if not a player
            World world = (sender instanceof Player) ? ((Player)sender).getWorld() : Bukkit.getWorlds().get(0);

            // Get entity by ID
            Entity entity = null;
            for(Entity e : world.getEntities()){
                try{if(e.getEntityId()==Integer.parseInt(args[0])) entity = e;} catch(NumberFormatException exception){}
            }
            if(entity==null) return Error.ENTITY_NOT_FOUND.displayActionBar(sender);


            // One arg - display info
            if(args.length==1){
                displayEntityInfo(sender, entity);
                return true;
            }

            // Two args - edit entity
            switch(args[1]){
                // TP entity to sender
                case "tphere":
                    if(sender instanceof Player) return entity.teleport((Player)sender);
                    else return Error.NO_PERMISSION.displayChat(sender);
                // Toggle entity invulnerability
                case "invulnerable":
                    if(entity.isInvulnerable()){
                        entity.setInvulnerable(false);
                        sender.sendMessage(Utils.messageText+"Entity "+entity.getEntityId()+" ("+entity.getType()+") is no longer invulnerable.");
                    } else {
                        entity.setInvulnerable(true);
                        sender.sendMessage(Utils.messageText+"Entity "+entity.getEntityId()+" ("+entity.getType()+") is now invulnerable.");
                    }
                    return true;
                // Set custom display name for entity
                case "name":
                    if(args.length==3){
                        entity.setCustomName(args[2]);
                        entity.setCustomNameVisible(true);
                        sender.sendMessage(Utils.messageText+"Entity "+entity.getEntityId()+" ("+entity.getType()+") name set to "+entity.getCustomName()+".");
                    } else {
                        entity.setCustomName(null);
                        entity.setCustomNameVisible(false);
                        sender.sendMessage(Utils.messageText+"Entity "+entity.getEntityId()+" ("+entity.getType()+") no longer has a name.");
                    }
                    return true;
                // Toggle glowing status
                case "glowing":
                    if(entity.isGlowing()){
                        entity.setGlowing(false);
                        sender.sendMessage(Utils.messageText+"Entity "+entity.getEntityId()+" ("+entity.getType()+") is no longer glowing.");
                    } else {
                        entity.setGlowing(true);
                        sender.sendMessage(Utils.messageText+"Entity "+entity.getEntityId()+" ("+entity.getType()+") is now glowing.");
                    }
                    return true;
                // Permanently remove entity
                case "remove":
                    entity.remove();
                    sender.sendMessage(Utils.messageText+"Entity "+entity.getEntityId()+" ("+entity.getType()+") was permanently removed.");
                    return true;
                // Invalid arg
                default:
                    return Error.INVALID_ARGS.displayActionBar(sender);
            }
        }
    
    return Error.INVALID_ARGS.displayActionBar(sender);
    }

    //// Tab completions
    @Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        // Completions for editing an entity
        if(args.length==2) return Arrays.asList("tphere", "invulnerable", "name", "glowing", "remove");
        
        return null;
    }


    //// Entity inspector
    // Set of players who have inspector active
    private static final HashSet<UUID> inspectingPlayers = new HashSet<UUID>();
    @EventHandler
    public void onEntityInspect(PlayerInteractEntityEvent event){
        // Display entity info if player has inspector active
        if(inspectingPlayers.contains(event.getPlayer().getUniqueId()) && event.getPlayer().hasPermission("realms.entity") && event.getHand().equals(EquipmentSlot.HAND))
            displayEntityInfo(event.getPlayer(), event.getRightClicked());
    }


    //// Methods
    // Display info about an entity
    public void displayEntityInfo(CommandSender sender, Entity entity){
        Prompt prompt = new Prompt();
        prompt.addQuestion(Utils.infoText+"--- "+Utils.messageText+"Entity "+entity.getEntityId()+" - "+entity.getType()+Utils.infoText+" ---");
        
        // Basic info
        prompt.addAnswer("Name: "+entity.getName() + (entity.getCustomName()!=null ? " ("+entity.getCustomName()+")" : ""), "");
        Location loc = entity.getLocation();
        prompt.addAnswer("Location: "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ(), "command_tp "+loc.getX()+" "+loc.getY()+" "+loc.getZ());
        prompt.addAnswer("Alive for "+entity.getTicksLived()+" ticks", "");
        prompt.addAnswer("Invulnerable: "+entity.isInvulnerable(), "command_entity "+entity.getEntityId()+" invulnerable");
        if(entity.getLastDamageCause()!=null) prompt.addAnswer("Last damaged from "+entity.getLastDamageCause().getCause(), "");

        // Living entity info
        if(entity instanceof Damageable){
            String maxHealth = entity instanceof Attributable ? "/"+((Attributable)entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : "";
            prompt.addAnswer("Health: "+((Damageable)entity).getHealth()+maxHealth, "");
        }

        // Tameable info
        if(entity instanceof Tameable){
            Tameable tamed = ((Tameable)entity);
            String tamedStatus = tamed.isTamed()&&tamed.getOwner()!=null ? "Owner: "+tamed.getOwner().getName() : "Not tamed";
            prompt.addAnswer(tamedStatus, "");
        }

        // Extra info
        StringBuilder extraInfo = new StringBuilder();
        extraInfo
            .append(entity.isDead() ? "Dead, " : "")
            .append(entity.isSilent() ? "Silent, " : "")
            .append(entity.isGlowing() ? "Glowing, " : "")
            .append(entity.isInsideVehicle() ? "Riding "+entity.getVehicle().getName()+", " : "")
            .append(entity.getPassengers().size()!=0 ? "Carrying "+entity.getPassengers().size()+" passengers, " : "")
            .append(entity.getFireTicks()>0 ? "On fire for "+entity.getFireTicks()+" ticks, " : "")
            .append(!entity.hasGravity() ? "Ignores gravity, " : "")
            .append(!entity.isValid() ? "Invalid (dead or despawned), " : "");
        prompt.addAnswer("Other info: "+extraInfo.toString(), "");

        // Actions
        prompt.addAnswer("Teleport entity to you", "command_entity "+entity.getEntityId()+" tphere");
        prompt.addAnswer(Utils.errorText+"Remove entity", "command_entity "+entity.getEntityId()+" remove");

        // Display prompt
        prompt.display(sender);
    }
}