package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

public class SnowListener implements Listener {

	SnowBallBattle plugin;
	Spectator spec;
	SnowScoreboard snowboard;
	World world;

	public SnowListener(SnowBallBattle plugin) {
		this.plugin = plugin;
		spec = new Spectator(this.plugin);
		snowboard = new SnowScoreboard(plugin);
		world = Bukkit.getServer().getWorlds().get(0);
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		List<String> missing = new ArrayList<String>();
		if (world.hasMetadata("ingame")) {
			world.removeMetadata("ingame", plugin);
		}

		// ロビーがconfigにある場合に、設定する

		if (plugin.getConfig().contains("lobby")) {
			Vector lobby = plugin.getConfig().getVector("lobby");
			float lobbyyaw = plugin.getConfig().getFloatList("lobbyyaw").get(0);
			new SnowLobby(plugin).setLobby(lobby, lobbyyaw);
		} else {
			missing.add("ロビー");
		}
		List<String> teamNames = plugin.getConfig().getStringList(
				"Team.TeamNames");
		for (String team : teamNames) {
			if (plugin.getConfig().contains(team + "respawn")) {
				Vector respawn = plugin.getConfig().getVector(team + "respawn");
				float yaw = plugin.getConfig().getFloatList(team + "yaw")
						.get(0);
				world.setMetadata(team + "resx", new FixedMetadataValue(plugin,
						respawn.getX()));
				world.setMetadata(team + "resy", new FixedMetadataValue(plugin,
						respawn.getY()));
				world.setMetadata(team + "resz", new FixedMetadataValue(plugin,
						respawn.getZ()));
				world.setMetadata(team + "resyaw", new FixedMetadataValue(
						plugin, yaw));
				world.setMetadata(team + "set", new FixedMetadataValue(plugin,
						true));
				continue;
			} else {
				missing.add(team + "のリスポーン地点");
			}
		}
		if (missing.size() != 0) {
			String miss = "";
			for (int i = 0; i < missing.size(); i++) {
				if (i != 0) {
					miss += missing.get(i);
				}
			}
			Bukkit.getLogger().info("[雪合戦]  ゲームを開始するには、" + miss + "を設定してください。");
		} else {
			Bukkit.getLogger().info("[雪合戦]  ゲームを開始する準備は完了しています。");
		}
		snowboard.showScore();
		snowboard.removePlayers();
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setScoreboard(SnowBallBattle.board);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!world.hasMetadata("ingame")) {

			//ゲーム外
			player.removeMetadata("team", plugin);
			player.removeMetadata("teamnumber", plugin);
			player.removeMetadata("teamcolor", plugin);
			player.removeMetadata("spectator", plugin);

			new PlayerJoinTeam(plugin).joinTeam(player);
		} else {

			//ゲーム中

			player.sendMessage("[雪合戦]  現在のゲームが終了するまでお待ちください。");
			player.setScoreboard(SnowBallBattle.board);
			spec.setSpectate(player);
			for (Player player1 : Bukkit.getOnlinePlayers()) {
				if (!spec.isSpectating(player1)) {

					//観戦者を隠す

					player1.hidePlayer(player);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
	}

	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (world.hasMetadata("ingame")) {

			//ゲーム中

			Player player = event.getPlayer();
			if (!spec.isSpectating(player)) {

				//観戦者

				event.setCancelled(true);
				event.getPlayer().sendMessage("ゲーム中はゲームモードの変更はできません。");

			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!world.hasMetadata("ingame")) {

			//ゲーム外

			if (player.isOp()) {

				//キャスト処理

				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

					//右クリック

					if (player.getItemInHand().getType() == Material.WOOD_AXE) {

						//木の斧

						Location loc = event.getClickedBlock().getLocation();
						if (loc != null) {

							//クリック先が空気じゃない場合、位置を記録

							player.setMetadata("locx", new FixedMetadataValue(
									plugin, loc.getBlockX() + 0.5));
							player.setMetadata("locy", new FixedMetadataValue(
									plugin, loc.getBlockY()));
							player.setMetadata("locz", new FixedMetadataValue(
									plugin, loc.getBlockZ() + 0.5));
							player.setMetadata("locyaw",
									new FixedMetadataValue(plugin,
											convertYaw(player.getLocation()
													.getYaw())));
							player.setMetadata("location",
									new FixedMetadataValue(plugin, true));
							player.sendMessage(loc.getBlockX() + 0.5 + ","
									+ loc.getBlockY() + "," + loc.getBlockZ()
									+ 0.5 + "を記録しました。");
						}
					}
				}
			} else {

				//ゲスト処理

				//event.setCancelled(true);
			}
		} else {

			//ゲーム中

			if (event.getAction() == Action.RIGHT_CLICK_AIR
					&& event.getMaterial() == Material.SNOW_BALL) {

				//右クリックかつ雪玉

			} else {

				//以外

				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {

		//天候の変化時

		//event.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {

		//空腹度の変化時

		event.setFoodLevel(18);

	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player
				&& event.getCause() != DamageCause.PROJECTILE) {

			//ダメージ処理をキャンセル

			event.setCancelled(true);
		}
	}

	public float convertYaw(float yaw) {
		if (-135 < yaw && yaw <= -45) {
			return -90;
		} else if (-45 < yaw && yaw <= 45) {
			return 0;
		} else if (45 < yaw && yaw <= 135) {
			return 90;
		} else {
			return 180;
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {

		//	event.getPlayer().sendMessage("playermove");

		//プレイヤー移動時

		//plugin.getLogger().info("Y = " + event.getTo().getY());
		//plugin.getLogger().info("specheight = " + plugin.getConfig().getInt("Spectator.Height"));
		if (event.getTo().getY() < plugin.getConfig().getInt(
				"Spectator.Height")) {

			//観戦位置が低い時

			Player player = event.getPlayer();
			if (spec.isSpectating(player)) {

				//観戦中

				Location loc = player.getLocation();
				loc.setY(plugin.getConfig().getInt(
						"Spectator.Height") + 5);


				player.teleport(loc);
				player.setFlying(true);

				player.sendMessage("[雪合戦]  ゲームに干渉するおそれがるため、これより下にはいけません。");
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {

			//エンティティによるダメージの場合

			// Bukkit.getLogger().info("edbe");
			event.setCancelled(true);
		} else if (event.getDamager().getType() == EntityType.SNOWBALL) {

			//雪玉によるダメージの場合

			Projectile projectile = (Projectile) event.getDamager();
			Player shooter = (Player) projectile.getShooter();
			Player hitPlayer = (Player) event.getEntity();
			if (!spec.isSpectating(hitPlayer)
					&& hitPlayer.getNoDamageTicks() == 0) {

				//試合中かつ無敵時間じゃない場合

				String hitPlayerTeam = hitPlayer.getMetadata("team").get(0)
						.asString();
				String shooterTeam = shooter.getMetadata("team").get(0)
						.asString();
				if (!shooter.getName().equals(hitPlayer.getName())
						&& !shooterTeam.equals(hitPlayerTeam)) {

					//チーム、プレイヤーが異なる場合

					Score person = SnowBallBattle.board.getObjective("Pscore")
							.getScore(shooter);
					String shooterTeamColor = shooter.getMetadata("teamcolor")
							.get(0).asString();
					String shooterTeamName = shooter.getMetadata("team").get(0)
							.asString();
					OfflinePlayer shooterTeamPlayerName = Bukkit
							.getOfflinePlayer(ChatColor
									.translateAlternateColorCodes('&',
											shooterTeamColor + shooterTeamName));
					Score team = SnowBallBattle.board.getObjective("Tscore")
							.getScore(shooterTeamPlayerName);
					person.setScore(person.getScore() + 1);
					team.setScore(team.getScore() + 1);
					shooter.playSound(shooter.getLocation(),
							Sound.SUCCESSFUL_HIT, 1, 1);
					shooter.giveExpLevels(1);
					double spawnx = world.getMetadata(hitPlayerTeam + "resx")
							.get(0).asDouble();
					double spawny = world.getMetadata(hitPlayerTeam + "resy")
							.get(0).asDouble();
					double spawnz = world.getMetadata(hitPlayerTeam + "resz")
							.get(0).asDouble();
					float spawnyaw = world
							.getMetadata(hitPlayerTeam + "resyaw").get(0)
							.asFloat();
					Location respawn = new Location(world, spawnx, spawny + 1,
							spawnz, spawnyaw, 0);
					hitPlayer.teleport(respawn);
					hitPlayer.setNoDamageTicks(16);
					ItemStack[] sb = new ItemStack[36];
					for (int i = 0; i < plugin.getConfig().getInt(
							"Game.SnowBallStacks"); i++) {
						sb[i] = new ItemStack(Material.SNOW_BALL, 32);
					}
					hitPlayer.getInventory().clear();
					hitPlayer.getInventory().setContents(sb);
				}
			} else {
				// Bukkit.getLogger().info("edbe2");
				event.setCancelled(true);
			}
		}
	}
}