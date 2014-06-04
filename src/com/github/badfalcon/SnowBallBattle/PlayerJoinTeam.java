package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerJoinTeam {

	SnowBallBattle plugin;

	public PlayerJoinTeam(SnowBallBattle plugin) {
		this.plugin = plugin;
	}

	public void joinTeam(Player player) {

		List<String> spectatorList = plugin.getConfig().getStringList(
				"Game.Spectators");

		Scoreboard board = SnowBallBattle.board;
		player.setScoreboard(board);

		if (!spectatorList.contains(player.getName())) {

			List<String> teams = plugin.getConfig().getStringList("Team.TeamNames");
			List<String> teamcolors = plugin.getConfig().getStringList(
					"Team.TeamColors");
			List<Integer> teamsizes = new ArrayList<Integer>();

			for (String team : teams) {
				teamsizes.add(board.getTeam(team).getSize());
			}
			Collections.sort(teamsizes);

			int leastTeam = teamsizes.get(0);
			int full = plugin.getConfig().getInt("Team.MaxPlayers");

			if (leastTeam < full) {
				while (true) {
					int teamnumber = (int) (Math.random() * teams.size());
					Team jointeam = board.getTeam(teams.get(teamnumber));
					if (jointeam.getSize() == leastTeam) {
						jointeam.addPlayer(player);
						player.setMetadata("team", new FixedMetadataValue(
								plugin, teams.get(teamnumber)));
						player.setMetadata("teamnumber",
								new FixedMetadataValue(plugin, teamnumber));
						player.setMetadata("teamcolor", new FixedMetadataValue(
								plugin, teamcolors.get(teamnumber)));
						player.setMetadata("spectator", new FixedMetadataValue(
								plugin, false));
						player.sendMessage(ChatColor
								.translateAlternateColorCodes('&', "&R[雪合戦]  あなたはチーム："
										+ teamcolors.get(teamnumber)
										+ jointeam.getName().toString()
										+ "&Rへ参加しました。"));
						break;
					}
				}
			} else {
				plugin.getLogger().info("人数が上限に達しました。");
			}
		} else {
			player.sendMessage("[雪合戦]  あなたは観戦者です。");
			player.setMetadata("spectating", new FixedMetadataValue(plugin,
					true));
		}
	}
}
