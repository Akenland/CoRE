package com.KyleNecrowolf.RealmsCore.Extras;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;

// Various commands for affecting player movement
public final class MovementCommands implements CommandExecutor, Listener {

    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //// Fly command
        if(command.getName().equalsIgnoreCase("fly")){
            if(!sender.hasPermission("realms.fly")) return Error.NO_PERMISSION.displayChat(sender);

            Player targetPlayer = null;
            if(args.length==1 && sender.hasPermission("realms.fly.others"))
                targetPlayer = Utils.getPlayer(args[0]);
            else if(sender instanceof Player)
                targetPlayer = (Player) sender;
            if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

            if(!targetPlayer.getAllowFlight()){
                targetPlayer.setAllowFlight(true);
                Utils.sendActionBar(sender, targetPlayer.getDisplayName()+Utils.messageText+" can now fly.");
                Utils.sendActionBar(targetPlayer, "You can now fly.");
            } else {
                targetPlayer.setAllowFlight(false);
                Utils.sendActionBar(sender, targetPlayer.getDisplayName()+Utils.messageText+" can no longer fly.");
                Utils.sendActionBar(targetPlayer, "You can no longer fly.");
            }
            
            return true;
        }


        //// Speed command
        if(command.getName().equalsIgnoreCase("speed")){
            if(!sender.hasPermission("realms.speed")) return Error.NO_PERMISSION.displayChat(sender);

            Player targetPlayer = null;
            if(args.length==2 && sender.hasPermission("realms.speed.others"))
                targetPlayer = Utils.getPlayer(args[1]);
            else if(sender instanceof Player)
                targetPlayer = (Player) sender;
            if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

            // No args, reset speed
            if(args.length==0){
                targetPlayer.setWalkSpeed(0.2f);
                targetPlayer.setFlySpeed(0.1f);
                Utils.sendActionBar(targetPlayer, "Your speed was reset.");
                return true;
            }
            
            // Convert speed value to a float
            float newValue = Float.parseFloat(args[0]);
            // Make sure it's between 0-10
            if(newValue>10 || newValue<=0) return Error.INVALID_ARGS.displayActionBar(sender);

            // Get the default speeds
            float defaultSpeed = targetPlayer.isFlying() ? 0.1f : 0.2f;

            // Calculate the new speed value (these calculations are taken from Essentials)
            if(newValue<1f){
                newValue = defaultSpeed*newValue;
            } else {
                newValue = (((newValue - 1) / 9) * (1f - defaultSpeed)) + defaultSpeed;
            }

            // Set speed depending on if player is walking or flying
            if(targetPlayer.isFlying()){
                targetPlayer.setFlySpeed(newValue);
                Utils.sendActionBar(sender, targetPlayer.getDisplayName()+Utils.messageText+"'s fly speed was modified.");
                Utils.sendActionBar(targetPlayer, "Your fly speed was modified.");
            } else {
                targetPlayer.setWalkSpeed(newValue);
                Utils.sendActionBar(sender, targetPlayer.getDisplayName()+Utils.messageText+"'s walk speed was modified.");
                Utils.sendActionBar(targetPlayer, "Your walk speed was modified.");
            }

            return true;
        }

        return false;
    }


    //// Listener - make sure players don't fall and die on login
    @EventHandler
    public void flyOnJoin(PlayerJoinEvent event){
        if(!event.getPlayer().isOnGround() && event.getPlayer().hasPermission("realms.fly")){
            Utils.sendActionBar(event.getPlayer(), "Protected from fall damage.");
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 4));
        }
    }
}