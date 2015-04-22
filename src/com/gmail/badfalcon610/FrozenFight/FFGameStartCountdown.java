package com.gmail.badfalcon610.FrozenFight;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

class FFGameStartCountdown extends BukkitRunnable {

	private int countdown;

	public FFGameStartCountdown(int count) {
		countdown = count;
	}

	public void run() {

		if (countdown < 10) {
			Player[] players = Bukkit.getOnlinePlayers();
			if (countdown <= 3) {
				for (Player player : players) {
					player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
				}
			}
			for (Player player : players) {
				player.sendMessage(FrozenFight.messagePrefix + "ゲーム開始まで "
						+ countdown);
			}
		} else {
			Bukkit.getServer().broadcastMessage(
					FrozenFight.messagePrefix + "ゲーム開始まで" + countdown);
		}

		if (countdown > 1) {
			countdown--;
		} else {
			cancel();
		}
	}
}
