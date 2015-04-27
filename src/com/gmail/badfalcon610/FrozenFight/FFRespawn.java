package com.gmail.badfalcon610.FrozenFight;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FFRespawn extends BukkitRunnable {

	Player player;
	FileConfiguration config;

	public FFRespawn(Player player, FileConfiguration config) {
		this.player = player;
		this.config = config;
	}

	@Override
	public void run() {
		FFPlayer.Respawn(player, config);
	}

}
