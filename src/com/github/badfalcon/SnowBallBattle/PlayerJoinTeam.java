package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerJoinTeam {

	SnowBallBattle plugin;

	public PlayerJoinTeam(SnowBallBattle plugin) {
		this.plugin = plugin;
	}

	public void joinTeam(Player player, String teamName) {
		Scoreboard board = SnowBallBattle.board;
		player.setScoreboard(board);

		Team jointeam = board.getTeam(teamName);

		jointeam.addPlayer(player);
		player.setMetadata("TeamName",
				new FixedMetadataValue(plugin, teamName));
		player.sendMessage(SnowBallBattle.messagePrefix + "あなたはチーム"
				+ jointeam.getPrefix() + jointeam.getName().toString()
				+ jointeam.getSuffix() + "へ参加しました。");
		SnowBallBattle.board.getObjective("Pscore").getScore(player)
				.setScore(0);
	}

	public void joinRandomTeam(Player player) {

		List<String> spectatorList = plugin.getConfig().getStringList(
				"Spectator.List");

		Scoreboard board = SnowBallBattle.board;
		player.setScoreboard(board);

		if (!spectatorList.contains(player.getName())) {

			List<String> teamNames = plugin.getConfig().getStringList(
					"Team.Names");
			List<Integer> teamsizes = new ArrayList<Integer>();

			for (String team : teamNames) {
				teamsizes.add(board.getTeam(team).getSize());
			}
			Collections.sort(teamsizes);

			int leastTeam = teamsizes.get(0);
			while (true) {

				int teamnumber = (int) (Math.random() * teamNames.size());
				Team jointeam = board.getTeam(teamNames.get(teamnumber));

				if (jointeam.getSize() == leastTeam) {
					jointeam.addPlayer(player);
					player.setMetadata("TeamName", new FixedMetadataValue(
							plugin, teamNames.get(teamnumber)));
					player.sendMessage(SnowBallBattle.messagePrefix + "あなたはチーム"
							+ jointeam.getPrefix()
							+ jointeam.getName().toString()
							+ jointeam.getSuffix() + "へ参加しました。");
					SnowBallBattle.board.getObjective("Pscore")
							.getScore(player).setScore(0);
					break;
				}
			}
		} else {
			player.sendMessage(SnowBallBattle.messagePrefix + "あなたは観戦者です。");
		}
	}
}
