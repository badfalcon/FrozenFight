package com.github.badfalcon.SnowBallBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;

public class SnowTask extends BukkitRunnable {

	private final JavaPlugin plugin;

	public SnowTask(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void run() {
		String[] steams = (String[]) plugin.getConfig()
				.getStringList("Team.TeamNames").toArray(new String[0]);
		String[] teamcolors = (String[]) plugin.getConfig()
				.getStringList("Team.TeamColors").toArray(new String[0]);
		Score[][] teamscores = new Score[steams.length][0];
		Score[] score = new Score[steams.length];
		int best = 0;
		int bestscore = 0;
		for (int i = 0; i < steams.length; i++) {
			teamcolors[i] = ChatColor.translateAlternateColorCodes('&',
					teamcolors[i]);
			teamscores[i] = Bukkit.getPlayer(teamcolors[i] + steams[i])
					.getScoreboard()
					.getScores(Bukkit.getPlayer(teamcolors[i] + steams[i]))
					.toArray(new Score[0]);
			score[i] = teamscores[i][0];
			if(score[i].getScore() > bestscore){
				best = i;
			}
		}
		plugin.getServer().broadcastMessage("ゲームが終了しました。");
		plugin.getServer().broadcastMessage("チーム" + steams[best] + "の勝利です！　　　score:" + bestscore);
		Player[] players = plugin.getServer().getOnlinePlayers();
		ItemStack[] clear = { new ItemStack(Material.AIR),
				new ItemStack(Material.AIR), new ItemStack(Material.AIR),
				new ItemStack(Material.AIR) };
		for (Player player : players) {
			if (!player.getMetadata("spectator").get(0).asBoolean()) {
				player.getInventory().setArmorContents(clear);
				player.getInventory().clear();
			}
		}
	}
}
