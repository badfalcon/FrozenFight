package com.gmail.badfalcon610.FrozenFight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class FFCommandExecutor implements CommandExecutor {

	FrozenFight plugin;
	BukkitTask gameStart;
	BukkitTask gameTimeCount;
	BukkitTask spawnItem;
	BukkitTask gameEnd;
	boolean ingame = false;
	int count = 10;

	public FFCommandExecutor(FrozenFight plugin) {
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

	boolean sendToLobby(CommandSender sender) {
		if (sender instanceof Player) {
			new FFLobby(plugin).warpLobby((Player) sender);
			return true;
		} else {
			sender.sendMessage(FrozenFight.messagePrefix + ChatColor.RED
					+ "コマンドを実行したプレイヤー(" + sender.getName() + ")を特定できませんでした。");
			return false;
		}

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		FFSpectator spec = new FFSpectator(plugin);
		/*
		 * sender.sendMessage("command = " + cmd.getName() + "\nlength = " +
		 * args.length); sender.sendMessage("label = " + label); for (int i = 0;
		 * i < args.length; i++) { sender.sendMessage("args[" + i + "] = " +
		 * args[i]); }
		 */
		// lobby

		if (cmd.getName().equalsIgnoreCase("l")) {
			if (sender instanceof Player) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}
				sendToLobby(sender);
				return true;
			} else {
				sender.sendMessage(FrozenFight.messagePrefix + ChatColor.RED
						+ "コマンドを実行したプレイヤー(" + sender.getName()
						+ ")を特定できませんでした。");
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("r")) {
			if (!(world.hasMetadata("ingame") || world.hasMetadata("ready"))) {
				sender.sendMessage(FrozenFight.messagePrefix
						+ "ゲーム外ではこのコマンドは実行できません");
				return true;
			}
			if (sender instanceof Player) {

				Player p = (Player) sender;

				FileConfiguration config = plugin.getConfig();

				int delay = 5;
				p.sendMessage(FrozenFight.messagePrefix + delay
						+ "秒後にリスポーンします。");
				new FFRespawn(p, config).runTaskLater(plugin, 20 * delay);
				return true;
			} else {
				sender.sendMessage(FrozenFight.messagePrefix + ChatColor.RED
						+ "コマンドを実行したプレイヤー(" + sender.getName()
						+ ")を特定できませんでした。");
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("all")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;

				if (!(world.hasMetadata("ingame") || world.hasMetadata("ready"))) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム外ではこのコマンドは実行できません");
					return true;
				}
				if (args.length == 0) {
					sender.sendMessage(FrozenFight.messagePrefix + "内容が無いよう");
					return true;
				}

				String message = "";
				for (String arg : args) {
					message += arg;
				}

				String teamName = p.getMetadata("TeamName").get(0).asString();
				Team team = FrozenFight.board.getTeam(teamName);

				for (Player player : Bukkit.getOnlinePlayers()) {
					player.sendMessage("[全体チャット] " + "<" + team.getPrefix()
							+ p.getName() + team.getSuffix() + "> " + message);
				}
				return true;
			} else {
				sender.sendMessage(FrozenFight.messagePrefix + ChatColor.RED
						+ "コマンドを実行したプレイヤー(" + sender.getName()
						+ ")を特定できませんでした。");
				return true;
			}
		}

		// ff

		else if (cmd.getName().equalsIgnoreCase("ff")) {

			FileConfiguration config = plugin.getConfig();

			if (args.length == 0) {
				sender.sendMessage(FrozenFight.messagePrefix + "パラメータが足りません。");
				return true;
			}
			Player[] players = plugin.getServer().getOnlinePlayers();

			// help

			if (args[0].equals("help")) {
				sender.sendMessage(FrozenFight.messagePrefix
						+ "SnowBallBattle ヘルプ\n" + "/l - ロビーへのワープ\n"
						+ "/r - リスポーン(ゲーム内)\n" + "/all <message> - 全体チャット\n"
						+ "/sbb help - ヘルプを表示\n"
						+ "/ff item list - アイテム一覧を表示\n"
						+ "/ff item toggle [item] - [item]を有効/無効\n"
						+ "/ff item dur [item] [time] - [item]の効果時間をを変更\n"
						+ "/ff item add [item] [time] - [item]のスポーン地点を追加\n"
						+ "/ff item rem [item] - 最寄りの[item]のスポーン地点を削除\n"
						+ "/ff ready - ゲーム開始コマンド\n"
						+ "/ff result - ゲームの結果を表示\n"
						+ "/ff set lobby - ロビーの登録\n"
						+ "/ff set spawn [team] - [team]のスポーン地点の登録\n"
						+ "/ff spect height [height] - 観戦者の最低高度を[height]に設定\n"
						+ "/ff spect add [player] - 観戦者に[player]を追加\n"
						+ "/ff spect remove [player] - 観戦者から[player]を削除\n"
						+ "/ff stop ゲームが進行中の時、ゲームを強制終了\n"
						+ "/ff teams list - チーム一覧を表示\n"
						+ "/ff teams add [team] [color] [armor] - チームを作成\n"
						+ "/ff teams remove [team] - チームを削除\n"
						+ "/ff update - コンフィグをアップデート");
			}

			// item

			else if (args[0].equals("item")) {

				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return true;
				}

				// toggle <ItemName>

				else if (args[1].equals("toggle")) {
					if (args[2] == null || args.length != 3) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					try {
						FFItem.valueOf(args[2]);
					} catch (Exception e) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "アイテムが存在しません。もう一度確認して下さい。");
						return true;
					}

					boolean bool = !config.getBoolean("Item." + args[2]
							+ ".Active");
					config.set("Item." + args[2] + ".Active", bool);
					sender.sendMessage(FrozenFight.messagePrefix + args[2]
							+ " toggled to " + bool);
					plugin.saveConfig();
					return true;
				}

				// item add <ItemName> <SpawnTime>

				else if (args[1].equals("add")) {
					if (args.length != 4) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					if (args[2] == null || args[3] == null) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					if (sender instanceof Player) {
						final Player player = (Player) sender;
						if (!player.hasMetadata("Location")) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "範囲がスロットに記録されていません。");
							return true;
						}

						try {
							FFItem.valueOf(args[2]);
						} catch (Exception e) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "アイテムが存在しません。もう一度確認して下さい。");
							return true;
						}

						String itemName = args[2];
						int spawnTime;
						try {
							int gameTime = config.getInt("Game.GameTime");
							spawnTime = Integer.parseInt(args[3]);
							if (gameTime < spawnTime) {
								sender.sendMessage(spawnTime
										+ "must be less than" + gameTime);
								return true;
							}
						} catch (Exception e) {
							sender.sendMessage(args[3] + "must be an integer");
							return true;
						}
						World world = Bukkit.getServer().getWorlds().get(0);
						double locx = player.getMetadata("Locx").get(0)
								.asDouble();
						double locy = player.getMetadata("Locy").get(0)
								.asDouble();
						double locz = player.getMetadata("Locz").get(0)
								.asDouble();
						double itemlocy = locy + 2;
						world.setMetadata(itemName + "Set",
								new FixedMetadataValue(plugin, true));
						int itemNum = config.getInt("Item." + itemName
								+ ".Numbers") + 1;
						config.set("Item." + itemName + ".Numbers", itemNum);
						config.set("Item." + itemName + ".num" + itemNum
								+ ".SpawnTime", spawnTime);
						config.set("Item." + itemName + ".num" + itemNum
								+ ".Spawn", new Vector(locx, itemlocy, locz));
						plugin.saveConfig();
						player.sendMessage(FrozenFight.messagePrefix + itemName
								+ "のスポーン地点を追加しました。");
						player.sendMessage(FrozenFight.messagePrefix + "time :"
								+ spawnTime);
						player.sendMessage(FrozenFight.messagePrefix
								+ "location : " + "X:" + locx + " Y:"
								+ itemlocy + " Z:" + locz);
						return true;
					} else {
						sender.sendMessage(FrozenFight.messagePrefix
								+ ChatColor.RED + "コマンドを実行したプレイヤー("
								+ sender.getName() + ")を特定できませんでした。");
						return true;
					}
				}

				// item dur <ItemName> <Duration>

				else if (args[1].equals("dur")) {
					if (args.length != 4) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					if (args[2] == null || args[3] == null) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					if (sender instanceof Player) {
						final Player player = (Player) sender;
						try {
							FFItem.valueOf(args[2]);
						} catch (Exception e) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "アイテムが存在しません。もう一度確認して下さい。");
							return true;
						}

						String itemName = args[2];
						int duration;
						try {
							int gameTime = config.getInt("Game.GameTime");
							duration = Integer.parseInt(args[3]);
							if (gameTime < duration) {
								sender.sendMessage(duration
										+ "must be less than" + gameTime);
								return true;
							}
						} catch (Exception e) {
							sender.sendMessage(args[3] + "must be an integer");
							return true;
						}
						config.set("Item." + itemName + ".Duration", duration);
						plugin.saveConfig();
						player.sendMessage(FrozenFight.messagePrefix + itemName
								+ "の効果時間を" + duration + "にしました。");

						return true;
					} else {
						sender.sendMessage(FrozenFight.messagePrefix
								+ ChatColor.RED + "コマンドを実行したプレイヤー("
								+ sender.getName() + ")を特定できませんでした。");
						return true;
					}
				}

				// item type <ItemName>

				else if (args[1].equals("type")) {
					if (args[2] == null || args.length != 3) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					if (sender instanceof Player) {

						Player player = (Player) sender;
						try {
							FFItem.valueOf(args[2]);
						} catch (Exception e) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "アイテムが存在しません。もう一度確認して下さい。");
							return true;
						}

						Material itemInHand = player.getItemInHand().getType();

						if (itemInHand.equals(Material.AIR)) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "手にアイテムがありません。");
							return true;
						}

						config.set("Item." + args[2] + ".Item", new ItemStack(
								itemInHand));
						plugin.saveConfig();
						player.sendMessage(FrozenFight.messagePrefix + args[2]
								+ "のアイテムタイプを" + itemInHand.toString() + "にしました");
						return true;
					} else {
						sender.sendMessage(FrozenFight.messagePrefix
								+ ChatColor.RED + "コマンドを実行したプレイヤー("
								+ sender.getName() + ")を特定できませんでした。");
						return true;
					}

				}

				// item rem <ItemName>

				else if (args[1].equals("rem")) {
					if (args[2] == null || args.length != 3) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					if (sender instanceof Player) {
						Player player = (Player) sender;
						Location playerLocation = player.getLocation();

						try {
							FFItem.valueOf(args[2]);
						} catch (Exception e) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "アイテムが存在しません。もう一度確認して下さい。");
							return true;
						}

						List<Location> itemLocations = new LinkedList<Location>();
						List<Integer> itemSpawnTimes = new LinkedList<Integer>();
						String itemName = args[2];

						int itemNum = config.getInt("Item." + itemName
								+ ".Numbers");

						if (itemNum <= 0) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "no locations registered");
							return true;
						}

						double nearestDistance = Double.MAX_VALUE;
						Location nearest = null;

						for (int i = 1; i <= itemNum; i++) {
							int spawnTime = config.getInt("Item." + itemName
									+ ".num" + i + ".SpawnTime");
							itemSpawnTimes.add(spawnTime);
							Vector vector = config.getVector("Item." + itemName
									+ ".num" + i + ".Spawn");
							Location location = new Location(world,
									vector.getX(), vector.getY(), vector.getZ());
							itemLocations.add(location);
							double distance = playerLocation.distance(location);
							if (distance < nearestDistance) {
								nearest = location;
								nearestDistance = distance;
							}
						}

						for (int i = 0; i < itemNum; i++) {
							try {
								int spawnTime = itemSpawnTimes.get(i);
								Location location = itemLocations.get(i);
								Vector vector = new Vector(location.getX(),
										location.getY(), location.getZ());
								if (location.equals(nearest)) {
									itemLocations.remove(i);
									itemSpawnTimes.remove(i);
									i--;
								} else {
									config.set("Item." + itemName + ".num" + i
											+ ".SpawnTime", spawnTime);
									config.set("Item." + itemName + ".num" + i
											+ ".Spawn", vector);
								}

							} catch (Exception e) {
								e.printStackTrace();
								config.set("Item." + itemName + ".num" + i,
										null);
							}
						}

						config.set("Item." + itemName + ".Numbers", itemNum - 1);

						player.sendMessage(FrozenFight.messagePrefix + "X:"
								+ nearest.getX() + " Y:" + nearest.getY()
								+ " Z:" + nearest.getZ() + " の" + itemName
								+ "を削除しました");

						plugin.saveConfig();

						return true;

					} else {
						sender.sendMessage(FrozenFight.messagePrefix
								+ ChatColor.RED + "コマンドを実行したプレイヤー("
								+ sender.getName() + ")を特定できませんでした。");
						return true;
					}
				}

				else if (args[1].equals("list")) {
					if (args.length != 2) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}

					FFItem[] si = FFItem.values();
					for (FFItem snowItem : si) {
						String itemName = snowItem.name();
						Boolean itemActive = config.getBoolean("Item."
								+ itemName + ".Active");
						sender.sendMessage(FrozenFight.messagePrefix + itemName
								+ "  active:" + itemActive);
						int itemNum = config.getInt("Item." + itemName
								+ ".Numbers");
						for (int j = 1; j <= itemNum; j++) {
							Vector vector = config.getVector("Item." + itemName
									+ ".num" + j + ".Spawn");
							Bukkit.getLogger().info(
									"Item." + itemName + ".num" + j
											+ ".SpawnTime");
							int spawnTime = config.getInt("Item." + itemName
									+ ".num" + j + ".SpawnTime");
							sender.sendMessage(FrozenFight.messagePrefix + j
									+ "  X:" + vector.getX() + " Y:"
									+ vector.getY() + " Z:" + vector.getZ()
									+ " , spawns at " + spawnTime + "min");
						}

					}
					return true;
				}

			}
			// ready

			else if (args[0].equals("ready")) {
				if (config.getString("Mode").equals("premade")) {
					if (TeamsWithoutPlayers()) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "teams need members!");
						return false;
					}
					if (MissingMembers()) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "missing member or wrong name!");
						return false;
					}
				}

				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return false;
				}
				if (config.getStringList("Team.Names").size() < 2) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "チーム数が少なすぎます。");
					return false;
				}
				World world = Bukkit.getServer().getWorlds().get(0);
				if (!world.hasMetadata("lobbyset")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ロビーが設定されていません。");
					return false;
				}
				for (String teamName : config.getStringList("Team.Names")) {
					if (world.hasMetadata(teamName + "Set")) {
						continue;
					} else {
						Team team = FrozenFight.board.getTeam(teamName);
						sender.sendMessage(FrozenFight.messagePrefix
								+ team.getPrefix() + team.getName()
								+ team.getSuffix() + "のリスポーンポイントが設定されていません。");
						return false;
					}
				}
				for (String itemName : config.getStringList("Item.Names")) {
					if (world.hasMetadata(itemName + "Set")) {
						continue;
					} else {
						sender.sendMessage(FrozenFight.messagePrefix + itemName
								+ "のスポーンポイントが設定されていません。");
						return false;
					}
				}

				// 確認完了

				FFScoreboard snowboard = new FFScoreboard(plugin);
				snowboard.resetScore();

				snowboard.removePlayers();
				FFTeam pjt = new FFTeam(plugin);
				if (config.getString("Mode").equals("premade")) {

					// premade

					// check

					for (String teamName : config.getStringList("Team.Names")) {
						for (String teamMember : config.getStringList(teamName
								+ "." + "Members")) {
							pjt.joinTeam(Bukkit.getPlayer(teamMember), teamName);
						}
					}

					for (Player player : players) {
						if (!player.hasMetadata("TeamName")) {
							spec.setSpectate(player);
						}
					}

				} else {

					// random

					for (Player player : players) {
						pjt.joinRandomTeam(player);

						// ↓途中参加者への例外処理が不完全
						if (!spec.isSpectator(player.getName())) {
							if (player.getGameMode().equals(GameMode.CREATIVE)) {
								player.setGameMode(GameMode.SURVIVAL);
							}

							FFTeam.warpToTeamSpawn(player);

							player.setWalkSpeed(0.001F);
							PotionEffect noJump = new PotionEffect(
									PotionEffectType.JUMP, 200, -100, false);
							player.addPotionEffect(noJump);
							for (Player player1 : players) {
								player.hidePlayer(player1);
							}
						} else {
							spec.setSpectate(player);
						}
					}
				}
				world.setMetadata("ready", new FixedMetadataValue(plugin, true));
				int MaxTime = config.getInt("Game.GameTime") * 60;
				int gamemin = MaxTime / 60;
				int gamesec = MaxTime % 60;

				String gamesecString;
				if (gamesec < 10) {
					gamesecString = "0" + String.valueOf(gamesec);
				} else {
					gamesecString = String.valueOf(gamesec);
				}

				// SnowBallBattle.board.getObjective("Tscore").setDisplayName(
				// "Time  " + gamemin + ":" + gamesecString);
				FrozenFight.board.getObjective("Tscore").setDisplayName(
						"チームスコア");
				FFScoreboard.showScore();
				plugin.getLogger().info("ゲーム開始コマンドが実行されました。");
				plugin.getServer().broadcastMessage(
						FrozenFight.messagePrefix + "もうすぐゲームが始まります。");

				for (Player player : players) {
					BarAPI.setMessage(player, "残り時間  " + gamemin + ":"
							+ gamesecString);
				}

				new FFGameStartCountdown(count).runTaskTimer(plugin, 0, 20);
				gameStart = new FFRunnableStart(this.plugin).runTaskLater(
						this.plugin, 20 * count);
				spawnItem = new FFSpawnItems(plugin).runTaskLater(plugin,
						20 * count);
				gameTimeCount = new FFGameCountdown(plugin).runTaskTimer(
						plugin, 20 * count, 20);
				gameEnd = new FFRunnableFinish(this.plugin).runTaskLater(
						this.plugin,
						20 * (count + 60 * config.getInt("Game.GameTime")));
				return true;
			}

			// result

			else if (args[0].equals("result")) {
				if (!world.hasMetadata("result")) {
					sender.sendMessage(FrozenFight.messagePrefix + "結果が存在しません");
					return false;
				}
				// スコア表示

				FFScoreboard.showScore();

				List<OfflinePlayer> winnerTeams = new ArrayList<OfflinePlayer>();
				List<String> teamNames = plugin.getConfig().getStringList(
						"Team.Names");
				int winnerscore = 0;
				for (String teamName : teamNames) {
					Team t = FrozenFight.board.getTeam(teamName);
					OfflinePlayer team = Bukkit.getOfflinePlayer(t.getPrefix()
							+ teamName + t.getSuffix());
					Score tsc = FrozenFight.board.getObjective("Tscore")
							.getScore(team);
					if (tsc.getScore() > winnerscore) {
						winnerTeams = new ArrayList<OfflinePlayer>();
						winnerTeams.add(team);
						winnerscore = tsc.getScore();
					} else if (tsc.getScore() == winnerscore) {
						winnerTeams.add(team);
					}
				}
				String winnerNames = "";
				for (OfflinePlayer winner : winnerTeams) {
					Team winnerTeam = FrozenFight.board.getTeam(ChatColor
							.stripColor(winner.getName()));
					String winnerName = winnerTeam.getPrefix()
							+ winner.getName() + winnerTeam.getSuffix();
					winnerNames += winnerName;
					if (!winner.equals(winnerTeams.get(winnerTeams.size() - 1))) {
						winnerNames += ",";
					}
				}

				List<OfflinePlayer> mvpPlayers = new ArrayList<OfflinePlayer>();
				int mvpscore = 0;
				for (Player player : players) {
					if (!FFSpectator.isSpectating(player)) {
						Score sc = FrozenFight.board.getObjective("Pscore")
								.getScore(player);
						if (sc.getScore() > mvpscore) {
							mvpPlayers = new ArrayList<OfflinePlayer>();
							mvpPlayers.add(player);
							mvpscore = sc.getScore();
						} else if (sc.getScore() == mvpscore) {
							mvpPlayers.add(player);
						}
					}
				}
				String mvpNames = "";
				for (OfflinePlayer mvp : mvpPlayers) {
					Team mvpTeam = Bukkit
							.getPlayer(mvp.getName())
							.getScoreboard()
							.getTeam(
									mvp.getPlayer().getMetadata("TeamName")
											.get(0).asString());
					String mvpName = mvpTeam.getPrefix() + mvp.getName()
							+ mvpTeam.getSuffix();
					mvpNames += mvpName;
					if (!mvp.equals(mvpPlayers.get(mvpPlayers.size() - 1))) {
						mvpNames += ",";
					}
				}

				if (winnerTeams.size() == 1) {
					plugin.getServer().broadcastMessage(
							FrozenFight.messagePrefix + "チーム" + winnerNames
									+ "の勝利です！ スコア:" + winnerscore + "pt");
				} else {
					plugin.getServer()
							.broadcastMessage(
									FrozenFight.messagePrefix + "チーム"
											+ winnerNames
											+ "による同点に終わりました。 スコア:"
											+ winnerscore + "pt");
				}

				plugin.getServer().broadcastMessage(
						FrozenFight.messagePrefix + "この試合のMVPは" + mvpNames
								+ "でした。スコア:" + mvpscore + "pt");

				for (OfflinePlayer mvp : mvpPlayers) {
					if (mvp.isOnline()) {
						Player player = (Player) mvp;
						new FFFireworks(player, 20).runTaskTimer(plugin, 0, 10);
					}
				}

				for (Player player : players) {
					if (!FFSpectator.isSpectating(player)) {

						// 個人成績の表示とゲームデータクリア

						Score personal = FrozenFight.board.getObjective(
								"Pscore").getScore(player);
						FrozenFight.board.getTeam(
								player.getMetadata("TeamName").get(0)
										.asString()).removePlayer(player);
						player.sendMessage(FrozenFight.messagePrefix
								+ "あなたのスコアは " + personal.getScore() + "pt でした。");
						// SnowBallBattle.board.getObjective("Tscore").setDisplayName("Time  finished");
						player.removeMetadata("TeamName", plugin);
					}
				}

				world.removeMetadata("result", plugin);

				return true;

			}

			// set

			else if (args[0].equals("set")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return false;
				}

				if (sender instanceof Player) {
					if (args[1] == null) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}
					final Player player = (Player) sender;
					if (!player.hasMetadata("Location")) {
						player.sendMessage(FrozenFight.messagePrefix
								+ "範囲がスロットに記録されていません。");
						return false;
					}
					World world = Bukkit.getServer().getWorlds().get(0);
					double locx = player.getMetadata("Locx").get(0).asDouble();
					double locy = player.getMetadata("Locy").get(0).asDouble();
					double locz = player.getMetadata("Locz").get(0).asDouble();
					float locyaw = player.getMetadata("Locyaw").get(0)
							.asFloat();

					List<Float> locyaw1 = new ArrayList<Float>();
					locyaw1.add(locyaw);

					// lobby

					if (args[1].equals("lobby")) {
						if (args.length != 2) {
							player.sendMessage(FrozenFight.messagePrefix
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
								plugin, locyaw));
						world.setMetadata("lobbyset", new FixedMetadataValue(
								plugin, true));
						config.set("lobby", new Vector(locx, locy, locz));
						config.set("lobbyyaw", locyaw1);
						plugin.saveConfig();
						player.sendMessage(FrozenFight.messagePrefix
								+ "ロビーのリスポーン地点を\nX:" + locx + "\nY:" + locy
								+ "\nZ:" + locz + "に設定しました。");
						return true;
					}

					// spawn

					else if (args[1].equals("spawn")) {
						if (args.length != 3) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "パラメータエラー");
							return false;
						}
						if (!config.getStringList("Team.Names").contains(
								args[2])) {
							player.sendMessage(FrozenFight.messagePrefix
									+ "チームが存在しません。もう一度確認して下さい。");
							return false;
						}
						world.setMetadata(args[2] + "Resx",
								new FixedMetadataValue(plugin, locx));
						world.setMetadata(args[2] + "Resy",
								new FixedMetadataValue(plugin, locy));
						world.setMetadata(args[2] + "Resz",
								new FixedMetadataValue(plugin, locz));
						world.setMetadata(args[2] + "Resyaw",
								new FixedMetadataValue(plugin, locyaw));
						world.setMetadata(args[2] + "Set",
								new FixedMetadataValue(plugin, true));
						config.set(args[2] + ".Respawn", new Vector(locx, locy,
								locz));
						config.set(args[2] + ".RespawnYaw", locyaw1);
						plugin.saveConfig();
						player.sendMessage(FrozenFight.messagePrefix + args[2]
								+ "のリスポーン地点を\nX:" + locx + "\nY:" + locy
								+ "\nZ:" + locz + "に設定しました。");
						return true;
					}

				} else {
					sender.sendMessage(FrozenFight.messagePrefix
							+ ChatColor.RED + "コマンドを実行したプレイヤー("
							+ sender.getName() + ")を特定できませんでした。");
					return false;
				}
			}

			// spect

			else if (args[0].equals("spect")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return false;
				}
				if (args[1].equals(null)) {
					sender.sendMessage(FrozenFight.messagePrefix + "パラメータエラー");
					return false;
				} else if (args[1].equals("add")) {
					if (args[2].equals(null)) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "プレイヤー名が入力されていません。");
						return false;
					}
					if (!Arrays.asList(players).contains(
							Bukkit.getPlayer(args[2]))) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "プレイヤーは存在しません。");
						return false;
					}
					if (spec.addSpectator(args[2])) {
						sender.sendMessage(FrozenFight.messagePrefix + args[2]
								+ "をspectatorに追加しました。");
						return true;
					} else {
						sender.sendMessage(FrozenFight.messagePrefix + args[2]
								+ "はすでにspectatorです。");
						return false;
					}
				} else if (args[1].equals("remove")) {
					if (args[2].equals(null)) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "プレイヤー名が入力されていません。");
						return false;
					}
					if (!Arrays.asList(players).contains(
							Bukkit.getPlayer(args[2]))) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "プレイヤーは存在しません。");
						return false;
					}
					if (spec.removeSpectator(args[2])) {
						sender.sendMessage(FrozenFight.messagePrefix + args[2]
								+ "をspectatorから削除しました。");
						return true;
					} else {
						sender.sendMessage(FrozenFight.messagePrefix + args[2]
								+ "をspectatorから削除できませんでした。");
						return false;
					}
				} else if (args[1].equals("height")) {
					if (args[2].equals(null)) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					} else {
						if (!isInteger(args[1])) {
							sender.sendMessage(FrozenFight.messagePrefix
									+ args[1] + "は整数にしてください。");
							return false;
						} else {
							sender.sendMessage(FrozenFight.messagePrefix
									+ "観戦の高さを" + args[1] + "に設定しました。");
							config.set("Spectator.Height",
									Integer.parseInt(args[1]));
							plugin.saveConfig();
							return true;
						}
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
					spawnItem.cancel();
					Bukkit.getServer().getScheduler()
							.cancelTask(spawnItem.getTaskId());

					for (Player player : Bukkit.getOnlinePlayers()) {
						player.setExp(0);
					}
					gameEnd = new FFRunnableFinish(this.plugin).runTask(plugin);
					return true;
				}
				return false;
			}

			// teams

			else if (args[0].equals("teams")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return false;
				}
				if (args[1] == null) {
					sender.sendMessage(FrozenFight.messagePrefix + "パラメーターエラー");
					return false;
				}
				if (args[1].equals("add")) {

					if (args[2] == null || args[3] == null || args[4] == null) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメータエラー");
						return false;
					}

					if (config.getStringList("Team.Names").contains(args[2])) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "既に存在しているチーム名です。");
						return false;
					}

					List<String> teamNames = config.getStringList("Team.Names");

					for (String teamName : teamNames) {
						if (config.getString(teamName + ".Color")
								.equalsIgnoreCase(args[3])) {
							sender.sendMessage(FrozenFight.messagePrefix
									+ "既に使われている色です。");
							return false;
						} else {
							continue;
						}
					}

					for (String teamName : teamNames) {
						if (config.getString(teamName + ".Armor")
								.equalsIgnoreCase(args[4])) {
							sender.sendMessage(FrozenFight.messagePrefix
									+ "既に使われている装備です。");
							return false;
						} else {
							continue;
						}
					}

					ChatColor teamColor = getColor(args[3]);
					if (teamColor == null) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "利用可能な色ではありません。");
						return false;
					}

					String armor = getArmor(args[4]);
					if (armor == null) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "利用可能な装備ではありません。");
						return false;
					}

					Team team = FrozenFight.board.registerNewTeam(args[2]);
					team.setPrefix(teamColor.toString());
					team.setSuffix(ChatColor.RESET.toString());
					team.setAllowFriendlyFire(false);
					OfflinePlayer teamPlayer = Bukkit.getOfflinePlayer(team
							.getPrefix() + team.getName() + team.getSuffix());

					team.addPlayer(teamPlayer);
					FrozenFight.board.getObjective("Tscore")
							.getScore(teamPlayer).setScore(0);

					// configへ保存
					teamNames.add(args[2]);
					config.set("Team.Names", teamNames);
					config.set(args[2] + ".Color", teamColor.toString());
					config.set(args[2] + ".Armor", armor);
					plugin.saveConfig();
					sender.sendMessage(FrozenFight.messagePrefix + "チーム:"
							+ team.getPrefix() + args[2] + team.getSuffix()
							+ "を作成しました。");
					sender.sendMessage(FrozenFight.messagePrefix
							+ "続けてリスポーン地点を設定してください。");
					return true;
				} else if (args[1].equals("remove")) {
					if (args[2] == null) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "パラメーターエラー。");
						return false;
					}
					if (FrozenFight.board.getTeam(args[2]) != null) {
						Team team = FrozenFight.board.getTeam(args[2]);
						String teamName = team.getPrefix() + args[2]
								+ team.getSuffix();
						team.unregister();
						List<String> teamNames = config
								.getStringList("Team.Names");
						teamNames.remove(args[2]);
						config.set("Team.Names", teamNames);
						config.set("args[2]", null);
						plugin.saveConfig();
						sender.sendMessage(FrozenFight.messagePrefix + "チーム:"
								+ teamName + "を削除しました。");
						return true;
					} else {
						sender.sendMessage(FrozenFight.messagePrefix
								+ "チームが存在しません。");
						return false;
					}
				} else if (args[1].equals("list")) {
					for (Team team : FrozenFight.board.getTeams()) {
						sender.sendMessage(FrozenFight.messagePrefix
								+ team.getPrefix() + team.getName()
								+ team.getSuffix());
					}
					return true;
				}
			}
			// update
			else if (args[0].equals("update")) {
				if (world.hasMetadata("ingame") || world.hasMetadata("ready")) {
					sender.sendMessage(FrozenFight.messagePrefix
							+ "ゲーム中はこのコマンドは実行できません");
					return false;
				}
				plugin.reloadConfig();
				sender.sendMessage(FrozenFight.messagePrefix
						+ "コンフィグをリロードしました。");
			} else {
				sender.sendMessage(FrozenFight.messagePrefix
						+ "定義されていないコマンドです。");
				return false;
			}
			/*
			 * switch (args[0]) { case "setspawn": case "ready": case "start":
			 * case "stop": default: return false; }
			 */
			return false;
		} else {
			return false;
		}
	}

	private boolean MissingMembers() {
		// TODO 自動生成されたメソッド・スタブ
		// for チーム
		List<String> teams = plugin.getConfig().getStringList("Team.Names");
		for (String team : teams) {
			List<String> teamMembers = plugin.getConfig().getStringList(
					team + ".Members");
			for (String teamMemberName : teamMembers) {
				Bukkit.getLogger().info("teammembername:" + teamMemberName);
				Player teamMember = Bukkit.getPlayer(teamMemberName);
				if (teamMember == null) {
					return true;
				} else {
					if (!teamMember.isOnline()) {
						return true;
					}
				}
			}
		}
		// for チームメンバー
		// if メンバーオフライン
		// return true
		return false;
	}

	private boolean TeamsWithoutPlayers() {
		// TODO 自動生成されたメソッド・スタブ
		// for チーム
		List<String> teams = plugin.getConfig().getStringList("Team.Names");
		for (String team : teams) {
			List<String> teamMembers = plugin.getConfig().getStringList(
					team + ".Members");
			Bukkit.getLogger().info(team + " membersize:" + teamMembers.size());
			if (teamMembers.size() == 0) {
				return true;
			}
		}
		// if チームメンバー.length = 0
		// return true
		return false;
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
