package com.kylenanakdewa.core.Extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class SignColorListener implements Listener, CommandExecutor {

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        for (int i = 0; i < 4; i++)
            event.setLine(i, ChatColor.translateAlternateColorCodes('&', event.getLine(i)));
    }

    @EventHandler
    public void onSignHit(PlayerInteractEvent event) {
        // Player must have perm, block must be sign, item must be ink sack
        if (!event.getPlayer().hasPermission("core.sign") || !event.hasBlock()
                || !(event.getClickedBlock().getState() instanceof Sign) || !event.hasItem()
                || !event.getItem().getType().equals(Material.INK_SAC))
            return;

        switch (event.getAction()) {
            // Right click - copy
            case RIGHT_CLICK_BLOCK:
                copySign(event.getPlayer(), (Sign) event.getClickedBlock().getState());
                break;

            // Left click - paste
            case LEFT_CLICK_BLOCK:
                pasteSign(event.getPlayer(), (Sign) event.getClickedBlock().getState());
                break;

            default:
                break;
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("core.sign") || !(event.getBlock().getState() instanceof Sign)
                || !event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.INK_SAC))
            return;

        event.setCancelled(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("core.sign") || !(sender instanceof Player))
            return Error.NO_PERMISSION.displayChat(sender);

        Player player = (Player) sender;

        // Get the sign
        BlockState blockState = player.getTargetBlock(null, 5).getState();
        if (!(blockState instanceof Sign)) {
            Utils.sendActionBar(sender, CommonColors.ERROR + "You must look at a sign.");
            return false;
        }
        Sign sign = (Sign) blockState;

        // No args
        if (args.length == 0)
            return Error.INVALID_ARGS.displayActionBar(sender);

        // Copy
        if (args[0].equalsIgnoreCase("copy")) {
            copySign(player, sign);
            return true;
        }
        // Paste
        if (args[0].equalsIgnoreCase("paste")) {
            pasteSign(player, sign);
            return true;
        }

        // Editing
        if (Character.isDigit(args[0].charAt(0))) {
            // Get the line
            int line = Integer.parseInt(args[0].substring(0, 1)) - 1;
            if (line < 0 || line > 3)
                return Error.INVALID_ARGS.displayActionBar(sender);

            // Get the text
            List<String> text = new ArrayList<String>(Arrays.asList(args));
            text.remove(0);
            String fullText = ChatColor.translateAlternateColorCodes('&', String.join(" ", text));

            // Update the sign
            sign.setLine(line, fullText);
            if (sign.update())
                Utils.sendActionBar(player, "Sign edited.");
            else
                Utils.sendActionBar(player, CommonColors.ERROR + "Sign was removed.");

            return true;
        }

        return Error.INVALID_ARGS.displayActionBar(sender);
    }

    /** Player signs clipboard. */
    private static final Map<Player, String[]> signClipboard = new HashMap<Player, String[]>();

    /**
     * Copies a sign to the player's clipboard.
     *
     * @param player the player to save for
     * @param sign   the sign to save
     */
    private static void copySign(Player player, Sign sign) {
        signClipboard.put(player, sign.getLines());
        Utils.sendActionBar(player, "Sign copied.");
    }

    /**
     * Pastes a sign from the player's clipboard.
     *
     * @param player the player to paste for
     * @param sign   the sign to overwrite
     */
    private static void pasteSign(Player player, Sign sign) {
        if (!signClipboard.containsKey(player)) {
            Utils.sendActionBar(player, CommonColors.ERROR + "No sign copied.");
            return;
        }

        String[] lines = signClipboard.get(player);
        for (int i = 0; i < lines.length; i++) {
            sign.setLine(i, lines[i]);
        }
        if (sign.update())
            Utils.sendActionBar(player, "Sign pasted.");
        else
            Utils.sendActionBar(player, CommonColors.ERROR + "Sign was removed.");
    }
}