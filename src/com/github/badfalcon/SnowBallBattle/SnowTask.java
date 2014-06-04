package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

public class SnowTask extends BukkitRunnable {

	private SnowBallBattle plugin;
	BukkitTask send;
	Spectator spec;

	public SnowTask(SnowBallBattle plugin) {
		this.plugin = plugin;
		spec = new Spectator(plugin);
	}


	public void run() {
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
			/*
			 * score[i] = teamscores[i][0]; if (score[i].getScore() > bestscore)
			 * { best = i; }
			 */
			if (teamscores[i].getScore() > bestscore) {
				bestTeamId = new ArrayList<Integer>();
				bestTeamId.add(i);
				bestscore = teamscores[i].getScore();
			} else if (teamscores[i].getScore() == bestscore) {
				bestTeamId.add(i);
			}
		}
		plugin.getServer().broadcastMessage("[雪合戦]  ゲームが終了しました。");
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
		Player[] players = plugin.getServer().getOnlinePlayers();
		ItemStack[] clear = { new ItemStack(Material.AIR),
				new ItemStack(Material.AIR), new ItemStack(Material.AIR),
				new ItemStack(Material.AIR) };
		for (Player player : players) {
			if (!spec.isSpectating(player)) {
				player.getInventory().setArmorContents(clear);
				player.getInventory().clear();
				player.setLevel(0);
				player.setExp(0);
			}
			for (Player player1 : players) {
				if (!player.canSee(player1)) {
					player.showPlayer(player1);
				}
			}
			player.sendMessage("[雪合戦]  ロビーへ転送します");
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
			for (Player player : players) {
				new SnowLobby(plugin).warpLobby(player);
				if (spec.isSpectating(player)) {
					spec.removeSpectate(player);
				}
			}
			SnowScoreboard snowboard = new SnowScoreboard(plugin);
//			snowboard.hideScore();
			snowboard.resetScore();
			snowboard.removePlayers();
		}
	}
}
