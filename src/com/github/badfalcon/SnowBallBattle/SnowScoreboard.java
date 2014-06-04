package com.github.badfalcon.SnowBallBattle;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class SnowScoreboard {

	SnowBallBattle plugin;
	Scoreboard board;

	public SnowScoreboard(SnowBallBattle plugin) {
		this.plugin = plugin;
		this.board = SnowBallBattle.board;
	}

	World world = Bukkit.getWorlds().get(0);

	public void setScoreboard() {
		String[] stringteams = plugin.getConfig()
				.getStringList("Team.TeamNames").toArray(new String[0]);
		String[] teamcolors = (String[]) plugin.getConfig()
				.getStringList("Team.TeamColors").toArray(new String[0]);
		Team[] teams = new Team[stringteams.length];
		Objective Tscore = board.registerNewObjective("Tscore", "dummy");
		Objective Pscore = board.registerNewObjective("Pscore", "dummy");
		Tscore.setDisplayName("Team Score");
		Pscore.setDisplayName("points");
		for (int i = 0; i < stringteams.length; i++) {
			teams[i] = board.registerNewTeam(stringteams[i]);
			teamcolors[i] = ChatColor.translateAlternateColorCodes('&',
					teamcolors[i]);
			teams[i].setPrefix(teamcolors[i]);
			teams[i].setSuffix(ChatColor.RESET.toString());
			teams[i].setAllowFriendlyFire(false);
			if (plugin.getConfig().contains(stringteams[i] + "respawn")) {
				Vector res = plugin.getConfig().getVector(
						stringteams[i] + "respawn");
				float resyaw = plugin.getConfig()
						.getFloatList(stringteams[i] + "yaw").get(0);
				world.setMetadata(stringteams[i] + "resx",
						new FixedMetadataValue(plugin, res.getX()));
				world.setMetadata(stringteams[i] + "resy",
						new FixedMetadataValue(plugin, res.getY()));
				world.setMetadata(stringteams[i] + "resz",
						new FixedMetadataValue(plugin, res.getZ()));
				world.setMetadata(stringteams[i] + "resyaw",
						new FixedMetadataValue(plugin, resyaw));
				world.setMetadata(stringteams[i] + "set",
						new FixedMetadataValue(plugin, true));
			}
		}
	}

	public void resetScore() {
		Objective Pscores = board.getObjective("Pscore");
		for (Player player : Bukkit.getOnlinePlayers()) {
			Score Pscore = Pscores.getScore(player);
			Pscore.setScore(0);
		}
		Objective Tscores = board.getObjective("Tscore");
		String[] teamNames = (String[]) plugin.getConfig()
				.getStringList("Team.TeamNames").toArray(new String[0]);
		String[] teamColors = (String[]) plugin.getConfig()
				.getStringList("Team.TeamColors").toArray(new String[0]);
		for (int i = 0; i < teamNames.length; i++) {
			Score teamscore = Tscores.getScore(Bukkit
					.getOfflinePlayer(ChatColor.translateAlternateColorCodes(
							'&', teamColors[i] + teamNames[i])));
			teamscore.setScore(0);
		}
	}

	public void showScore() {
		Objective Pscore = board.getObjective("Pscore");
		Objective Tscore = board.getObjective("Tscore");
		Pscore.setDisplaySlot(DisplaySlot.BELOW_NAME);
		Tscore.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public void hideScore() {
		Objective Pscore = board.getObjective("personalscore");
		Objective Tscore = board.getObjective("score");
		Pscore.setDisplaySlot(null);
		Tscore.setDisplaySlot(null);
	}

	public void removePlayers() {
		Set<Team> teams = board.getTeams();
		for (Team team : teams) {
			Set<OfflinePlayer> players = team.getPlayers();
			for (OfflinePlayer player : players) {
				team.removePlayer(player);
			}
		}
	}

}
