package com.github.badfalcon.SnowBallBattle;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowBallBattle extends JavaPlugin {

	private SnowCommandExecutor snowExecutor;

	@Override
	public void onEnable() {
		snowExecutor = new SnowCommandExecutor(this);
		getCommand("sbb").setExecutor(snowExecutor);

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new SnowListener(this), this);

		this.saveDefaultConfig();
	}
}
