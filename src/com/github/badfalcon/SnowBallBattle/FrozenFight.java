package com.github.badfalcon.SnowBallBattle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class FrozenFight extends JavaPlugin {

	final static String messagePrefix = "[フローズンファイト] ";

	private FFCommandExecutor snowExecutor;
	private FFTabCompleter snowCompleter;

	static Scoreboard board;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		ScoreboardManager manager = Bukkit.getScoreboardManager();

		board = manager.getNewScoreboard();
		new FFScoreboard(this).setScoreboard();
		snowExecutor = new FFCommandExecutor(this);
		snowCompleter = new FFTabCompleter(this);
		getCommand("sbb").setExecutor(snowExecutor);
		getCommand("lobby").setExecutor(snowExecutor);
		getCommand("sbb").setTabCompleter(snowCompleter);

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new FFListener(this), this);

	}

}
