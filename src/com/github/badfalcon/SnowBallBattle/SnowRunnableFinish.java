package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public class SnowRunnableFinish extends BukkitRunnable {

	private SnowBallBattle plugin;
	BukkitTask send;
	Spectator spec;

	public SnowRunnableFinish(SnowBallBattle plugin) {
		this.plugin = plugin;
		spec = new Spectator(plugin);
	}

	public void run() {
		/*
		String[] steams = (String[]) plugin.getConfig()
				.getStringList("Team.TeamNames").toArray(new String[0]);
		String[] teamcolors = (String[]) plugin.getConfig()
				.getStringList("Team.TeamColors").toArray(new String[0]);
		Score[] teamscores = new Score[steams.length];
		// Score[] score = new Score[steams.length];
		int bestscore = 0;
		List<Integer> bestTeamId = new ArrayList<Integer>();
		for (int i = 0; i < steams.length; i++) {
			teamcolors[i] = ChatColor.translateAlternateColorCodes('&',
					teamcolors[i]);
			teamscores[i] = Bukkit.getOnlinePlayers()[0]
					.getScoreboard()
					.getObjective(DisplaySlot.SIDEBAR)
					.getScore(
							Bukkit.getOfflinePlayer(teamcolors[i] + steams[i]));
			if (teamscores[i].getScore() > bestscore) {
				bestTeamId = new ArrayList<Integer>();
				bestTeamId.add(i);
				bestscore = teamscores[i].getScore();
			} else if (teamscores[i].getScore() == bestscore) {
				bestTeamId.add(i);
			}
		}
		if (bestTeamId.size() == 1) {
			plugin.getServer().broadcastMessage(
					"[雪合戦]  チーム" + teamcolors[bestTeamId.get(0)]
							+ steams[bestTeamId.get(0)]
							+ ChatColor.RESET.toString() + "の勝利です！");
		} else {
			String winTeams = "";
			for (int best : bestTeamId) {
				if (best == bestTeamId.get(bestTeamId.size() - 1)) {
					winTeams += teamcolors[best] + steams[best]
							+ ChatColor.RESET.toString();
				} else {
					winTeams += teamcolors[best] + steams[best]
							+ ChatColor.RESET.toString() + ",";
				}
			}
			plugin.getServer().broadcastMessage(
					"[雪合戦]  チーム" + winTeams + "による同点に終わりました。");
		}
		plugin.getServer().broadcastMessage("[雪合戦]  score:" + bestscore);
		*/
		Player[] players = plugin.getServer().getOnlinePlayers();
		ItemStack[] clear = { new ItemStack(Material.AIR),
				new ItemStack(Material.AIR), new ItemStack(Material.AIR),
				new ItemStack(Material.AIR) };

		//新スコア表示

		List<OfflinePlayer> winnerTeams = new ArrayList<OfflinePlayer>();
		List<String> teamNames = plugin.getConfig()
				.getStringList("Team.Names");
		int winnerscore = 0;
		for (String teamName : teamNames) {
			Team t = SnowBallBattle.board.getTeam(teamName);
			OfflinePlayer team = Bukkit.getOfflinePlayer(t.getPrefix() + teamName + t.getSuffix());
			Score tsc = SnowBallBattle.board.getObjective("Tscore").getScore(team);
			if (tsc.getScore() > winnerscore) {
				winnerTeams = new ArrayList<OfflinePlayer>();
				winnerTeams.add(team);
				winnerscore = tsc.getScore();
			} else if (tsc.getScore() == winnerscore) {
				winnerTeams.add(team);
			}
		}
		String winnerNames = "";
		for (OfflinePlayer winner : winnerTeams) {
			Team winnerTeam = SnowBallBattle.board.getTeam(ChatColor.stripColor(winner.getName()));
			String winnerName = winnerTeam.getPrefix() + winner.getName() + winnerTeam.getSuffix();
			winnerNames += winnerName;
			if (!winner.equals(winnerTeams.get(winnerTeams.size() - 1))) {
				winnerNames += ",";
			}
		}

		List<OfflinePlayer> mvpPlayers = new ArrayList<OfflinePlayer>();
		int mvpscore = 0;
		for (Player player : players) {
			if (!spec.isSpectating(player)) {
				Score sc = SnowBallBattle.board.getObjective("Pscore")
						.getScore(player);
				if (sc.getScore() > mvpscore) {
					mvpPlayers = new ArrayList<OfflinePlayer>();
					mvpPlayers.add(player);
					mvpscore = sc.getScore();
				} else if (sc.getScore() == mvpscore) {
					mvpPlayers.add(player);
				}
			}
		}
		String mvpNames = "";
		for (OfflinePlayer mvp : mvpPlayers) {
			Team mvpTeam = Bukkit.getPlayer(mvp.getName()).getScoreboard()
					.getTeam(mvp.getPlayer().getMetadata("TeamName").get(0).asString());
			String mvpName = mvpTeam.getPrefix() + mvp.getName() + mvpTeam.getSuffix();
			mvpNames += mvpName;
			if (!mvp.equals(mvpPlayers.get(mvpPlayers.size() - 1))) {
				mvpNames += ",";
			}
		}

		plugin.getServer().broadcastMessage(SnowBallBattle.messagePrefix + "ゲームが終了しました。");
		if (winnerTeams.size() == 1) {
			plugin.getServer().broadcastMessage(
					SnowBallBattle.messagePrefix + "チーム" + winnerNames + "の勝利です！ スコア:" + winnerscore + "pt");
		} else {
			plugin.getServer().broadcastMessage(
					SnowBallBattle.messagePrefix + "チーム" + winnerNames + "による同点に終わりました。 スコア:" + winnerscore + "pt");
		}

		plugin.getServer().broadcastMessage(
				SnowBallBattle.messagePrefix + "この試合のMVPは" + mvpNames + "でした。スコア:" + mvpscore + "pt");

		for (Player player : players) {
			if (!spec.isSpectating(player)) {

				//個人成績の表示とゲームデータクリア

				Score personal = SnowBallBattle.board.getObjective("Pscore").getScore(player);
				SnowBallBattle.board.getTeam(player.getMetadata("TeamName").get(0).asString()).removePlayer(player);
				player.sendMessage(SnowBallBattle.messagePrefix + "あなたのスコアは " + personal.getScore() + "pt でした。");
				player.getInventory().setArmorContents(clear);
				player.getInventory().clear();
				player.setLevel(0);
				player.setExp(0);
				SnowBallBattle.board.getObjective("Tscore").setDisplayName("Time  finished");
			}
			for (Player player1 : players) {
				if (!player.canSee(player1)) {
					player.showPlayer(player1);
				}
			}
			player.sendMessage(SnowBallBattle.messagePrefix + "ロビーへ転送します。");
		}
		World world = Bukkit.getWorlds().get(0);
		world.removeMetadata("ingame", plugin);
		send = new sendToLobby().runTaskLater(this.plugin, 60);
	}

	public class sendToLobby extends BukkitRunnable {

		public sendToLobby() {

		}

		public void run() {
			Player[] players = plugin.getServer().getOnlinePlayers();
			SnowScoreboard snowboard = new SnowScoreboard(plugin);
			snowboard.hideScore();
			snowboard.resetScore();
			for (Player player : players) {
				if (spec.isSpectator(player.getName())) {
					spec.removeSpectate(player);
				}
				new SnowLobby(plugin).warpLobby(player);
			}
		}
	}
}
