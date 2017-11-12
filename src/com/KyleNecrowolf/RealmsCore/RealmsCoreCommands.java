package com.KyleNecrowolf.RealmsCore;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Permissions.PermsUtils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

public final class RealmsCoreCommands implements TabExecutor {
    
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Version command
        if(args.length==0 || args[0].equalsIgnoreCase("version")){
            Prompt prompt = new Prompt();
            prompt.addQuestion("&rRealmsCore "+Main.plugin.getDescription().getVersion()+" by Kyle Necrowolf");
            prompt.addAnswer("A roleplaying framework and essential server functions, for small/medium survival and roleplay servers.","");
            prompt.addAnswer("Website: http://WolfiaMC.com/plugins", "url_http://WolfiaMC.com/plugins");

            // Plugin configuration info
            if(sender.hasPermission("realms.admin")){
                prompt.addAnswer("Server running compatible version: "+Utils.isLatestVersion()+" (click for details)", "command_version");
                prompt.addAnswer("Permissions enabled: "+ConfigValues.permsEnabled, "");
                prompt.addAnswer("Chat formatting enabled: "+ ConfigValues.formatChat, "");
                if(ConfigValues.enableWolfiaFeatures)
                    prompt.addAnswer("Realms of Wolfia functionality enabled: "+ConfigValues.enableWolfiaFeatures, "");
            }

            prompt.display(sender);
			return true;
        }

        // Reload command
        if(args[0].equalsIgnoreCase("reload")){
            // Check permissions
            if(!sender.hasPermission("realms.reload") || !PermsUtils.isDoubleCheckedAdmin(sender)){
                Utils.notifyAdminsError(sender.getName()+Utils.errorText+" failed security check (reloading RealmsCore config).");
                return Error.NO_PERMISSION.displayChat(sender);
            }

            ConfigValues.reloadConfig();
            Utils.notifyAdmins(sender.getName()+Utils.messageText+" reloaded the RealmsCore config.");
            return true;
        }

        return Error.INVALID_ARGS.displayActionBar(sender);
    }


    //// Tab completion
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if(args.length==1 && sender.hasPermission("realms.admin")) return Arrays.asList("version","reload");

        return Arrays.asList("");
	}
}