package com.github.badfalcon.SnowBallBattle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class SnowBallBattle extends JavaPlugin {

	private SnowCommandExecutor snowExecutor;
	private SnowTabCompleter snowCompleter;

	static Scoreboard board;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		new SnowScoreboard(this).setScoreboard();
		snowExecutor = new SnowCommandExecutor(this);
		snowCompleter = new SnowTabCompleter(this);
		getCommand("sbb").setExecutor(snowExecutor);
		getCommand("lobby").setExecutor(snowExecutor);
		getCommand("sbb").setTabCompleter(snowCompleter);

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new SnowListener(this), this);

	}
}
