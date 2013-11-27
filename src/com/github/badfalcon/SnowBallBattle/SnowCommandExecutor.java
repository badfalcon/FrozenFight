package com.github.badfalcon.SnowBallBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

public class SnowCommandExecutor implements CommandExecutor {

	private SnowBallBattle plugin;
	BukkitTask game;

	public SnowCommandExecutor(SnowBallBattle plugin) {
		this.plugin = plugin;
	}

	Boolean ingame = false;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("sbb")) {
				if (args[0] == null) {
					player.sendMessage("パラメータが足りません。");
					return false;
				} else {
					Player[] players = plugin.getServer().getOnlinePlayers();
					if (args[0].equals("setspawn")) {
						if (!player.hasMetadata("loc1x")
								|| !player.hasMetadata("loc2x")) {
							sender.sendMessage("範囲がスロットに記録されていません。");
							return false;
						} else {
							if (args[1] == null) {
								player.sendMessage("チーム名が与えられていません。");
								return false;
							} else {
								World world = Bukkit.getServer().getWorlds()
										.get(0);
								double loc1x = player.getMetadata("loc1x")
										.get(0).asDouble();
								double loc1y = player.getMetadata("loc1y")
										.get(0).asDouble();
								double loc1z = player.getMetadata("loc1z")
										.get(0).asDouble();
								double loc2x = player.getMetadata("loc2x")
										.get(0).asDouble();
								double loc2y = player.getMetadata("loc2y")
										.get(0).asDouble();
								double loc2z = player.getMetadata("loc2z")
										.get(0).asDouble();
								if ((int) loc1y != (int) loc2y) {
									player.sendMessage("高さが異なっています。");
									return false;
								} else if (loc1x > loc2x) {
									loc1x = loc1x + loc2x;
									loc2x = loc1x - loc2x;
									loc1x = loc1x - loc2x;
								} else if (loc1z > loc2z) {
									loc1z = loc1z + loc2z;
									loc2z = loc1z - loc2z;
									loc1z = loc1z - loc2z;
								}
								world.setMetadata(args[1] + "res1x",
										new FixedMetadataValue(plugin, loc1x));
								world.setMetadata(args[1] + "res1y",
										new FixedMetadataValue(plugin, loc1y));
								world.setMetadata(args[1] + "res1z",
										new FixedMetadataValue(plugin, loc1z));
								world.setMetadata(args[1] + "res2x",
										new FixedMetadataValue(plugin, loc2x));
								world.setMetadata(args[1] + "res2y",
										new FixedMetadataValue(plugin, loc2y));
								world.setMetadata(args[1] + "res2z",
										new FixedMetadataValue(plugin, loc2z));
								player.sendMessage(args[1] + "のリスポーン地点を\nX:"
										+ loc1x + "~" + loc2x
										+ "\nZ" + loc1z + "~"
										+ loc2z + "に設定しました。");
							}
						}
					} else if (args[0].equals("ready")) {
						World world = Bukkit.getServer().getWorlds().get(0);
						for (Player player1 : players) {
							if (!player1.getMetadata("spectator").get(0)
									.asBoolean()) {
								String team = player1.getMetadata("team")
										.get(0).asString();
								double loc1x =  world
										.getMetadata(team + "res1x").get(0).asDouble();
								double loc1y =  world
										.getMetadata(team + "res1y").get(0).asDouble();
								double loc1z =  world
										.getMetadata(team + "res1z").get(0).asDouble();
								double loc2x = world
										.getMetadata(team + "res2x").get(0).asDouble();
//								double loc2y = world
//										.getMetadata(team + "res2y").get(0).asDouble();
								double loc2z = world
										.getMetadata(team + "res2z").get(0).asDouble();
								double spawnx = loc1x
										+ Math.random()
										* (loc2x - loc1x);
								double spawny = loc1y + 1;
								double spawnz = loc1z
										+ Math.random()
										* (loc2z - loc1z);
								Location respawn = new Location(world,spawnx,spawny,spawnz);
								player1.setBedSpawnLocation(respawn);
								player1.teleport(respawn);
							}
						}
					} else if (args[0].equals("start")) {
						ingame = true;
						plugin.getServer().broadcastMessage("ゲームを開始します。");
						ItemStack[] sb = new ItemStack[36];
						String[] configarmor = plugin.getConfig()
								.getStringList("Team.TeamArmor")
								.toArray(new String[0]);
						String[][] armors = new String[configarmor.length][4];
						ItemStack[][] armor = new ItemStack[configarmor.length][4];
						for (int i = 0; i < armors.length; i++) {
							armors[i][0] = configarmor[i] + "_BOOTS";
							armors[i][1] = configarmor[i] + "_LEGGINGS";
							armors[i][2] = configarmor[i] + "_CHESTPLATE";
							armors[i][3] = configarmor[i] + "_HELMET";
							for (int j = 0; j < 4; j++) {
								armor[i][j] = new ItemStack(
										Material.getMaterial(armors[i][j]));
							}
						}
						for (int i = 0; i < plugin.getConfig().getInt(
								"Game.SnowBallStacks"); i++) {
							sb[i] = new ItemStack(Material.SNOW_BALL, 16);
						}
						for (Player player1 : players) {
							if (!player1.getMetadata("spectator").get(0)
									.asBoolean()) {
								if (player1.getGameMode().equals(
										GameMode.CREATIVE)) {
									player1.setGameMode(GameMode.SURVIVAL);
								}
								player1.setFoodLevel(20);
								int teamnumber = player1
										.getMetadata("teamnumber").get(0)
										.asInt();
								player1.getInventory().clear();
								player1.getInventory().setArmorContents(
										armor[teamnumber]);
								player1.getInventory().setContents(sb);
							}
						}
						game = new SnowTask(this.plugin).runTaskLater(
								this.plugin, 20 * 60 * plugin.getConfig()
										.getInt("Game.GameTime"));
						return true;
					}else if(args[0].equals("stop")){
						if (game != null) {
							Bukkit.getServer().getScheduler()
									.cancelTask(game.getTaskId());
							game = new SnowTask(this.plugin).runTask(plugin);
						}
					}else{
						return false;
					}
/*					switch (args[0]) {
					case "setspawn":
					case "ready":
					case "start":
					case "stop":
					default:
						return false;
					}*/
				}
			}
			return false;
		} else {
			sender.sendMessage(ChatColor.RED + "コマンドを実行したプレイヤー("
					+ sender.getName() + ")を特定できませんでした。");
			return false;
		}

	}

}
