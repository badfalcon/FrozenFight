package com.github.badfalcon.SnowBallBattle;

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
		for (Player player : players) {
			if (!player.getMetadata("spectator").get(0).asBoolean()) {
				player.getInventory().setArmorContents(clear);
				player.getInventory().clear();
			}
		}
	}
}
