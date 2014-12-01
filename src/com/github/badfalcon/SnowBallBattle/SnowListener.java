package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerDropItemEvent;
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
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class SnowListener implements Listener {

	SnowBallBattle plugin;
	Spectator spec;
	SnowScoreboard snowboard;
	World world;

	public SnowListener(SnowBallBattle plugin) {
		this.plugin = plugin;
		spec = new Spectator(this.plugin);
		world = Bukkit.getServer().getWorlds().get(0);
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {

		List<String> missing = new ArrayList<String>();
		if (world.hasMetadata("ingame")) {
			world.removeMetadata("ingame", plugin);
		}

		int teams = plugin.getConfig().getStringList("Team.Names").size();
		if (teams < 2) {
			missing.add((2 - teams) + "つ以上のチームを作成し");
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
				"Team.Names");
		for (String teamName : teamNames) {
			if (plugin.getConfig().contains(teamName + ".Respawn")) {
				Vector respawn = plugin.getConfig().getVector(teamName + ".Respawn");
				float yaw = plugin.getConfig().getFloatList(teamName + ".RespawnYaw")
						.get(0);
				world.setMetadata(teamName + "Resx", new FixedMetadataValue(plugin,
						respawn.getX()));
				world.setMetadata(teamName + "Resy", new FixedMetadataValue(plugin,
						respawn.getY()));
				world.setMetadata(teamName + "Resz", new FixedMetadataValue(plugin,
						respawn.getZ()));
				world.setMetadata(teamName + "Resyaw", new FixedMetadataValue(
						plugin, yaw));
				world.setMetadata(teamName + "Set", new FixedMetadataValue(plugin,
						true));
				continue;
			} else {
				missing.add(teamName + "のリスポーン地点");
			}
		}
		if (missing.size() != 0) {
			String miss = "";
			for (int i = 0; i < missing.size(); i++) {
				miss += missing.get(i);
				if (i != missing.size() - 1) {
					miss += "、";
				}
			}
			Bukkit.getLogger().info(SnowBallBattle.messagePrefix + "ゲームを開始するには、" + miss + "を設定してください。");
		} else {
			Bukkit.getLogger().info(SnowBallBattle.messagePrefix + "ゲームを開始する準備は完了しています。");
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setScoreboard(SnowBallBattle.board);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {

			//ゲーム中

			if (player.hasMetadata("team")) {

				//チームに所属している時

				if (world.hasMetadata("gameStart")) {

					if (player.getLastPlayed() == 0) {

						//初参加の時

						player.sendMessage(SnowBallBattle.messagePrefix + "現在のゲームが終了するまでお待ちください。");
						player.setScoreboard(SnowBallBattle.board);
						spec.setSpectate(player);
						for (Player player1 : Bukkit.getOnlinePlayers()) {
							if (!spec.isSpectating(player1)) {

								//観戦者を隠す

								player1.hidePlayer(player);
							}
						}

					}

					if (player.getLastPlayed() < world.getMetadata("gameStart").get(0).asLong()) {

						//この試合に参加していない場合

						player.removeMetadata("team", plugin);
						player.removeMetadata("teamnumber", plugin);
						player.removeMetadata("teamcolor", plugin);
						player.removeMetadata("spectator", plugin);

						player.sendMessage(SnowBallBattle.messagePrefix + "現在のゲームが終了するまでお待ちください。");
						player.setScoreboard(SnowBallBattle.board);
						spec.setSpectate(player);
						for (Player player1 : Bukkit.getOnlinePlayers()) {
							if (!spec.isSpectating(player1)) {

								//観戦者を隠す

								player1.hidePlayer(player);
							}
						}

					} else {

						//この試合に参加している時

						//何もしない

					}

				}

			} else {

				//チームに所属していない時

				player.sendMessage(SnowBallBattle.messagePrefix + "現在のゲームが終了するまでお待ちください。");
				player.setScoreboard(SnowBallBattle.board);
				spec.setSpectate(player);
				for (Player player1 : Bukkit.getOnlinePlayers()) {
					if (!spec.isSpectating(player1)) {

						//観戦者を隠す

						player1.hidePlayer(player);
					}
				}
			}

		} else {

			//ゲーム外
			player.removeMetadata("team", plugin);
			player.removeMetadata("teamnumber", plugin);
			player.removeMetadata("teamcolor", plugin);
			player.removeMetadata("spectator", plugin);

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
				event.getPlayer().sendMessage(SnowBallBattle.messagePrefix + "ゲーム中はゲームモードの変更はできません。");

			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!world.hasMetadata("ingame")) {

			if (world.hasMetadata("ready")) {

				event.setCancelled(true);

			} else {

				//ゲーム外

				if (player.isOp()) {

					//キャスト処理

					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

						//右クリック

						if (player.getItemInHand().getType() == Material.getMaterial(plugin.getConfig().getString(
								"LocationItem"))) {

							//コンフィグに記載されてるアイテムを持っている場合

							Location loc = event.getClickedBlock().getLocation();
							if (loc != null) {

								//クリック先が空気じゃない場合、位置を記録

								player.setMetadata("Locx", new FixedMetadataValue(
										plugin, loc.getBlockX() + 0.5));
								player.setMetadata("Locy", new FixedMetadataValue(
										plugin, loc.getBlockY()));
								player.setMetadata("Locz", new FixedMetadataValue(
										plugin, loc.getBlockZ() + 0.5));
								player.setMetadata("Locyaw",
										new FixedMetadataValue(plugin,
												convertYaw(player.getLocation()
														.getYaw())));
								player.setMetadata("Location",
										new FixedMetadataValue(plugin, true));
								player.sendMessage(SnowBallBattle.messagePrefix + loc.getBlockX() + 0.5 + ","
										+ loc.getBlockY() + "," + loc.getBlockZ()
										+ 0.5 + "を記録しました。");
							}
						}
					}
				} else {

					//ゲスト処理

					event.setCancelled(true);
				}
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
	public void onPlayerDropItem(PlayerDropItemEvent event) {

		//プレイヤーがアイテムを落とした時

		if (world.hasMetadata("ingame")) {

			//ゲーム中の時

			event.setCancelled(true);
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

		//プレイヤー移動時

		Player player = event.getPlayer();

		if (world.hasMetadata("ready")) {

			//準備中

			if (!spec.isSpectating(player)) {

				//観戦者ではない時

				String playerTeam = player.getMetadata("TeamName").get(0).asString();
				double spawnx = world.getMetadata(playerTeam + "Resx")
						.get(0).asDouble();
				double spawny = world.getMetadata(playerTeam + "Resy")
						.get(0).asDouble();
				double spawnz = world.getMetadata(playerTeam + "Resz")
						.get(0).asDouble();
				float spawnyaw = world
						.getMetadata(playerTeam + "Resyaw").get(0)
						.asFloat();
				Location respawn = new Location(world, spawnx, spawny + 1,
						spawnz, spawnyaw, 0);

				if (event.getTo().getY() != respawn.getY()) {

					//リスポーン地点のYじゃない時

					player.teleport(respawn);
				}
			}
		} else {

			//ゲーム中

			if (spec.isSpectating(player)) {

				//観戦中

				if (event.getTo().getY() < plugin.getConfig().getInt(
						"Spectator.Height")) {

					//観戦位置が低い時

					Location loc = player.getLocation();
					loc.setY(plugin.getConfig().getInt(
							"Spectator.Height") + 5);

					//上へ５マステレポート

					player.teleport(loc);
					player.setFlying(true);

					player.sendMessage(SnowBallBattle.messagePrefix + " ゲームに干渉するおそれがるため、これより下にはいけません。");

				}
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

			if (world.hasMetadata("ingame")) {

				//ゲーム中の場合

				Projectile projectile = (Projectile) event.getDamager();
				Player shooter = (Player) projectile.getShooter();
				Player hitPlayer = (Player) event.getEntity();

				if (!spec.isSpectating(hitPlayer)
						&& hitPlayer.getNoDamageTicks() == 0) {

					//試合中かつ無敵時間じゃない場合

					String hitPlayerTeamName = hitPlayer.getMetadata("TeamName").get(0).asString();
					String shooterTeamName = shooter.getMetadata("TeamName").get(0).asString();
					if (!shooter.getName().equals(hitPlayer.getName()) && !shooterTeamName.equals(hitPlayerTeamName)) {

						//チーム、プレイヤーが異なる場合

						//

						event.setCancelled(true);

						//無敵にして、リスポーン

						hitPlayer.setNoDamageTicks(plugin.getConfig().getInt("Game.InvincibleTime"));
						double spawnx = world.getMetadata(hitPlayerTeamName + "Resx").get(0).asDouble();
						double spawny = world.getMetadata(hitPlayerTeamName + "Resy").get(0).asDouble();
						double spawnz = world.getMetadata(hitPlayerTeamName + "Resz").get(0).asDouble();
						float spawnyaw = world.getMetadata(hitPlayerTeamName + "Resyaw").get(0).asFloat();
						Location respawn = new Location(world, spawnx, spawny + 1, spawnz, spawnyaw, 0);
						hitPlayer.teleport(respawn);

						//アイテムリセット

						ItemStack[] sb = new ItemStack[36];
						for (int i = 0; i < plugin.getConfig().getInt("Game.SnowBallStacks"); i++) {
							sb[i] = new ItemStack(Material.SNOW_BALL, 16);
						}
						hitPlayer.getInventory().clear();
						hitPlayer.getInventory().setContents(sb);

						//経験値リセット
						hitPlayer.setExp(1.0f);

						//スコア追加

						Team shooterTeam = SnowBallBattle.board.getTeam(shooterTeamName);
						Score personalScore = SnowBallBattle.board.getObjective("Pscore").getScore(shooter);
						Score teamScore = SnowBallBattle.board.getObjective("Tscore").getScore(
								Bukkit.getOfflinePlayer(shooterTeam.getPrefix() + shooterTeamName
										+ shooterTeam.getSuffix()));

						personalScore.setScore(personalScore.getScore() + 1);
						teamScore.setScore(teamScore.getScore() + 1);
						shooter.playSound(shooter.getLocation(),
								Sound.SUCCESSFUL_HIT, 1, 1);
						shooter.giveExpLevels(1);
						for (Player player : Bukkit.getOnlinePlayers()) {
							player.sendMessage(SnowBallBattle.messagePrefix + "" + shooterTeam.getPrefix()
									+ shooterTeam.getName() + " + 1pt! "
									+ ChatColor.RESET + " (" + shooter.getName() + " → " + hitPlayer.getName() + ")");
						}
					} else {
						event.setCancelled(true);
					}
				} else {
					// Bukkit.getLogger().info("edbe2");
					event.setCancelled(true);
				}

			} else {
				event.setCancelled(true);
			}

		}
	}
}