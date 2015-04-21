package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
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

		FileConfiguration config = plugin.getConfig();

		checkConfig(config);

		// ゲーム設定の確認

		List<String> missing = new ArrayList<String>();

		// ゲーム中ならば、ゲーム停止
		if (world.hasMetadata("ingame")) {
			new SnowRunnableFinish(this.plugin).run();
		}

		// チーム数の確認
		int teams = config.getStringList("Team.Names").size();
		if (teams < 2) {
			missing.add((2 - teams) + "つ以上のチームを作成し");
		}

		// モードの確認



		// ロビーがconfigに存在する場合に、設定する
		if (config.contains("lobby")) {
			Vector lobby = config.getVector("lobby");
			float lobbyyaw = config.getFloatList("lobbyyaw").get(0);
			new SnowLobby(plugin).setLobby(lobby, lobbyyaw);
		} else {
			missing.add("ロビー");
		}

		List<String> teamNames = config.getStringList("Team.Names");
//		int n = 0;
		for (String teamName : teamNames) {
			if(config.getString("Mode").equals("premade")){
				if (!config.contains(teamName + ".Members")) {
					config.set(teamName + ".Members", Arrays.asList());
				}
	//			n++;
			}

			if (config.contains(teamName + ".Respawn")) {
				Vector respawn = config.getVector(teamName + ".Respawn");
				float yaw = config.getFloatList(teamName + ".RespawnYaw")
						.get(0);
				world.setMetadata(teamName + "Resx", new FixedMetadataValue(
						plugin, respawn.getX()));
				world.setMetadata(teamName + "Resy", new FixedMetadataValue(
						plugin, respawn.getY()));
				world.setMetadata(teamName + "Resz", new FixedMetadataValue(
						plugin, respawn.getZ()));
				world.setMetadata(teamName + "Resyaw", new FixedMetadataValue(
						plugin, yaw));
				world.setMetadata(teamName + "Set", new FixedMetadataValue(
						plugin, true));
				continue;
			} else {
				missing.add(teamName + "のリスポーン地点");
			}
		}
		List<String> itemNames = config.getStringList("Item.Names");
		for (String itemName : itemNames) {
			if (config.getBoolean("Item." + itemName + ".activate")) {
				if (config.getInt("Item." + itemName + ".Numbers") < 1) {
					missing.add(itemName + "のスポーン地点");
				}
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
			Bukkit.getLogger().info(
					SnowBallBattle.messagePrefix + "ゲームを開始するには、" + miss
							+ "を設定してください。");
		} else {
			Bukkit.getLogger().info(
					SnowBallBattle.messagePrefix + "ゲームを開始する準備は完了しています。");
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setScoreboard(SnowBallBattle.board);
		}
	}

	private void checkConfig(FileConfiguration config) {
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {

			// ゲーム中

			if (player.hasMetadata("team")) {

				// チームに所属している時

				if (world.hasMetadata("gameStart")) {

					if (player.getLastPlayed() == 0) {

						// 初参加の時

						player.sendMessage(SnowBallBattle.messagePrefix
								+ "現在のゲームが終了するまでお待ちください。");
						player.setScoreboard(SnowBallBattle.board);
						spec.setSpectate(player);
						for (Player player1 : Bukkit.getOnlinePlayers()) {
							if (!Spectator.isSpectating(player1)) {

								// 観戦者を隠す
								player1.hidePlayer(player);
							}
						}

					}

					if (player.getLastPlayed() < world.getMetadata("gameStart")
							.get(0).asLong()) {

						// この試合に参加していない場合

						player.removeMetadata("team", plugin);
						player.removeMetadata("teamnumber", plugin);
						player.removeMetadata("teamcolor", plugin);
						player.removeMetadata("spectator", plugin);

						player.sendMessage(SnowBallBattle.messagePrefix
								+ "現在のゲームが終了するまでお待ちください。");
						player.setScoreboard(SnowBallBattle.board);
						spec.setSpectate(player);
						for (Player player1 : Bukkit.getOnlinePlayers()) {
							if (!Spectator.isSpectating(player1)) {

								// 観戦者を隠す
								player1.hidePlayer(player);
							}
						}

					} else {

						// この試合に参加している時
						// 何もしない

					}

				}

			} else {

				// チームに所属していない時

				player.sendMessage(SnowBallBattle.messagePrefix
						+ "現在のゲームが終了するまでお待ちください。");
				player.setScoreboard(SnowBallBattle.board);
				spec.setSpectate(player);
				for (Player player1 : Bukkit.getOnlinePlayers()) {
					if (!Spectator.isSpectating(player1)) {

						// 観戦者を隠す

						player1.hidePlayer(player);
					}
				}
			}

		} else {

			if(Spectator.isSpectating(player)){
				new Spectator(plugin).removeSpectate(player);
			}

			// ゲーム外
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
	void onCreatureSpawn(CreatureSpawnEvent e) {
		if (!e.getSpawnReason().equals(SpawnReason.CUSTOM)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onEntityCombust(EntityCombustEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (world.hasMetadata("ingame")) {

			// ゲーム中

			Player player = event.getPlayer();
			if (!Spectator.isSpectating(player)) {

				// 観戦者

				event.setCancelled(true);
				event.getPlayer().sendMessage(
						SnowBallBattle.messagePrefix + "ゲーム中はゲームモードの変更はできません。");

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

				// ゲーム外

				if (player.isOp()) {

					// キャスト処理

					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

						// 右クリック

						if (player.getItemInHand().getType() == Material
								.getMaterial(plugin.getConfig().getString(
										"LocationItem"))) {

							// コンフィグに記載されてるアイテムを持っている場合

							Location loc = event.getClickedBlock()
									.getLocation();
							if (loc != null) {

								// クリック先が空気じゃない場合、位置を記録

								player.setMetadata(
										"Locx",
										new FixedMetadataValue(plugin, loc
												.getBlockX() + 0.5));
								player.setMetadata(
										"Locy",
										new FixedMetadataValue(plugin, loc
												.getBlockY()));
								player.setMetadata(
										"Locz",
										new FixedMetadataValue(plugin, loc
												.getBlockZ() + 0.5));
								player.setMetadata("Locyaw",
										new FixedMetadataValue(plugin,
												convertYaw(player.getLocation()
														.getYaw())));
								player.setMetadata("Location",
										new FixedMetadataValue(plugin, true));
								player.sendMessage(SnowBallBattle.messagePrefix
										+ loc.getBlockX() + 0.5 + " , "
										+ loc.getBlockY() + " , "
										+ loc.getBlockZ() + 0.5 + " を記録しました。");
							}
						}
					}
				} else {

					// ゲスト処理

					event.setCancelled(true);
				}
			}
		} else {

			// ゲーム中

			if ((event.getAction() == Action.RIGHT_CLICK_AIR || event
					.getAction() == Action.RIGHT_CLICK_BLOCK)
					&& event.getMaterial() == Material.SNOW_BALL) {

				// 右クリックかつ雪玉

				if (player.hasMetadata("TripleShot")) {
					TripleShot(player);
				}

			} else {

				// 以外

				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent e) {

		if (world.hasMetadata("ingame")) {
			// ingame判定

			if (e.getCurrentItem() != null) {
				ItemStack is = e.getCurrentItem();
				if (!(is.getType() == Material.SNOW_BALL || is.getType() == Material.AIR)) {
					e.setCancelled(true);
				}
			}
		}

	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (world.hasMetadata("ingame")) {

			// ingame判定

			Item item = event.getItem();
			final Player p = event.getPlayer();

			// tripleshot

			if (item.getItemStack().getItemMeta().getDisplayName()
					.equals("TripleShot")) {
				item.remove();
				event.setCancelled(true);
				p.setMetadata("TripleShot",
						new FixedMetadataValue(plugin, true));

				plugin.getServer().broadcastMessage(
						SnowBallBattle.messagePrefix + p.getName()
								+ "の雪球が3ウェイになった!!");

				// 削除タスク
				int dur = plugin.getConfig().getInt("Item.TripleShot.Duration");
				new DeleteBuff("TripleShot", p).runTaskLater(plugin, 20 * dur);

			}

			// invisible

			else if (item.getItemStack().getItemMeta().getDisplayName()
					.equals("Invisible")) {
				item.remove();
				event.setCancelled(true);
				ItemStack[] armor = p.getInventory().getArmorContents();
				p.getInventory().setArmorContents(new ItemStack[4]);
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.INVISIBILITY, 200, 1));
				p.setMetadata("Invisible", new FixedMetadataValue(plugin, true));

				plugin.getServer().broadcastMessage(
						SnowBallBattle.messagePrefix + p.getName()
								+ "が透明になった!!");

				// 削除タスク
				int dur = plugin.getConfig().getInt("Item.Invisible.Duration");
				new DeleteBuff("Invisible", p).runTaskLater(plugin, 20 * dur);
				new SetArmor(p, armor).runTaskLater(plugin, 20 * dur);
			}

			// speedup

			else if (item.getItemStack().getItemMeta().getDisplayName()
					.equals("SpeedUp")) {
				item.remove();
				event.setCancelled(true);
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200,
						1));
				p.setMetadata("SpeedUp", new FixedMetadataValue(plugin, true));

				plugin.getServer().broadcastMessage(
						SnowBallBattle.messagePrefix + p.getName()
								+ "の足が速くなった!!");

				// 削除タスク
				int dur = plugin.getConfig().getInt("Item.SpeedUp.Duration");
				new DeleteBuff("SpeedUp", p).runTaskLater(plugin, 20 * dur);
			}

			// spawncannon

			else if (item.getItemStack().getItemMeta().getDisplayName()
					.equals("SpawnCannon")) {
				item.remove();
				event.setCancelled(true);
				int dur = plugin.getConfig().getInt("Item.Invisible.Duration");
				new SpawnCannon(p, dur / 2).runTaskTimer(plugin, 0, 40);

				plugin.getServer().broadcastMessage(
						SnowBallBattle.messagePrefix + p.getName()
								+ "が雪だるまを召喚した!!");

			}
		}
	}

	Player getNearestPlayer(Location location) {
		Player nearest = null;
		double d = Double.MAX_VALUE;
		double d2;
		for (Player p : Bukkit.getOnlinePlayers()) {
			d2 = location.distance(p.getLocation());
			if (d2 < d) {
				d = d2;
				nearest = p;
			}
		}
		return nearest;
	}

	@EventHandler
	public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
		event.setCancelled(true);
	}

	public class SpawnCannon extends BukkitRunnable {
		int health;
		Location loc;
		Skeleton cannon;
		Player shooter;

		public SpawnCannon(Player p, int health) {
			loc = p.getLocation();
			shooter = p;
			cannon = (Skeleton) loc.getWorld().spawnEntity(loc,
					EntityType.SKELETON);
			cannon.setMetadata("owner",
					new FixedMetadataValue(plugin, shooter.getName()));
			cannon.setRemoveWhenFarAway(false);
			cannon.setCustomNameVisible(true);
			cannon.setCustomName("固定砲台");
			cannon.setNoDamageTicks(Integer.MAX_VALUE);
			cannon.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
					Integer.MAX_VALUE, 100), true);
			this.health = health;
		}

		@Override
		public void run() {
			List<Entity> entities = cannon.getNearbyEntities(16, 16, 16);
			String ownerTeamName = shooter.getMetadata("TeamName").get(0)
					.asString();

			Player nearestEntity = null;
			double nearestDistance = Double.MAX_VALUE;

			for (Entity entity2 : entities) {
				if (entity2.getType().equals(EntityType.PLAYER)) {
					Player p = (Player) entity2;
					String targetTeamName = p.getMetadata("TeamName").get(0)
							.asString();
					double d = cannon.getLocation().distance(
							entity2.getLocation());
					if (d < nearestDistance
							&& (!ownerTeamName.equals(targetTeamName))) {
						nearestEntity = p;
						nearestDistance = d;
					}
				}
			}

			if (nearestEntity != null) {
				Bukkit.getLogger().info("target is " + nearestEntity.getName());
				cannon.setTarget(nearestEntity);
				Location loca = cannon.getEyeLocation();
				Location locb = nearestEntity.getEyeLocation();

				double dX = loca.getX() - locb.getX();
				double dY = loca.getY() - locb.getY();
				double dZ = loca.getZ() - locb.getZ();

				float yaw = (float) Math.atan2(dZ, dX);

				float pitch = (float) (Math.atan2(Math.sqrt(dZ * dZ + dX * dX),
						dY) + Math.PI);

				cannon.teleport(new Location(world, loca.getX(), loca.getY(),
						loca.getZ(), yaw, pitch));

				Bukkit.getLogger().info(
						loca.toVector().toString() + ",,"
								+ locb.toVector().toString());
				Vector vector = locb.toVector().subtract(loca.toVector());
				Arrow arrow = world.spawnArrow(loca, vector, (float) 1,
						(float) 0);
				Location location = arrow.getLocation();
				Vector velocity = arrow.getVelocity();
				arrow.remove();
				Snowball ball = (Snowball) world.spawnEntity(location,
						EntityType.SNOWBALL);
				ball.setShooter(cannon);
				ball.setVelocity(velocity);
			}

			health--;
			if (health <= 0) {
				cannon.remove();
				cancel();
			}
		}
	}

	public class SetArmor extends BukkitRunnable {
		Player player;
		ItemStack[] armor;

		public SetArmor(Player p, ItemStack[] a) {
			player = p;
			armor = a;
		}

		@Override
		public void run() {
			player.getInventory().setArmorContents(armor);
		}
	}

	public class DeleteBuff extends BukkitRunnable {

		Player player;
		String name;

		public DeleteBuff(String name, Player p) {
			this.name = name;
			player = p;
		}

		@Override
		public void run() {
			if (player.isOnline()) {
				player.removeMetadata(name, plugin);
			}
		}

	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {

		// プレイヤーがアイテムを落とした時

		if (world.hasMetadata("ingame")) {

			// ゲーム中の時

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {

		// 天候の変化時

		event.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {

		// 空腹度の変化時

		event.setFoodLevel(18);

	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player
				&& event.getCause() != DamageCause.PROJECTILE) {

			// ダメージ処理をキャンセル

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

		// プレイヤー移動時

		Player player = event.getPlayer();

		if (world.hasMetadata("ready")) {

			// 準備中

			if (!Spectator.isSpectating(player)) {

				// 観戦者ではない時

				String playerTeam = player.getMetadata("TeamName").get(0)
						.asString();
				double spawnx = world.getMetadata(playerTeam + "Resx").get(0)
						.asDouble();
				double spawny = world.getMetadata(playerTeam + "Resy").get(0)
						.asDouble();
				double spawnz = world.getMetadata(playerTeam + "Resz").get(0)
						.asDouble();
				float spawnyaw = world.getMetadata(playerTeam + "Resyaw")
						.get(0).asFloat();
				Location respawn = new Location(world, spawnx, spawny + 1,
						spawnz, spawnyaw, 0);

				if (event.getTo().getY() != respawn.getY()) {

					// リスポーン地点のYじゃない時

					player.teleport(respawn);
				}
			}
		} else {

			// ゲーム中

			if (Spectator.isSpectating(player)) {

				// 観戦中

				if (event.getTo().getY() < plugin.getConfig().getInt(
						"Spectator.Height")) {

					// 観戦位置が低い時

					Location loc = player.getLocation();
					loc.setY(plugin.getConfig().getInt("Spectator.Height") + 5);

					// 上へ５マステレポート

					player.teleport(loc);
					player.setFlying(true);

					player.sendMessage(SnowBallBattle.messagePrefix
							+ " ゲームに干渉するおそれがるため、これより下にはいけません。");

				}
			}
		}
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		Projectile proj = event.getEntity();
		Vector velocity = proj.getVelocity();
		proj.setVelocity(velocity);
		// LivingEntity e = proj.getShooter();
		// Player p = (Player) e;

	}

	public void SpawnSnowball(Player p, double angle, double speed) {

		Vector v = p.getEyeLocation().getDirection();
		Location l = p.getEyeLocation();

		v = rotate(v, angle);
		Snowball ball = l.getWorld().spawn(l, Snowball.class);
		// ball.setMetadata("normal", new FixedMetadataValue(plugin, true));
		ball.setShooter(p);
		ball.setVelocity(v.multiply(speed));
	}

	public void TripleShot(Player p) {
		SpawnSnowball(p, Math.PI / 12, 1.5);

		// SpawnSnowball(p, Math.PI / 6,1.5);

		SpawnSnowball(p, -Math.PI / 12, 1.5);

		// SpawnSnowball(p, -Math.PI / 6,1.5);

	}

	public Vector rotate(Vector v, double rotate) {
		double x = v.getX();
		double y = v.getY();
		double z = v.getZ();

		double sin = Math.sin(rotate);
		double cos = Math.cos(rotate);

		Vector vnew = new Vector(x * cos - z * sin, y, x * sin + z * cos);
		vnew.normalize();
		return vnew;
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {

			// エンティティによるダメージの場合

			// Bukkit.getLogger().info("edbe");
			event.setCancelled(true);
		} else if (event.getDamager().getType() == EntityType.SNOWBALL) {

			// 雪玉によるダメージの場合

			if (world.hasMetadata("ingame")
					&& event.getEntity().getType().equals(EntityType.PLAYER)) {

				// ゲーム中の場合

				Projectile projectile = (Projectile) event.getDamager();
				LivingEntity livingentity = projectile.getShooter();
				Player shooter = null;
				if (livingentity.getType().equals(EntityType.SKELETON)) {

					// スケルトンによる攻撃
					Bukkit.getLogger().info("by skel");

					shooter = Bukkit.getPlayer(livingentity
							.getMetadata("owner").get(0).asString());

				} else if (livingentity.getType().equals(EntityType.PLAYER)) {

					// プレイヤーによる攻撃
					Bukkit.getLogger().info("by player");

					shooter = (Player) projectile.getShooter();
				} else {

					Bukkit.getLogger().info("by null");

				}
				Player hitPlayer = (Player) event.getEntity();

				Bukkit.getLogger().info(
						shooter.getName() + "→" + hitPlayer.getName());

				if (!Spectator.isSpectating(hitPlayer)
						&& hitPlayer.getNoDamageTicks() == 0) {

					// 試合中かつ無敵時間じゃない場合

					String hitPlayerTeamName = hitPlayer
							.getMetadata("TeamName").get(0).asString();
					String shooterTeamName = shooter.getMetadata("TeamName")
							.get(0).asString();
					if (!shooterTeamName.equals(hitPlayerTeamName)) {

						// チーム、プレイヤーが異なる場合

						//

						event.setCancelled(true);

						// 無敵にして、リスポーン

						hitPlayer.setNoDamageTicks(plugin.getConfig().getInt(
								"Game.InvincibleTime"));
						double spawnx = world
								.getMetadata(hitPlayerTeamName + "Resx").get(0)
								.asDouble();
						double spawny = world
								.getMetadata(hitPlayerTeamName + "Resy").get(0)
								.asDouble();
						double spawnz = world
								.getMetadata(hitPlayerTeamName + "Resz").get(0)
								.asDouble();
						float spawnyaw = world
								.getMetadata(hitPlayerTeamName + "Resyaw")
								.get(0).asFloat();
						Location respawn = new Location(world, spawnx,
								spawny + 1, spawnz, spawnyaw, 0);
						hitPlayer.teleport(respawn);

						// アイテムリセット

						ItemStack[] sb = new ItemStack[36];
						for (int i = 0; i < plugin.getConfig().getInt(
								"Game.SnowBallStacks"); i++) {
							sb[i] = new ItemStack(Material.SNOW_BALL, 16);
						}
						hitPlayer.getInventory().clear();
						hitPlayer.getInventory().setContents(sb);

						// 経験値リセット

						hitPlayer.setExp(1.0f);

						// スコア追加

						Team shooterTeam = SnowBallBattle.board
								.getTeam(shooterTeamName);
						Score personalScore = SnowBallBattle.board
								.getObjective("Pscore").getScore(shooter);
						Score teamScore = SnowBallBattle.board.getObjective(
								"Tscore").getScore(
								Bukkit.getOfflinePlayer(shooterTeam.getPrefix()
										+ shooterTeamName
										+ shooterTeam.getSuffix()));

						personalScore.setScore(personalScore.getScore() + 1);
						teamScore.setScore(teamScore.getScore() + 1);
						shooter.playSound(shooter.getLocation(),
								Sound.SUCCESSFUL_HIT, 1, 1);
						shooter.giveExpLevels(1);
						for (Player player : Bukkit.getOnlinePlayers()) {
							player.sendMessage(SnowBallBattle.messagePrefix
									+ "" + shooterTeam.getPrefix()
									+ shooterTeam.getName() + " + 1pt! "
									+ ChatColor.RESET + " ("
									+ shooter.getName() + " → "
									+ hitPlayer.getName() + ")");
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