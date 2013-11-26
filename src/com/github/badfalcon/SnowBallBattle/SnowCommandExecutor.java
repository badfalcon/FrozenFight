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
					switch (args[0]) {
					case "setspawn":
						if (!player.hasMetadata("loc1")
								|| !player.hasMetadata("loc2")) {
							sender.sendMessage("範囲がスロットに記録されていません。");
							return false;
						} else {
							if (args[1] == null) {
								player.sendMessage("チーム名が与えられていません。");
								return false;
							} else {
								Location loc1 = (Location) player.getMetadata(
										"loc1").get(0);
								Location loc2 = (Location) player.getMetadata(
										"loc2").get(0);
								if ((int) loc1.getY() != (int) loc2.getY()) {
									player.sendMessage("高さが異なっています。");
									return false;
								} else if (loc1.getX() > loc2.getX()) {
									loc1.setX(loc1.getX() + loc2.getX());
									loc2.setX(loc1.getX() - loc2.getX());
									loc1.setX(loc1.getX() - loc2.getX());
								} else if (loc1.getY() > loc2.getY()) {
									loc1.setY(loc1.getX() + loc2.getY());
									loc2.setY(loc1.getX() - loc2.getY());
									loc1.setY(loc1.getX() - loc2.getY());
								}
								World world = Bukkit.getServer().getWorlds()
										.get(0);
								world.setMetadata(args[1] + "res1",
										new FixedMetadataValue(plugin, loc1));
								world.setMetadata(args[1] + "res2",
										new FixedMetadataValue(plugin, loc2));
								player.sendMessage(args[1] + "のリスポーン地点を\nX:"
										+ loc1.getX() + "~" + loc2.getX()
										+ "\nY" + loc1.getX() + "~"
										+ loc2.getZ() + "に設定しました。");
							}
						}
					case "start":
						ingame = true;
						plugin.getServer().broadcastMessage("ゲームを開始します。");
						Player[] players = plugin.getServer()
								.getOnlinePlayers();
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
					case "stop":
						if (game != null) {
							Bukkit.getServer().getScheduler()
									.cancelTask(game.getTaskId());
							game = new SnowTask(this.plugin).runTask(plugin);
						}
					default:
						return false;
					}
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
