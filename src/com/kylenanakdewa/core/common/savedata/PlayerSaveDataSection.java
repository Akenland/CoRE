package com.kylenanakdewa.core.common.savedata;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

/**
 * Represents plugin save data that can be stored with a PlayerCharacter.
 * <p>
 * Extend this class with your own plugin's methods, for easy saving and loading of data for PlayerCharacters.
 */
public abstract class PlayerSaveDataSection extends SaveDataSection {

    /** The PlayerCharacter that this save data is for. */
    protected final PlayerCharacter character;


    /**
     * Creates or retrieves a SaveDataSection for the specified player and plugin.
     * @param player the PlayerCharacter to save data for
     * @param plugin the plugin that is saving data
     */
    public PlayerSaveDataSection(PlayerCharacter player, Plugin plugin){
        super(player.getData(plugin), "");
        this.character = player;
    }

    /**
     * Creates or retrieves a SaveDataSection for the specified player and plugin.
     * @param player the Player to save data for
     * @param plugin the plugin that is saving data
     */
    public PlayerSaveDataSection(OfflinePlayer player, Plugin plugin){
        this(PlayerCharacter.getCharacter(player), plugin);
    }


    /**
     * Gets the PlayerCharacter that this data is saved for.
     * @return the PlayerCharacter where this data is stored
     */
    public PlayerCharacter getCharacter(){
        return character;
    }

    @Override
    public void save(){
        character.saveData();
    }

}