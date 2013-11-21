package com.github.badfalcon.SnowBallBattle;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowTask extends BukkitRunnable {

	private final JavaPlugin plugin;

	public SnowTask(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void run() {
		plugin.getServer().broadcastMessage("ゲームが終了しました。");
		Player[] players = plugin.getServer().getOnlinePlayers();
		ItemStack[] clear = { new ItemStack(Material.AIR),
				new ItemStack(Material.AIR), new ItemStack(Material.AIR),
				new ItemStack(Material.AIR) };
		List<String> spectatorstringlist = plugin.getConfig().getStringList(
				"Game.Spectators");
		for (Player player : players) {
			if (!spectatorstringlist.contains(player.getName())) {
				player.getInventory().setArmorContents(clear);
				player.getInventory().clear();
			}
		}
	}
}
