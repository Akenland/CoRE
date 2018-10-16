package com.kylenanakdewa.core.common.savedata;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Represents Save Data that can be stored in a SaveDataHolder.
 */
public class SaveDataSection {

	/** The data section. */
	protected final ConfigurationSection data;
	/** The plugin that owns this save data. */
    protected final Plugin plugin;

    public SaveDataSection(ConfigurationSection data, Plugin plugin){
		this.data = data;
		this.plugin = plugin;
	}
	public SaveDataSection(SaveDataSection data){
		this.data = data.data;
		this.plugin = data.plugin;
	}


	public ConfigurationSection getData(){
		return data;
	}

    /**
	 * Saves all data.
	 * <p>
	 * You should not call this method unless there is actually data that needs to be saved.
	 * If your plugin does not need to persist data, do not call this method.
	 * Small amounts of easily re-creatable data should not be saved (but can still be set in the data section).
	 */
    public void save(){
		
	}

	/**
     * Gets the Plugin that owns this save data.
     * @return the Plugin that stored this data
     */
    protected final Plugin getPlugin(){
        return plugin;
    }


	/**
	 * Gets a SaveDataSection with the summed values of all specified sections.
	 * This can be used to "inherit" values across a number of data sections.
	 * Later sections will override earlier sections.
	 */
	public static SaveDataSection mergeData(SaveDataSection... data){
		MemoryConfiguration mergedData = new MemoryConfiguration();
		mergedData.options().copyDefaults(true);
		for(SaveDataSection section : data) mergedData.addDefaults(section.getData().getValues(true));

		return new SaveDataSection(mergedData, data[data.length-1].getPlugin());
	}
}