package com.github.badfalcon.SnowBallBattle;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class SnowListener implements Listener {

	private final SnowBallBattle plugin;

	public SnowListener(SnowBallBattle plugin) {
		this.plugin = plugin;
	}

	ScoreboardManager manager = Bukkit.getScoreboardManager();
	Scoreboard board = manager.getNewScoreboard();
	Team[] teams = new Team[0];

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		board = manager.getNewScoreboard();
		String[] steams = (String[]) plugin.getConfig()
				.getStringList("Team.TeamNames").toArray(new String[0]);
		String[] teamcolors = (String[]) plugin.getConfig()
				.getStringList("Team.TeamColors").toArray(new String[0]);
		teams = new Team[steams.length];
		Score[] score = new Score[steams.length];
		Objective objective = board.registerNewObjective("score", "dummy");
		Objective personal = board.registerNewObjective("personalscore",
				"dummy");
		personal.setDisplaySlot(DisplaySlot.BELOW_NAME);
		personal.setDisplayName("");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (int i = 0; i < steams.length; i++) {
			teams[i] = board.registerNewTeam(steams[i]);
			teamcolors[i] = ChatColor.translateAlternateColorCodes('&',
					teamcolors[i]);
			score[i] = objective.getScore(Bukkit.getOfflinePlayer(teamcolors[i]
					+ steams[i]));
			score[i].setScore(0);
		}
		for(Player player:Bukkit.getOnlinePlayers()){
			PlayerJoinTeam(player);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		PlayerJoinTeam(event.getPlayer());
	}
	
	public void PlayerJoinTeam(Player player) {
		List<String> spectatorstringlist = plugin.getConfig().getStringList(
				"Game.Spectators");
		player.setScoreboard(board);
		if (!spectatorstringlist.contains(player.getName())) {
			while (true) {
				String[] teams = plugin.getConfig()
						.getStringList("Team.TeamNames").toArray(new String[0]);
				int fullteams = 0;
				for (String team : teams) {
					if (board.getTeam(team).getSize() == plugin.getConfig()
							.getInt("Team.MaxPlayers")) {
						fullteams++;
					}
				}
				if (fullteams < teams.length) {
					int teamnumber = (int) (Math.random() * teams.length);
					Team jointeam = board.getTeam(teams[teamnumber]);
					if (jointeam.getSize() < plugin.getConfig().getInt(
							"Team.MaxPlayers")) {
						jointeam.addPlayer(player);
						String[] teamcolors = plugin.getConfig()
								.getStringList("Team.TeamColors")
								.toArray(new String[0]);
						player.sendMessage(ChatColor
								.translateAlternateColorCodes('&', "&Rあなたはチーム："
										+ teamcolors[teamnumber]
										+ jointeam.getName().toString()
										+ "&Rへ参加しました。"));
						Score person = board.getObjective("personalscore")
								.getScore(player);
						person.setScore(0);
						break;
					}
				} else {
					plugin.getLogger().info("人数が上限を超えました。");
					break;
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player breaker = event.getPlayer();
		if (!breaker.isOp()) {
			event.setCancelled(true);
			breaker.sendMessage("壊さないでね。");
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player placer = event.getPlayer();
		if (!placer.isOp()) {
			event.setCancelled(true);
			placer.sendMessage("置けません。");
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		if (projectile instanceof Snowball) {
			List<Entity> hitnearentity = projectile.getNearbyEntities(0.5, 1.0,
					0.5);
			if (hitnearentity.size() != 0) {
				Player shooter = (Player) projectile.getShooter();
				String[] teams = plugin.getConfig()
						.getStringList("Team.TeamNames").toArray(new String[0]);
				int shooterteamnum = 0;
				for (int i = 0; i < teams.length; i++) {
					if (teams[i].equals(board.getPlayerTeam(shooter).getName())) {
						shooterteamnum = i;
					}
				}
				Player hitPlayer = (Player) hitnearentity.get(0);
				if (!board.getPlayerTeam(shooter).getName()
						.equals(board.getPlayerTeam(hitPlayer).getName())) {
					Score person = board.getObjective("personalscore")
							.getScore(shooter);
					Score team = board
							.getObjective("score")
							.getScore(
									Bukkit.getOfflinePlayer(ChatColor
											.translateAlternateColorCodes(
													'&',
													plugin.getConfig()
															.getStringList(
																	"Team.TeamColors")
															.toArray(
																	new String[0])[shooterteamnum])
											+ board.getPlayerTeam(shooter)
													.getName()));
					person.setScore(person.getScore() + 1);
					team.setScore(team.getScore() + 1);
				}
				// hitnearentity.get(0).playEffect(EntityEffect.HURT);
			}
		}
	}
}