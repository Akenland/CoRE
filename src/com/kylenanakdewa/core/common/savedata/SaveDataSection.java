package com.kylenanakdewa.core.common.savedata;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

/**
 * Represents Save Data that can be stored in a SaveDataHolder.
 */
public class SaveDataSection extends MemorySection {

    public SaveDataSection(ConfigurationSection parent, String path){
        super(parent, path);
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