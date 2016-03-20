package com.gmail.badfalcon610.FrozenFight;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;

class FFGameCountdown extends BukkitRunnable {
	private int maxtime;
	private int gametime;
	private int maxFillTime;
	private int giveSnowNum;
	private float decreaseExp;
	private float exp;

	public FFGameCountdown(FrozenFight plugin) {
		maxtime = plugin.getConfig().getInt("Game.GameTime") * 60;
		gametime = plugin.getConfig().getInt("Game.GameTime") * 60;
		maxFillTime = plugin.getConfig().getInt("Game.GiveSnowBallTime");
		giveSnowNum = plugin.getConfig().getInt("Game.GiveSnowBallNum");
		exp = 1.0f;
		decreaseExp = (float) (1.0f / maxFillTime);
	}

	public void run() {
		int gamemin = gametime / 60;
		int gamesec = gametime % 60;

		String gamesecString;
		if (gamesec < 10) {
			gamesecString = "0" + String.valueOf(gamesec);
		} else {
			gamesecString = String.valueOf(gamesec);
		}

		if (gametime == 60) {
			Bukkit.getServer().broadcastMessage(
					FrozenFight.messagePrefix + "----終了1分前----");
			FFScoreboard.hideScore();
		}

		Player[] onlinePlayers = Bukkit.getOnlinePlayers();
		float currentExp = onlinePlayers[0].getExp();
		for (Player player : onlinePlayers) {

			Score time = FrozenFight.board.getObjective("Tscore").getScore(
					Bukkit.getOfflinePlayer("Time"));
			time.setScore(gametime);

			BarAPI.setMessage(player, "残り時間  " + gamemin + ":" + gamesecString);
			BarAPI.setHealth(player, (float) gametime / (float) maxtime * 100F);
			if (gametime <= 10) {
				player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
			}
			if (gametime == 60) {
				player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1,
						1);
			}

			if (currentExp - decreaseExp <= 0.0f) {
				if (gametime != maxtime && gametime != 0) {
					if (!FFSpectator.isSpectating(player)) {

						PlayerInventory inventory = player.getInventory();
						inventory.addItem(new ItemStack(Material.SNOW_BALL,
								giveSnowNum));
						player.playSound(player.getLocation(),
								Sound.CHICKEN_EGG_POP, 1, 1);
					}
				}
				player.setExp(1.0f);
			} else {
				player.setExp(currentExp - decreaseExp);
			}
		}
		if (gametime > 0) {
			gametime--;
		} else {
			for (Player player : onlinePlayers) {
				BarAPI.removeBar(player);
			}
			cancel();
		}
	}
}
