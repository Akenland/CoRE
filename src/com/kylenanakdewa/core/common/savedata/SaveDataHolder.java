package com.kylenanakdewa.core.common.savedata;

import org.bukkit.plugin.Plugin;

/**
 * Represents an object that can hold Data.
 */
public interface SaveDataHolder {

    /**
	 * Gets a plugin's data section for this object.
	 * <p>
	 * Each plugin can save a single Data object in this object's data.
	 * @param plugin the plugin for which to retrieve data
	 * @return the plugin's data
	 */
    public SaveDataSection getData(Plugin plugin);

    /**
     * Saves your plugin's data section for this object.
     * <p>
     * Each plugin can save a single Data object in this object's data. If data already exists in this object, it will be overwritten.
	 * @param plugin the plugin for which to save data
	 * @param data the plugin's data
     */

}