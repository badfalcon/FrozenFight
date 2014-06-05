package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class SnowCommandExecutor implements CommandExecutor {

	SnowBallBattle plugin;
	BukkitTask gameStart;
	BukkitTask gameEnd;
	boolean ingame = false;
	int count = 10;

	public SnowCommandExecutor(SnowBallBattle plugin) {
		this.plugin = plugin;
	}

	World world = Bukkit.getServer().getWorlds().get(0);

	public boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfex) {
			return false;
		}
	}

	class SnowCountdown extends BukkitRunnable {

		private int countdown = count;

		public void run() {

			if (countdown < 10) {
				if (countdown <= 3) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.playSound(player.getLocation(), Sound.CLICK, 1,
								1);
					}
				}
				Bukkit.getServer().broadcastMessage(
						"[雪合戦]  ゲーム開始まで " + countdown);
			} else {
				Bukkit.getServer().broadcastMessage(
						"[雪合戦]  ゲーム開始まで" + countdown);
			}

			if (countdown > 1) {
				countdown--;
			} else {
				cancel();
			}
		}
	}

	boolean sendToLobby(CommandSender sender) {
		if (sender instanceof Player) {
			new SnowLobby(plugin).warpLobby((Player) sender);
			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "コマンドを実行したプレイヤー("
					+ sender.getName() + ")を特定できませんでした。");
			return false;
		}

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Spectator spec = new Spectator(plugin);
		/*
		 * sender.sendMessage("command = " + cmd.getName() + "\nlength = " +
		 * args.length); sender.sendMessage("label = " + label); for (int i = 0;
		 * i < args.length; i++) { sender.sendMessage("args[" + i + "] = " +
		 * args[i]); }
		 */
		// lobby

		if (cmd.getName().equalsIgnoreCase("lobby")) {
			if (world.hasMetadata("ingame")) {
				sender.sendMessage("ゲーム中はこのコマンドは実行できません");
				return true;
			}
			sendToLobby(sender);
		}

		// sbb

		if (cmd.getName().equalsIgnoreCase("sbb")) {
			if (args.length == 0) {
				sender.sendMessage("パラメータが足りません。");
				return false;
			}
			Player[] players = plugin.getServer().getOnlinePlayers();

			// getmeta

			if (args[0].equals("getmeta")) {
				if (args[1] == null || args.length != 3) {
					sender.sendMessage("パラメータエラー");
					return false;
				}
				if (args[1].equals("world")) {
					World world = Bukkit.getServer().getWorlds().get(0);
					if (args[2] == null) {
						sender.sendMessage("チーム名が与えられていません。");
						return false;
					}
					if (!plugin.getConfig().getStringList("Team.TeamNames")
							.contains(args[2])
							&& !args[2].equals("lobby")) {
						sender.sendMessage("チームが存在しません。もう一度確認して下さい。");
						return true;
					}
					if (!world.hasMetadata(args[2] + "set")) {
						sender.sendMessage("データがありません。");
						return true;
					}
					sender.sendMessage(args[2] + "のリスポーンポイントは");
					sender.sendMessage("x = "
							+ world.getMetadata(args[2] + "resx").get(0)
									.asString());
					sender.sendMessage("y = "
							+ world.getMetadata(args[2] + "resy").get(0)
									.asString());
					sender.sendMessage("z = "
							+ world.getMetadata(args[2] + "resz").get(0)
									.asString());
					return true;
				} else if (args[1].equals("player")) {
					if (args[2] == null) {
						sender.sendMessage("プレイヤー名が与えられていません。");
						return false;
					}
					if (!Arrays.asList(players).contains(
							Bukkit.getPlayer(args[2]))) {
						sender.sendMessage("プレイヤーは存在しません。");
						return true;
					}
					Player obj = Bukkit.getPlayer(args[2]);
					sender.sendMessage(args[2] + " team:"
							+ obj.getMetadata("team").get(0).asString());
					sender.sendMessage(args[2] + " teamcolor: "
							+ obj.getMetadata("teamcolor").get(0).asString());
					sender.sendMessage(args[2] + " spect:"
							+ obj.getMetadata("spectator").get(0).asString());
					return true;
				}
			}

			// rearrange

			else if (args[0].equals("rearrange")) {
				sender.sendMessage("under construction");
				/*
				if (world.hasMetadata("ingame")) {
					sender.sendMessage("ゲーム中はこのコマンドは実行できません");
					return true;
				}
				new SnowScoreboard(plugin).removePlayers();
				for (Player player : players) {
					new PlayerJoinTeam(plugin).joinTeam(player);
				}*/
				return true;
			}

			// set

			else if (args[0].equals("set")) {
				if (world.hasMetadata("ingame")) {
					sender.sendMessage("ゲーム中はこのコマンドは実行できません");
					return true;
				}

				if (sender instanceof Player) {
					if (args[1] == null) {
						sender.sendMessage("パラメータエラー");
						return false;
					}
					final Player player = (Player) sender;
					if (!player.hasMetadata("location")) {
						player.sendMessage("範囲がスロットに記録されていません。");
						return true;
					}
					World world = Bukkit.getServer().getWorlds().get(0);
					double locx = player.getMetadata("locx").get(0).asDouble();
					double locy = player.getMetadata("locy").get(0).asDouble();
					double locz = player.getMetadata("locz").get(0).asDouble();
					List<Float> locyaw = new ArrayList<Float>() {
						{
							add(player.getMetadata("locyaw").get(0).asFloat());
						}
					};

					// lobby

					if (args[1].equals("lobby")) {
						if (args.length != 2) {
							player.sendMessage("パラメータエラー");
							return false;
						}
						world.setMetadata("lobbyresx", new FixedMetadataValue(
								plugin, locx));
						world.setMetadata("lobbyresy", new FixedMetadataValue(
								plugin, locy));
						world.setMetadata("lobbyresz", new FixedMetadataValue(
								plugin, locz));
						world.setMetadata("lobbyyaw", new FixedMetadataValue(
								plugin, locyaw.get(0)));
						world.setMetadata("lobbyset", new FixedMetadataValue(
								plugin, true));
						plugin.getConfig().set("lobby",
								new Vector(locx, locy, locz));
						plugin.getConfig().set("lobbyyaw", locyaw);
						plugin.saveConfig();
						player.sendMessage("ロビーのリスポーン地点を\nX:" + locx + "\nY:"
								+ locy + "\nZ:" + locz + "に設定しました。");
						return true;
					}

					// spawn

					else if (args[1].equals("spawn")) {
						if (args.length != 3) {
							player.sendMessage("パラメータエラー");
							return false;
						}
						if (!plugin.getConfig().getStringList("Team.TeamNames")
								.contains(args[2])) {
							player.sendMessage("チームが存在しません。もう一度確認して下さい。");
							return true;
						}
						world.setMetadata(args[2] + "resx",
								new FixedMetadataValue(plugin, locx));
						world.setMetadata(args[2] + "resy",
								new FixedMetadataValue(plugin, locy));
						world.setMetadata(args[2] + "resz",
								new FixedMetadataValue(plugin, locz));
						world.setMetadata(args[2] + "resyaw",
								new FixedMetadataValue(plugin, locyaw.get(0)));
						world.setMetadata(args[2] + "set",
								new FixedMetadataValue(plugin, true));
						plugin.getConfig().set(args[2] + "respawn",
								new Vector(locx, locy, locz));
						plugin.getConfig().set(args[2] + "yaw", locyaw);
						plugin.saveConfig();
						player.sendMessage(args[2] + "のリスポーン地点を\nX:" + locx
								+ "\nY:" + locy + "\nZ:" + locz + "に設定しました。");
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "コマンドを実行したプレイヤー("
							+ sender.getName() + ")を特定できませんでした。");
					return false;
				}
			}

			// ready

			else if (args[0].equals("ready")) {
				if (world.hasMetadata("ingame")) {
					sender.sendMessage("ゲーム中はこのコマンドは実行できません");
					return true;
				}
				World world = Bukkit.getServer().getWorlds().get(0);
				if (!world.hasMetadata("lobbyset")) {
					sender.sendMessage("ロビーが設定されていません。");
					return true;
				}
				for (String teams : plugin.getConfig().getStringList(
						"Team.TeamNames")) {
					if (world.hasMetadata(teams + "set")) {
						continue;
					} else {
						sender.sendMessage(teams + "のリスポーンポイントが設定されていません。");
						return true;
					}
				}
				if (players.length > plugin.getConfig().getStringList("Team.TeamNames").size()
						* plugin.getConfig().getInt("Team.MaxPlayers")) {
					sender.sendMessage("チームサイズを超えています。");
					return true;
				}
				new SnowScoreboard(plugin).removePlayers();
				for (Player player : players) {
					new PlayerJoinTeam(plugin).joinTeam(player);

					//↓途中参加者への例外処理が不完全
					if (!spec.isSpectator(player.getName())) {

						String team = player.getMetadata("team").get(0).asString();
						double spawnx = world.getMetadata(team + "resx").get(0)
								.asDouble();
						double spawny = world.getMetadata(team + "resy").get(0)
								.asDouble();
						double spawnz = world.getMetadata(team + "resz").get(0)
								.asDouble();
						float spawnyaw = world.getMetadata(team + "resyaw").get(0)
								.asFloat();
						Location respawn = new Location(world, spawnx, spawny + 1,
								spawnz, spawnyaw, 0);
						player.setBedSpawnLocation(respawn, true);
						player.teleport(respawn);
						player.setWalkSpeed(0.001F);
						PotionEffect noJump = new PotionEffect(
								PotionEffectType.JUMP, 200, -100, false);
						player.addPotionEffect(noJump);
						for (Player player1 : players) {
							player.hidePlayer(player1);
						}
					}
				}
				world.setMetadata("ready", new FixedMetadataValue(plugin, true));
				// new SnowScoreboard(plugin).showScore();
				plugin.getServer().broadcastMessage("[雪合戦]  もうすぐゲームが始まります。");
				new SnowCountdown().runTaskTimer(plugin, 0, 20);
				gameStart = new SnowRunnableStart(this.plugin).runTaskLater(
						this.plugin, 20 * count);
				gameEnd = new SnowTask(this.plugin).runTaskLater(
						this.plugin,
						20 * (count + 60 * plugin.getConfig().getInt(
								"Game.GameTime")));
				return true;
			}

			// spectateheight

			else if (args[0].equals("spectateheight")) {
				if (world.hasMetadata("ingame")) {
					sender.sendMessage("ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (args[1].equals(null)) {
					sender.sendMessage("missing parameter");
					return false;
				} else {
					if (!isInteger(args[1])) {
						sender.sendMessage("must be an integer");
						return true;
					} else {
						plugin.getConfig().set("Spectator.Height", Integer.parseInt(args[1]));
						plugin.saveConfig();
						return true;
					}
				}
			}

			// stop

			else if (args[0].equals("stop")) {
				if (world.hasMetadata("ingame") && gameEnd != null) {
					Bukkit.getServer().getScheduler()
							.cancelTask(gameEnd.getTaskId());
					gameEnd = new SnowTask(this.plugin).runTask(plugin);
				}
			}

			// spectators

			else if (args[0].equals("spectators")) {
				if (world.hasMetadata("ingame")) {
					sender.sendMessage("ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (args[2].equals(null)) {
					sender.sendMessage("プレイヤー名が入力されていません。");
					return false;
				}
				if (!Arrays.asList(players).contains(Bukkit.getPlayer(args[2]))) {
					sender.sendMessage("プレイヤーは存在しません。");
					return true;
				}
				if (args[1].equals("add")) {
					if (spec.addSpectator(args[2])) {
						sender.sendMessage(args[2] + "をspectatorに追加しました。");
					} else {
						sender.sendMessage(args[2] + "はすでにspectatorです。");
					}
					return true;
				} else if (args[1].equals("remove")) {
					if (spec.removeSpectator(args[2])) {
						sender.sendMessage(args[2] + "をspectatorから削除しました。");
					} else {
						sender.sendMessage(args[2] + "をspectatorから削除できませんでした。");
					}
					return true;
				}
			}

			// maxplayers

			else if (args[0].equals("maxplayers")) {
				if (world.hasMetadata("ingame")) {
					sender.sendMessage("ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (args[1].equals(null)) {
					sender.sendMessage("missing parameter");
					return false;
				} else {
					if (!isInteger(args[1])) {
						sender.sendMessage("must be an integer");
						return true;
					} else {
						plugin.getConfig().set("Team.MaxPlayers", args[1]);
						plugin.saveConfig();
						return true;
					}
				}
			} else {
				sender.sendMessage("定義されていないコマンドです。");
				return false;
			}
			/*
			 * switch (args[0]) { case "setspawn": case "ready": case "start":
			 * case "stop": default: return false; }
			 */
		}
		return true;
	}
}
