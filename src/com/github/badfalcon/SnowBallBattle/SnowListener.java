package com.github.badfalcon.SnowBallBattle;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.metadata.FixedMetadataValue;
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
		for (Player player : Bukkit.getOnlinePlayers()) {
			PlayerJoinTeam(player);
			player.removeMetadata("loc1", plugin);
			player.removeMetadata("loc2", plugin);
			player.removeMetadata("locset", plugin);
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
				String[] teamcolors = plugin.getConfig()
						.getStringList("Team.TeamColors")
						.toArray(new String[0]);
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
						player.setMetadata("team", new FixedMetadataValue(
								plugin, teams[teamnumber]));
						player.setMetadata("teamnumber",
								new FixedMetadataValue(plugin, teamnumber));
						player.setMetadata("teamcolor", new FixedMetadataValue(
								plugin, teamcolors[teamnumber]));
						player.setMetadata("spectator", new FixedMetadataValue(
								plugin, false));
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
		} else {
			player.setMetadata("spectator",
					new FixedMetadataValue(plugin, true));
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
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.SNOWBALL) {
			Player shooter = (Player) event.getEntity().getShooter();
			event.getEntity().setMetadata("Shooter",
					new FixedMetadataValue(plugin, shooter.getName()));
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& player.getItemInHand().getType() == Material.WOOD_AXE) {
			Location loc = event.getClickedBlock().getLocation();
			if (loc != null){
				
			}
			if (player.hasMetadata("locset")) {
		
				if (player.getMetadata("locset").get(0).asInt() == 1) {
					player.setMetadata("loc2", new FixedMetadataValue(plugin,
							loc));
					player.setMetadata("locset", new FixedMetadataValue(plugin,
							2));
					Bukkit.getServer().broadcastMessage(
							loc.getX() + "," + loc.getX() + "," + loc.getX()
									+ "をスロット２に記録しました。");
				} else {
					player.setMetadata("loc1", new FixedMetadataValue(plugin,
							loc));
					player.setMetadata("locset", new FixedMetadataValue(plugin,
							1));
					Bukkit.getServer().broadcastMessage(
							loc.getX() + "," + loc.getX() + "," + loc.getX()
									+ "をスロット１に記録しました。");
				}
			} else {
				player.setMetadata("loc1", new FixedMetadataValue(plugin, loc));
				player.setMetadata("locset", new FixedMetadataValue(plugin, 1));
				Bukkit.getServer().broadcastMessage(
						loc.getX() + "," + loc.getX() + "," + loc.getX()
								+ "をスロット１に記録しました。");
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() == EntityType.SNOWBALL) {
			Projectile projectile = (Projectile) event.getDamager();
			Player shooter = (Player) projectile.getShooter();
			Player hitPlayer = (Player) event.getEntity();
			if (!shooter.getName().equals(hitPlayer.getName())
					&& !shooter
							.getMetadata("team")
							.get(0)
							.asString()
							.equals(hitPlayer.getMetadata("team").get(0)
									.asString())) {
				Score person = board.getObjective("personalscore").getScore(
						shooter);
				Score team = board.getObjective("score").getScore(
						Bukkit.getOfflinePlayer(ChatColor
								.translateAlternateColorCodes('&', shooter
										.getMetadata("teamcolor").get(0)
										.asString()
										+ shooter.getMetadata("team").get(0)
												.asString())));
				person.setScore(person.getScore() + 1);
				team.setScore(team.getScore() + 1);
				hitPlayer
						.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
			}
		}
		event.setCancelled(true);
	}

}