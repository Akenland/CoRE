package com.kylenanakdewa.core.Extras;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

/**
 * FindPetCommands
 */
public final class FindPetsCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("core.findpets")) return Error.NO_PERMISSION.displayChat(sender);

        AnimalTamer tamer = null;
        Entity destination = null;

        // If one arg, find pets from another player
        if(args.length==1 && sender.hasPermission("core.findpets.others")){
            OfflinePlayer player = Utils.getPlayer(args[0], true);
            tamer = player;
            if(player.isOnline()) destination = player.getPlayer();
            else if(sender instanceof Player) destination = (Player)sender;
            else return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
        }
        // If no args, make sure sender is a player
        else if(sender instanceof AnimalTamer){
            tamer = (AnimalTamer)sender;
            destination = (Player)sender;
        }
        if(tamer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

        // Get all tamables in all worlds
        for(World world : Bukkit.getWorlds()){
            for(LivingEntity entity : world.getLivingEntities()){
                if(entity instanceof Tameable && ((Tameable)entity).getOwner()!=null && ((Tameable)entity).getOwner().equals(tamer)){
                    entity.teleport(destination);
                    sender.sendMessage(CommonColors.INFO+"Teleported "+entity.getCustomName()+CommonColors.INFO+" ("+entity.getType().toString()+")");
                }
            }
        }
        return true;
    }

}