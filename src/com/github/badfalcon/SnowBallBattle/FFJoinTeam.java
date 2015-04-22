package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class FFJoinTeam {

	FrozenFight plugin;

	public FFJoinTeam(FrozenFight plugin) {
		this.plugin = plugin;
	}

	public void joinTeam(Player player, String teamName) {
		Scoreboard board = FrozenFight.board;
		player.setScoreboard(board);

		Team jointeam = board.getTeam(teamName);

		jointeam.addPlayer(player);
		player.setMetadata("TeamName",
				new FixedMetadataValue(plugin, teamName));
		player.sendMessage(FrozenFight.messagePrefix + "あなたはチーム"
				+ jointeam.getPrefix() + jointeam.getName().toString()
				+ jointeam.getSuffix() + "へ参加しました。");
		FrozenFight.board.getObjective("Pscore").getScore(player)
				.setScore(0);
	}

	public void joinRandomTeam(Player player) {

		List<String> spectatorList = plugin.getConfig().getStringList(
				"Spectator.List");

		Scoreboard board = FrozenFight.board;
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
					player.sendMessage(FrozenFight.messagePrefix + "あなたはチーム"
							+ jointeam.getPrefix()
							+ jointeam.getName().toString()
							+ jointeam.getSuffix() + "へ参加しました。");
					FrozenFight.board.getObjective("Pscore")
							.getScore(player).setScore(0);
					break;
				}
			}
		} else {
			player.sendMessage(FrozenFight.messagePrefix + "あなたは観戦者です。");
		}
	}
}
