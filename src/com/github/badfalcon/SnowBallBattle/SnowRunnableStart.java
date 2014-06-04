package com.github.badfalcon.SnowBallBattle;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class SnowRunnableStart extends BukkitRunnable {

	SnowBallBattle plugin;
	Spectator spec;

	public SnowRunnableStart(SnowBallBattle plugin) {
		this.plugin = plugin;
		spec = new Spectator(plugin);
	}

	Player[] players = Bukkit.getOnlinePlayers();

	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1,
					1);
		}
		plugin.getServer().broadcastMessage("[雪合戦]  ゲームを開始します。");

		ItemStack[] sb = new ItemStack[36];
		String[] configarmor = plugin.getConfig()
				.getStringList("Team.TeamArmor").toArray(new String[0]);
		String[][] armors = new String[configarmor.length][4];
		ItemStack[][] armor = new ItemStack[configarmor.length][4];
		for (int i = 0; i < armors.length; i++) {
			armors[i][0] = configarmor[i] + "_BOOTS";
			armors[i][1] = configarmor[i] + "_LEGGINGS";
			armors[i][2] = configarmor[i] + "_CHESTPLATE";
			armors[i][3] = configarmor[i] + "_HELMET";
			for (int j = 0; j < 4; j++) {
				armor[i][j] = new ItemStack(Material.getMaterial(armors[i][j]));
			}
		}
		for (int i = 0; i < plugin.getConfig().getInt("Game.SnowBallStacks"); i++) {
			sb[i] = new ItemStack(Material.SNOW_BALL, 32);
		}
		for (Player player : players) {
			if (!spec.isSpectator(player.getName())) {
				if (player.getGameMode().equals(GameMode.CREATIVE)) {
					player.setGameMode(GameMode.SURVIVAL);
				}
				Bukkit.getWorlds().get(0).removeMetadata("ready", plugin);
				player.setLevel(0);
				player.setExp(0);
				int teamnumber = player.getMetadata("teamnumber").get(0)
						.asInt();
				player.getInventory().clear();
				player.getInventory().setArmorContents(armor[teamnumber]);
				player.getInventory().setContents(sb);
				player.setWalkSpeed(0.2F);
			} else {
				spec.setSpectate(player);
				Location l = player.getLocation();
				l.setY(plugin.getConfig().getDouble(
						"Spectator.Height"));
				player.teleport(l);
			}
		}
		for (Player player : players) {
			if (!spec.isSpectator(player.getName())) {
				for (Player player2 : players) {
					if (!spec.isSpectating(player2)) {
						player.showPlayer(player2);
					}
				}
			}
		}

		Bukkit.getWorlds().get(0).setMetadata("ingame",
				new FixedMetadataValue(plugin, true));
	}
}
