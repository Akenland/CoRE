package com.kylenanakdewa.core.realms.scoreboard;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.realms.Realm;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/**
 * A Realm backed by a {@link org.bukkit.scoreboard.Scoreboard} {@link Team}.
 * <p>
 * ScoreboardRealms are created by a {@link ScoreboardRealmProvider}, which is linked to a particular Scoreboard.
 * <p>
 * This type of Realm does not support taglines, or parent/child realms.
 * @author Kyle Nanakdewa
 */
public class ScoreboardRealm implements Realm {

	/** The team that backs this Realm. */
	private final Team team;

	/**
	 * Creates a new Realm backed by the specified Scoreboard Team.
	 * @param team the Scoreboard team
	 */
	ScoreboardRealm(Team team){
		this.team = team;
	}

	@Override
	public String getIdentifier() {
		return team.getName();
	}

	@Override
	public String getName() {
		return team.getDisplayName();
	}

	@Override
	public void setName(String name) {
		team.setDisplayName(name);
	}

	@Override
	public String getTagline(){
		// Scoreboard realms cannot have taglines
		return null;
	}

	@Override
	public void setTagline(String tagline){
		// Scoreboard realms cannot have taglines
		return;
	}

	@Override
	public ChatColor getColor() {
		return team.getColor();
	}

	@Override
	public void setColor(ChatColor color) {
		team.setColor(color);
	}

	@Override
	public Realm getParentRealm(){
		// Scoreboard realms cannot have parent realms
		return null;
	}

	@Override
	public void setParentRealm(Realm realm){
		// Scoreboard realms cannot have parent realms
		return;
	}

	@Override
	public List<Realm> getAllParentRealms(){
		// Scoreboard realms cannot have parent realms
		return null;
	}

	@Override
	public Realm getTopParentRealm(){
		// Scoreboard realms cannot have parent realms
		return null;
	}

	@Override
	public Collection<Realm> getChildRealms(){
		// Scoreboard realms cannot have child realms
		return new HashSet<Realm>();
	}

	@Override
	public Collection<PlayerCharacter> getOnlineCharacters(){
		Collection<PlayerCharacter> characters = new HashSet<PlayerCharacter>();
		getOnlinePlayers().forEach(player -> characters.add(PlayerCharacter.getCharacter(player)));
		return characters;
	}

	@Override
	public Collection<Player> getOnlinePlayers(){
		Collection<Player> teamMembers = new HashSet<Player>();
		// Iterate through all team members and collect the ones who are online
		for(String entry : team.getEntries()){
			Player player = Bukkit.getPlayerExact(entry);
			if(player!=null && player.isOnline())
				teamMembers.add(player);
		}
		return teamMembers;
	}


	@Override
	public void addPlayer(PlayerCharacter character){
		team.addEntry(character.getUsername());
	}
	@Override
	public void addPlayer(Player player){
		team.addEntry(player.getName());
	}

	@Override
	public void removePlayer(PlayerCharacter character){
		team.removeEntry(character.getUsername());
	}
	@Override
	public void removePlayer(Player player){
		team.removeEntry(player.getName());
	}

}