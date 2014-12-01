package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class SnowCommandExecutor implements CommandExecutor {

	SnowBallBattle plugin;
	BukkitTask gameStart;
	BukkitTask gameTimeCount;
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

	class GameCountdown extends BukkitRunnable {
		private int maxtime;
		private int gametime;

		public GameCountdown() {
			maxtime = plugin.getConfig().getInt("Game.GameTime") * 60;
			gametime = plugin.getConfig().getInt("Game.GameTime") * 60;
		}

		public void run() {
			int gamemin = gametime / 60;
			int gamesec = gametime % 60;

			String gamesecString;
			if (gamesec < 10) {
				gamesecString = "0" + String.valueOf(gamesec);
			} else {
				gamesecString = String.valueOf(gamesec);
			}

			SnowBallBattle.board.getObjective("Tscore").setDisplayName(
					"Time  " + gamemin + ":" + gamesecString);
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.sendMessage("T :" + ((float) gametime / (float) maxtime));
				BarAPI.setHealth(player, (float) gametime / (float) maxtime
						* 100F);
				int maxFillTime = plugin.getConfig().getInt(
						"Game.GiveSnowBallTime");
				float current = player.getExp();
				if (current - (float) (1.0f / maxFillTime) <= 0.0f) {
					/*
					 * player.sendMessage(String.valueOf(current - (float) (1.0f
					 * / maxFillTime)));
					 * player.sendMessage(String.valueOf(gametime));
					 * player.sendMessage(String.valueOf(gametime != maxtime &&
					 * gametime != 0));
					 */
					if (gametime != maxtime && gametime != 0) {
						int SnowNum = plugin.getConfig().getInt(
								"Game.GiveSnowBallNum");
						player.getInventory().addItem(
								new ItemStack(Material.SNOW_BALL, SnowNum));
					}
					player.setExp(1.0f);
				} else {
					player.setExp(current - (float) (1.0f / maxFillTime));
				}
			}
			if (gametime > 0) {
				gametime--;
			} else {
				for (Player player : Bukkit.getOnlinePlayers()) {
					BarAPI.removeBar(player);
				}
				cancel();
			}
		}
	}

	class SnowCountdown extends BukkitRunnable {

		private int countdown = count;

		public void run() {

			if (countdown < 10) {
				Player[] players = Bukkit.getOnlinePlayers();
				if (countdown <= 3) {
					for (Player player : players) {
						player.playSound(player.getLocation(), Sound.CLICK, 1,
								1);
					}
				}
				for (Player player : players) {
					player.sendMessage(SnowBallBattle.messagePrefix
							+ "ゲーム開始まで " + countdown);
				}
			} else {
				Bukkit.getServer().broadcastMessage(
						SnowBallBattle.messagePrefix + "ゲーム開始まで" + countdown);
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
			sender.sendMessage(SnowBallBattle.messagePrefix + ChatColor.RED
					+ "コマンドを実行したプレイヤー(" + sender.getName() + ")を特定できませんでした。");
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
			if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
				sender.sendMessage(SnowBallBattle.messagePrefix
						+ "ゲーム中はこのコマンドは実行できません");
				return true;
			}
			sendToLobby(sender);
			return true;
		}

		// sbb

		if (cmd.getName().equalsIgnoreCase("sbb")) {
			if (args.length == 0) {
				sender.sendMessage(SnowBallBattle.messagePrefix
						+ "パラメータが足りません。");
				return false;
			}
			Player[] players = plugin.getServer().getOnlinePlayers();

			// getmeta

			if (args[0].equals("getmeta")) {
				if (args[1] == null || args.length != 3) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "パラメータエラー");
					return false;
				}
				if (args[1].equals("world")) {
					World world = Bukkit.getServer().getWorlds().get(0);
					if (args[2] == null) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "チーム名が与えられていません。");
						return false;
					}
					if (!plugin.getConfig().getStringList("Team.Names")
							.contains(args[2])
							&& !args[2].equals("lobby")) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "チームが存在しません。もう一度確認して下さい。");
						return true;
					}
					if (!world.hasMetadata(args[2] + "set")) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "データがありません。");
						return true;
					}
					sender.sendMessage(SnowBallBattle.messagePrefix + args[2]
							+ "のリスポーンポイントは");
					sender.sendMessage("x = "
							+ world.getMetadata(args[2] + "Resx").get(0)
									.asString());
					sender.sendMessage("y = "
							+ world.getMetadata(args[2] + "Resy").get(0)
									.asString());
					sender.sendMessage("z = "
							+ world.getMetadata(args[2] + "Resz").get(0)
									.asString());
					return true;
				} else if (args[1].equals("player")) {
					if (world.hasMetadata("ingame")) {
						if (args[2] == null) {
							sender.sendMessage(SnowBallBattle.messagePrefix
									+ "プレイヤー名が与えられていません。");
							return false;
						}
						if (!Arrays.asList(players).contains(
								Bukkit.getPlayer(args[2]))) {
							sender.sendMessage(SnowBallBattle.messagePrefix
									+ "プレイヤーは存在しません。");
							return true;
						}
						Player obj = Bukkit.getPlayer(args[2]);
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ args[2] + " team:"
								+ obj.getMetadata("team").get(0).asString());
						return true;
					} else {
						return true;
					}
				}
			}

			// rearrange

			else if (args[0].equals("rearrange")) {
				sender.sendMessage("under construction");
				/*
				 * if (world.hasMetadata("ingame")) {
				 * sender.sendMessage("ゲーム中はこのコマンドは実行できません"); return true; } new
				 * SnowScoreboard(plugin).removePlayers(); for (Player player :
				 * players) { new PlayerJoinTeam(plugin).joinTeam(player); }
				 */
				return true;
			}

			// set

			else if (args[0].equals("set")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}

				if (sender instanceof Player) {
					if (args[1] == null) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					final Player player = (Player) sender;
					if (!player.hasMetadata("Location")) {
						player.sendMessage(SnowBallBattle.messagePrefix
								+ "範囲がスロットに記録されていません。");
						return true;
					}
					World world = Bukkit.getServer().getWorlds().get(0);
					double locx = player.getMetadata("Locx").get(0).asDouble();
					double locy = player.getMetadata("Locy").get(0).asDouble();
					double locz = player.getMetadata("Locz").get(0).asDouble();
					List<Float> locyaw = new ArrayList<Float>() {
						{
							add(player.getMetadata("Locyaw").get(0).asFloat());
						}
					};

					// lobby

					if (args[1].equals("lobby")) {
						if (args.length != 2) {
							player.sendMessage(SnowBallBattle.messagePrefix
									+ "パラメータエラー");
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
						player.sendMessage(SnowBallBattle.messagePrefix
								+ "ロビーのリスポーン地点を\nX:" + locx + "\nY:" + locy
								+ "\nZ:" + locz + "に設定しました。");
						return true;
					}

					// spawn

					else if (args[1].equals("spawn")) {
						if (args.length != 3) {
							player.sendMessage(SnowBallBattle.messagePrefix
									+ "パラメータエラー");
							return false;
						}
						if (!plugin.getConfig().getStringList("Team.Names")
								.contains(args[2])) {
							player.sendMessage(SnowBallBattle.messagePrefix
									+ "チームが存在しません。もう一度確認して下さい。");
							return true;
						}
						world.setMetadata(args[2] + "Resx",
								new FixedMetadataValue(plugin, locx));
						world.setMetadata(args[2] + "Resy",
								new FixedMetadataValue(plugin, locy));
						world.setMetadata(args[2] + "Resz",
								new FixedMetadataValue(plugin, locz));
						world.setMetadata(args[2] + "Resyaw",
								new FixedMetadataValue(plugin, locyaw.get(0)));
						world.setMetadata(args[2] + "Set",
								new FixedMetadataValue(plugin, true));
						plugin.getConfig().set(args[2] + ".Respawn",
								new Vector(locx, locy, locz));
						plugin.getConfig().set(args[2] + ".RespawnYaw", locyaw);
						plugin.saveConfig();
						player.sendMessage(SnowBallBattle.messagePrefix
								+ args[2] + "のリスポーン地点を\nX:" + locx + "\nY:"
								+ locy + "\nZ:" + locz + "に設定しました。");
						return true;
					}
				} else {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ ChatColor.RED + "コマンドを実行したプレイヤー("
							+ sender.getName() + ")を特定できませんでした。");
					return false;
				}
			}

			// ready

			else if (args[0].equals("ready")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (plugin.getConfig().getStringList("Team.Names").size() < 2) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "チーム数が少なすぎます。");
					return true;
				}
				World world = Bukkit.getServer().getWorlds().get(0);
				if (!world.hasMetadata("lobbyset")) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "ロビーが設定されていません。");
					return true;
				}
				for (String teamName : plugin.getConfig().getStringList(
						"Team.Names")) {
					if (world.hasMetadata(teamName + "Set")) {
						continue;
					} else {
						Team team = SnowBallBattle.board.getTeam(teamName);
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ team.getPrefix() + team.getName()
								+ team.getSuffix() + "のリスポーンポイントが設定されていません。");
						return true;
					}
				}
				new SnowScoreboard(plugin).removePlayers();
				for (Player player : players) {
					new PlayerJoinTeam(plugin).joinTeam(player);

					// ↓途中参加者への例外処理が不完全
					if (!spec.isSpectator(player.getName())) {

						String team = player.getMetadata("TeamName").get(0)
								.asString();
						double spawnx = world.getMetadata(team + "Resx").get(0)
								.asDouble();
						double spawny = world.getMetadata(team + "Resy").get(0)
								.asDouble();
						double spawnz = world.getMetadata(team + "Resz").get(0)
								.asDouble();
						float spawnyaw = world.getMetadata(team + "Resyaw")
								.get(0).asFloat();
						Location respawn = new Location(world, spawnx,
								spawny + 1, spawnz, spawnyaw, 0);
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
				int MaxTime = plugin.getConfig().getInt("Game.GameTime") * 60;
				int gamemin = MaxTime / 60;
				int gamesec = MaxTime % 60;

				String gamesecString;
				if (gamesec < 10) {
					gamesecString = "0" + String.valueOf(gamesec);
				} else {
					gamesecString = String.valueOf(gamesec);
				}

				SnowBallBattle.board.getObjective("Tscore").setDisplayName(
						"Time  " + gamemin + ":" + gamesecString);
				new SnowScoreboard(plugin).showScore();
				plugin.getLogger().info("ゲーム開始コマンドが実行されました。");
				plugin.getServer().broadcastMessage(
						SnowBallBattle.messagePrefix + "もうすぐゲームが始まります。");
				for (Player player : players) {
					BarAPI.setMessage(player, "残り時間");
				}
				new SnowCountdown().runTaskTimer(plugin, 0, 20);
				gameStart = new SnowRunnableStart(this.plugin).runTaskLater(
						this.plugin, 20 * count);
				gameTimeCount = new GameCountdown().runTaskTimer(plugin,
						20 * count, 20);
				gameEnd = new SnowRunnableFinish(this.plugin).runTaskLater(
						this.plugin, 20 * (count + 60 * plugin.getConfig()
								.getInt("Game.GameTime")));
				return true;
			}

			// spectateheight

			else if (args[0].equals("spectateheight")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (args[1].equals(null)) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "パラメータエラー");
					return false;
				} else {
					if (!isInteger(args[1])) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ args[1] + "は整数にしてください。");
						return true;
					} else {
						plugin.getConfig().set("Spectator.Height",
								Integer.parseInt(args[1]));
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
					Bukkit.getServer().getScheduler()
							.cancelTask(gameTimeCount.getTaskId());
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.setExp(0);
					}
					gameEnd = new SnowRunnableFinish(this.plugin)
							.runTask(plugin);
				}
			}

			// spectators

			else if (args[0].equals("spectators")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (args[2].equals(null)) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "プレイヤー名が入力されていません。");
					return false;
				}
				if (!Arrays.asList(players).contains(Bukkit.getPlayer(args[2]))) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "プレイヤーは存在しません。");
					return true;
				}
				if (args[1].equals("add")) {
					if (spec.addSpectator(args[2])) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ args[2] + "をspectatorに追加しました。");
					} else {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ args[2] + "はすでにspectatorです。");
					}
					return true;
				} else if (args[1].equals("remove")) {
					if (spec.removeSpectator(args[2])) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ args[2] + "をspectatorから削除しました。");
					} else {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ args[2] + "をspectatorから削除できませんでした。");
					}
					return true;
				}
			}

			// maxplayers

			else if (args[0].equals("maxplayers")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (args[1].equals(null)) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "パラメータエラー");
					return false;
				} else {
					if (!isInteger(args[1])) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ args[1] + "は整数にしてください。");
						return true;
					} else {
						plugin.getConfig().set("Team.MaxPlayers", args[1]);
						plugin.saveConfig();
						return true;
					}
				}
			} else if (args[0].equals("teams")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}
				if (args[1] == null) {
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "パラメーターエラー");
					return false;
				}
				if (args[1].equals("add")) {

					if (args[2] == null || args[3] == null || args[4] == null) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "パラメータエラー");
						return false;
					}

					if (plugin.getConfig().getStringList("Team.Names")
							.contains(args[2])) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "既に存在しているチーム名です。");
						return true;
					}

					List<String> teamNames = plugin.getConfig().getStringList(
							"Team.Names");

					for (String teamName : teamNames) {
						if (plugin.getConfig().getString(teamName + ".Color")
								.equalsIgnoreCase(args[3])) {
							sender.sendMessage(SnowBallBattle.messagePrefix
									+ "既に使われている色です。");
							return true;
						} else {
							continue;
						}
					}

					for (String teamName : teamNames) {
						if (plugin.getConfig().getString(teamName + ".Armor")
								.equalsIgnoreCase(args[4])) {
							sender.sendMessage(SnowBallBattle.messagePrefix
									+ "既に使われている装備です。");
							return true;
						} else {
							continue;
						}
					}

					ChatColor teamColor = getColor(args[3]);
					if (teamColor == null) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "利用可能な色ではありません。");
						return true;
					}

					String armor = getArmor(args[4]);
					if (armor == null) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "利用可能な装備ではありません。");
						return true;
					}

					Team team = SnowBallBattle.board.registerNewTeam(args[2]);
					team.setPrefix(teamColor.toString());
					team.setSuffix(ChatColor.RESET.toString());
					team.setAllowFriendlyFire(false);
					OfflinePlayer teamPlayer = Bukkit.getOfflinePlayer(team
							.getPrefix() + team.getName() + team.getSuffix());

					team.addPlayer(teamPlayer);
					SnowBallBattle.board.getObjective("Tscore")
							.getScore(teamPlayer).setScore(0);

					// configへ保存
					teamNames.add(args[2]);
					plugin.getConfig().set("Team.Names", teamNames);
					plugin.getConfig().set(args[2] + ".Color",
							teamColor.toString());
					plugin.getConfig().set(args[2] + ".Armor", armor);
					plugin.saveConfig();
					sender.sendMessage(SnowBallBattle.messagePrefix + "チーム:"
							+ team.getPrefix() + args[2] + team.getSuffix()
							+ "を作成しました。");
					sender.sendMessage(SnowBallBattle.messagePrefix
							+ "続けてリスポーン地点を設定してください。");
					return true;
				} else if (args[1].equals("remove")) {
					if (args[2] == null) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "パラメーターエラー。");
						return false;
					}
					if (SnowBallBattle.board.getTeam(args[2]) != null) {
						Team team = SnowBallBattle.board.getTeam(args[2]);
						String teamName = team.getPrefix() + args[2]
								+ team.getSuffix();
						team.unregister();
						List<String> teamNames = plugin.getConfig()
								.getStringList("Team.Names");
						teamNames.remove(args[2]);
						plugin.getConfig().set("Team.Names", teamNames);
						plugin.getConfig().set("args[2]", null);
						plugin.saveConfig();
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "チーム:" + teamName + "を削除しました。");
						return true;
					} else {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ "チームが存在しません。");
						return true;
					}
				} else if (args[1].equals("list")) {
					for (Team team : SnowBallBattle.board.getTeams()) {
						sender.sendMessage(SnowBallBattle.messagePrefix
								+ team.getPrefix() + team.getName()
								+ team.getSuffix());
					}
				}
			} else {
				sender.sendMessage(SnowBallBattle.messagePrefix
						+ "定義されていないコマンドです。");
				return false;
			}
			/*
			 * switch (args[0]) { case "setspawn": case "ready": case "start":
			 * case "stop": default: return false; }
			 */
		}
		return true;
	}

	public String getArmor(String str) {
		if (str.equalsIgnoreCase("leather")) {
			return "LEATHER";
		} else if (str.equalsIgnoreCase("chainmail")) {
			return "CHAINMAIL";
		} else if (str.equalsIgnoreCase("iron")) {
			return "IRON";
		} else if (str.equalsIgnoreCase("gold")) {
			return "GOLD";
		} else if (str.equalsIgnoreCase("diamond")) {
			return "DIAMOND";
		} else {
			return null;
		}
	}

	public ChatColor getColor(String str) {
		if (str.equalsIgnoreCase("black")) {
			return ChatColor.BLACK;
		} else if (str.equalsIgnoreCase("dark_blue")) {
			return ChatColor.DARK_BLUE;
		} else if (str.equalsIgnoreCase("dark_green")) {
			return ChatColor.DARK_GREEN;
		} else if (str.equalsIgnoreCase("dark_aqua")) {
			return ChatColor.DARK_AQUA;
		} else if (str.equalsIgnoreCase("dark_red")) {
			return ChatColor.DARK_RED;
		} else if (str.equalsIgnoreCase("dark_purple")) {
			return ChatColor.DARK_PURPLE;
		} else if (str.equalsIgnoreCase("gold")) {
			return ChatColor.GOLD;
		} else if (str.equalsIgnoreCase("gray")) {
			return ChatColor.GRAY;
		} else if (str.equalsIgnoreCase("dark_gray")) {
			return ChatColor.DARK_GRAY;
		} else if (str.equalsIgnoreCase("blue")) {
			return ChatColor.BLUE;
		} else if (str.equalsIgnoreCase("green")) {
			return ChatColor.GREEN;
		} else if (str.equalsIgnoreCase("aqua")) {
			return ChatColor.AQUA;
		} else if (str.equalsIgnoreCase("red")) {
			return ChatColor.RED;
		} else if (str.equalsIgnoreCase("light_purple")) {
			return ChatColor.LIGHT_PURPLE;
		} else if (str.equalsIgnoreCase("yellow")) {
			return ChatColor.YELLOW;
		} else if (str.equalsIgnoreCase("white")) {
			return ChatColor.WHITE;
		} else {
			return null;
		}
	}
}
