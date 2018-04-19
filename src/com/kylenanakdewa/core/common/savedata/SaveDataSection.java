package com.kylenanakdewa.core.common.savedata;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents Save Data that can be stored in a SaveDataHolder.
 */
public class SaveDataSection {

	/** The data section. */
	protected final ConfigurationSection data;

    public SaveDataSection(ConfigurationSection data){
        this.data = data;
	}
	public SaveDataSection(SaveDataSection data){
		this.data = data.data;
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

}