package com.kylenanakdewa.core.realms.scoreboard;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.realms.Realm;
import com.kylenanakdewa.core.realms.RealmProvider;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A RealmProvider that uses {@link Scoreboard} Teams.
 * <p>
 * A Realm is automatically created for every Team that exists on this Scoreboard.
 * A member of a Team is also a member of the corresponding Realm.
 * @author Kyle Nanakdewa
 */
public class ScoreboardRealmProvider implements RealmProvider {

	/** The scoreboard that contains the teams to be used as Realms. */
	private Scoreboard scoreboard;

	/** All realms represented by this scoreboard. */
	private Map<Team,ScoreboardRealm> realms = new HashMap<Team,ScoreboardRealm>(scoreboard.getTeams().size());


	/**
	 * Creates a RealmProvider for the specified Scoreboard.
	 * @param scoreboard the Scoreboard to track
	 */
	public ScoreboardRealmProvider(Scoreboard scoreboard){
		this.scoreboard = scoreboard;
		reloadTeams();
	}

	/**
	 * Refreshes the map with any new teams.
	 */
	private void reloadTeams(){
		// Remove teams that have been removed from this scoreboard
		realms.keySet().removeIf(team -> team.getScoreboard()!=scoreboard);

		// Add new teams
		scoreboard.getTeams().forEach(team -> realms.putIfAbsent(team, new ScoreboardRealm(team)));
	}


	@Override
	public Collection<? extends Realm> getAllRealms(){
		reloadTeams();
		return realms.values();
	}

	@Override
	public Realm getRealm(String identifier){
		reloadTeams();

		Team team = scoreboard.getTeam(identifier);
		return team!=null ? realms.get(team) : null;
	}

	@Override
	public Realm getCharacterRealm(Character character){
		// Scoreboards use usernames, not UUIDs, so this will NOT persist past username changes - Mojang refuses to support it
		String characterString = (character instanceof PlayerCharacter) ? ((PlayerCharacter)character).getUsername() : character.getName();

		// Check for a scoreboard team
		Team team = scoreboard.getEntryTeam(characterString);

		// Only return a realm if a team was found
		return team!=null ? realms.get(team) : null;
	}

	@Override
	public boolean isOfficer(Character character){
		// This provider does not support officers
		return false;
	}

	@Override
	public void setOfficer(Character character, boolean isOfficer) {
		// This provider does not support officers
	}

}