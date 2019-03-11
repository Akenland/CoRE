package com.kylenanakdewa.core;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.Permissions.PermsUtils;
import com.kylenanakdewa.core.common.prompts.Prompt;

/**
 * Meta commands for Project CoRE.
 * @author Kyle Nanakdewa
 */
public final class CoreCommands implements TabExecutor {

    /** The Project CoRE instance that this command handler is for. */
    private final CorePlugin plugin;

    /**
     * Creates a command handler for the specified instance of Project CoRE.
     */
    CoreCommands(CorePlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Version command
        if(args.length==0 || args[0].equalsIgnoreCase("version")){
            Prompt prompt = new Prompt();
            prompt.addQuestion("&2Project CoRE "+plugin.getDescription().getVersion()+" by Kyle Nanakdewa");
            prompt.addAnswer("A roleplaying framework and server essentials, for small/medium survival and roleplay servers.","");
            prompt.addAnswer("Website: http://plugins.akenland.com/", "url_http://plugins.akenland.com/");

            // Plugin configuration info
            if(sender.hasPermission("core.admin")){
                prompt.addAnswer("Server running compatible version: "+Utils.isLatestVersion()+" (click for details)", "command_version");
                prompt.addAnswer("Permissions enabled: "+CoreConfig.permsEnabled, "");
                prompt.addAnswer("Chat formatting enabled: "+ CoreConfig.formatChat, "");
                if(CoreConfig.enableWolfiaFeatures)
                    prompt.addAnswer("Akenland functionality enabled: "+CoreConfig.enableWolfiaFeatures, "");
            }

            prompt.display(sender);
			return true;
        }

        // Reload command
        if(args[0].equalsIgnoreCase("reload")){
            // Check permissions
            if(!sender.hasPermission("core.reload") || !PermsUtils.isDoubleCheckedAdmin(sender)){
                Utils.notifyAdminsError(sender.getName()+CommonColors.ERROR+" failed security check (reloading CoRE).");
                return Error.NO_PERMISSION.displayChat(sender);
            }

            plugin.reload();
            Utils.notifyAdmins(sender.getName()+CommonColors.MESSAGE+" reloaded CoRE.");
            return true;
        }

        return Error.INVALID_ARGS.displayActionBar(sender);
    }


    //// Tab completion
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if(args.length==1 && sender.hasPermission("core.admin")) return Arrays.asList("version","reload");

        return Arrays.asList("");
	}
}