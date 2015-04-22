package com.github.badfalcon.SnowBallBattle;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class FFRunnableStart extends BukkitRunnable {

	FrozenFight plugin;
	FFSpectator spec;
	Player[] players;

	public FFRunnableStart(FrozenFight plugin) {
		this.plugin = plugin;
		spec = new FFSpectator(plugin);
		players = Bukkit.getOnlinePlayers();
	}

	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
		}
		plugin.getServer().broadcastMessage(
				FrozenFight.messagePrefix + "ゲームを開始します。");

		List<String> TeamNames = plugin.getConfig().getStringList("Team.Names");
		HashMap<String, ItemStack[]> armors = new HashMap<String, ItemStack[]>();
		for (String teamName : TeamNames) {
			String ConfigArmorType = plugin.getConfig().getString(
					teamName + ".Armor");
			ItemStack[] armor = new ItemStack[4];
			armor[0] = new ItemStack(Material.getMaterial(ConfigArmorType
					+ "_BOOTS"));
			armor[1] = new ItemStack(Material.getMaterial(ConfigArmorType
					+ "_LEGGINGS"));
			armor[2] = new ItemStack(Material.getMaterial(ConfigArmorType
					+ "_CHESTPLATE"));
			armor[3] = new ItemStack(Material.getMaterial(ConfigArmorType
					+ "_HELMET"));
			armors.put(teamName, armor);
		}

		ItemStack[] sb = new ItemStack[36];

		// sb[8] = new ItemStack(Material.COMPASS);

		int i = 0;
		int n = 0;
		while (n < plugin.getConfig().getInt("Game.SnowBallStacks")) {
			if (sb[i] == null) {
				sb[i] = new ItemStack(Material.SNOW_BALL, 16);
				n++;
			}
			i++;
		}
		Bukkit.getWorlds().get(0).removeMetadata("ready", plugin);
		double specHeight = plugin.getConfig().getDouble("Spectator.Height");
		for (Player player : players) {
			if (!FFSpectator.isSpectating(player)) {
				if (player.getGameMode().equals(GameMode.CREATIVE)) {
					player.setGameMode(GameMode.SURVIVAL);
				}
				player.setLevel(0);
				String teamName = player.getMetadata("TeamName").get(0)
						.asString();
				player.getInventory().clear();
				player.getInventory().setArmorContents(armors.get(teamName));
				player.getInventory().setContents(sb);
				player.setWalkSpeed(0.2F);
			} else {
				Location l = player.getLocation();
				if (l.getY() < specHeight) {
					l.setY(specHeight);
					player.teleport(l);
				}
			}
		}
		for (Player player : players) {
			if (!spec.isSpectator(player.getName())) {
				for (Player player2 : players) {
					if (!FFSpectator.isSpectating(player2)) {
						player.showPlayer(player2);
					}
				}
			}
		}

		Bukkit.getWorlds().get(0)
				.setMetadata("ingame", new FixedMetadataValue(plugin, true));
		long startTime = new Date().getTime();
		Bukkit.getWorlds()
				.get(0)
				.setMetadata("gameStart",
						new FixedMetadataValue(plugin, startTime));
	}
}
