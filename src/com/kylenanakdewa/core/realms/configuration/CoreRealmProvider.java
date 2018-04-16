package com.kylenanakdewa.core.realms.configuration;

import com.kylenanakdewa.core.CorePlugin;
import com.kylenanakdewa.core.common.ConfigAccessor;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

/**
 * A Realm Provider for the default realms.yml file included with Project CoRE.
 */
public class CoreRealmProvider extends ConfigRealmProvider implements Listener {

    private static final ConfigAccessor REALMS_CONFIG_ACCESSOR = new ConfigAccessor("realms.yml");

    /**
	 * Creates a RealmProvider for the default realms.yml file in CoRE.
	 */
	public CoreRealmProvider(CorePlugin plugin){
        super(REALMS_CONFIG_ACCESSOR.getConfig());
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Auto-saves the realms.yml file when the world is saved.
     */
    @EventHandler
    public void autoSave(WorldSaveEvent event){
        save();
    }

    /**
     * Saves the realms.yml file. This will remove changes 
     */
    public void save(){
        REALMS_CONFIG_ACCESSOR.saveConfig();
    }
    /**
     * Reloads the realms.yml file. This will remove changes in memory.
     */
    public void reload(){
        REALMS_CONFIG_ACCESSOR.reloadConfig();
    }

}