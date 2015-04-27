package com.gmail.badfalcon610.FrozenFight;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FFRunnableFinish extends BukkitRunnable {

	private FrozenFight plugin;
	BukkitTask send;
	FFSpectator spec;

	public FFRunnableFinish(FrozenFight plugin) {
		this.plugin = plugin;
		spec = new FFSpectator(plugin);
	}

	public void run() {

		Player[] players = plugin.getServer().getOnlinePlayers();
		ItemStack[] clear = { new ItemStack(Material.AIR),
				new ItemStack(Material.AIR), new ItemStack(Material.AIR),
				new ItemStack(Material.AIR) };

		plugin.getServer().broadcastMessage(
				FrozenFight.messagePrefix + "ゲームが終了しました。");

		for (Player player : players) {
			if (!FFSpectator.isSpectating(player)) {
				BarAPI.setMessage(player, "残り時間  finished");
			}
			for (Player player1 : players) {
				if (!player.canSee(player1)) {
					player.showPlayer(player1);
					player.getInventory().setArmorContents(clear);
					player.getInventory().clear();
					player.setLevel(0);
					player.setExp(0);
					player.removeMetadata("TeamName", plugin);
				}
			}
			player.sendMessage(FrozenFight.messagePrefix + "ロビーへ転送します。");
		}
		World world = Bukkit.getWorlds().get(0);
		world.removeMetadata("ingame", plugin);
		world.setMetadata("result", new FixedMetadataValue(plugin, true));
		send = new sendToLobby().runTaskLater(this.plugin, 60);

	}

	public class sendToLobby extends BukkitRunnable {

		public sendToLobby() {

		}

		public void run() {
			Player[] players = plugin.getServer().getOnlinePlayers();
			FFScoreboard snowboard = new FFScoreboard(plugin);
			FFScoreboard.hideScore();
			snowboard.resetScore();
			for (Player player : players) {
				BarAPI.removeBar(player);
				if (FFSpectator.isSpectating(player)) {
					spec.removeSpectate(player);
				}
				if (spec.isSpectator(player.getName())) {
					spec.removeSpectate(player);
				}
				new FFLobby(plugin).warpLobby(player);
			}
		}
	}
}
