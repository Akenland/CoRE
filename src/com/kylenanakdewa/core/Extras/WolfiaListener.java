package com.kylenanakdewa.core.Extras;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.CoreConfig;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;

public final class WolfiaListener implements Listener, CommandExecutor {

    //// Blade of Light
    // Get the blade for a player
    private static ItemStack getBladeOfLight(PlayerCharacter player){
        ItemStack sword = new ItemStack(Material.WOOD_SWORD);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.WOOD_SWORD);
        
        // Get player info
        String username = player.getUsername();
        String dateString = player.getFirstLoginDate().format(DateTimeFormatter.ofPattern("MM/dd/yy"));

        // Lore text
        String codes = ChatColor.GRAY+ChatColor.ITALIC.toString();
        List<String> loreText = Arrays.asList(
            codes+"You'll see the sun rising above these",
            codes+"lands. The lands we explored, the",
            codes+"civilizations we built, the bonds we",
            codes+"formed. This is our world, our home.",
            "",
            codes+"Let this be the torch we pass to you. The",
            codes+"mountains you'll climb, the people you'll",
            codes+"meet, the secrets you'll find. These",
            codes+"lands are our words, that we wrote with",
            codes+"our souls, our glimmer of light. We are",
            codes+"watching.",
            "",
            codes+"We'll remember you not by what you",
            codes+"defeat, but by the stories you'll write.",
            "",
            codes+"Welcome to our home.",
            "",
            codes+"-The Descendants of Aunix",
            "",
            ChatColor.DARK_PURPLE+ChatColor.ITALIC.toString()+username,
            ChatColor.DARK_PURPLE+ChatColor.ITALIC.toString()+dateString
        );

        // Set item metadata
        meta.setDisplayName(ChatColor.DARK_PURPLE+"Blade of Light");
        meta.setLore(loreText);

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

        sword.setItemMeta(meta);

        return sword;
    }


    //// Command
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Check permissions
        if(!sender.hasPermission("core.bladeoflight") || !CoreConfig.enableWolfiaFeatures || !(sender instanceof Player)) return Error.NO_PERMISSION.displayChat(sender);

        // Get target player
        OfflinePlayer player = args.length==1 ? Utils.getPlayer(args[0], true) : (Player) sender;
        if(player==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

        // Get a Blade of Light for this player
        ItemStack sword = getBladeOfLight(PlayerCharacter.getCharacter(player));

        // Give sword to sender
        ((Player) sender).getInventory().addItem(sword);
        Utils.sendActionBar(sender, ChatColor.DARK_PURPLE+"Blade of Light"+CommonColors.MESSAGE+" for "+player.getName()+" added to inventory");

        return true;
    }


    //// Listener
    @EventHandler
    public void giveSwordOnJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(!player.hasPlayedBefore() && CoreConfig.enableWolfiaFeatures){
            // Notify admins
            //Utils.notifyAdmins(player.getDisplayName()+CommonColors.INFO+" is a new player, giving Blade of Light, emeralds, and apples.");
            // Get a sword for them
            ItemStack sword = getBladeOfLight(PlayerCharacter.getCharacter(player));
            // Add sword, 32 emeralds, and 10 apples to inventory
            player.getInventory().addItem(sword, new ItemStack(Material.EMERALD, 32), new ItemStack(Material.APPLE, 10));
        }
    }
}