package com.gmail.badfalcon610.FrozenFight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class FFTeam {

	FrozenFight plugin;

	public FFTeam(FrozenFight plugin) {
		this.plugin = plugin;
	}

	public void joinTeam(Player player, String teamName) {
		Scoreboard board = FrozenFight.board;
		player.setScoreboard(board);

		Team jointeam = board.getTeam(teamName);

		jointeam.addPlayer(player);
		player.setMetadata("TeamName", new FixedMetadataValue(plugin, teamName));
		player.sendMessage(FrozenFight.messagePrefix + "あなたはチーム"
				+ jointeam.getPrefix() + jointeam.getName().toString()
				+ jointeam.getSuffix() + "へ参加しました。");
		FrozenFight.board.getObjective("Pscore").getScore(player).setScore(0);
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
					String teamName = teamNames.get(teamnumber);
					player.setMetadata("TeamName", new FixedMetadataValue(
							plugin, teamName));
					player.sendMessage(FrozenFight.messagePrefix + "あなたはチーム"
							+ jointeam.getPrefix()
							+ jointeam.getName().toString()
							+ jointeam.getSuffix() + "へ参加しました。");
					FrozenFight.board.getObjective("Pscore").getScore(player)
							.setScore(0);
					World world = Bukkit.getWorlds().get(0);
					double spawnx = world.getMetadata(teamName + "Resx").get(0)
							.asDouble();
					double spawny = world.getMetadata(teamName + "Resy").get(0)
							.asDouble();
					double spawnz = world.getMetadata(teamName + "Resz").get(0)
							.asDouble();
					float spawnyaw = world.getMetadata(teamName + "Resyaw")
							.get(0).asFloat();
					Location respawn = new Location(world, spawnx, spawny + 1,
							spawnz, spawnyaw, 0);
					player.setMetadata("res", new FixedMetadataValue(plugin,
							respawn));
					break;
				}
			}
		} else {
			player.sendMessage(FrozenFight.messagePrefix + "あなたは観戦者です。");
		}
	}

	public static void warpToTeamSpawn(Player player) {
		World world = player.getWorld();
		String TeamName = player.getMetadata("TeamName").get(0).asString();
		double spawnx = world.getMetadata(TeamName + "Resx").get(0).asDouble();
		double spawny = world.getMetadata(TeamName + "Resy").get(0).asDouble();
		double spawnz = world.getMetadata(TeamName + "Resz").get(0).asDouble();
		float spawnyaw = world.getMetadata(TeamName + "Resyaw").get(0)
				.asFloat();
		Location respawn = new Location(world, spawnx, spawny + 1, spawnz,
				spawnyaw, 0);
		player.teleport(respawn);
	}
}
