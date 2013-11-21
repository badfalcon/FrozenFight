package com.github.badfalcon.SnowBallBattle;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class SnowCommandExecutor implements CommandExecutor {

	private SnowBallBattle plugin;

	public SnowCommandExecutor(SnowBallBattle plugin) {
		this.plugin = plugin;
	}

	Boolean ingame = false;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("sbb")) {
				if (args[0] == null) {

				} else {
					switch (args[0]) {
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
						String[] teams = plugin.getConfig()
								.getStringList("Team.TeamNames")
								.toArray(new String[0]);
						List<String> spectatorstringlist = plugin.getConfig()
								.getStringList("Game.Spectators");
						for (Player player1 : players) {
							if (!spectatorstringlist
									.contains(player1.getName())) {
								if(player1.getGameMode().equals(GameMode.CREATIVE)){
									player1.setGameMode(GameMode.SURVIVAL);
								}
								player1.setFoodLevel(20);
								int teamnumber = 0;
								for (int i = 0; i < teams.length; i++) {
									if (player1.getScoreboard()
											.getPlayerTeam(player1).getName()
											.equals(teams[i])) {
										teamnumber = i;
										player1.sendMessage("teamnumber = "
												+ teamnumber);
										break;
									}
								}
								player1.getInventory().clear();
								player1.getInventory().setArmorContents(
										armor[teamnumber]);
								player1.getInventory().setContents(sb);
							}
						}
						BukkitTask game = new SnowTask(this.plugin)
								.runTaskLater(this.plugin, 20 * 60 * plugin
										.getConfig().getInt("Game.GameTime"));
						return true;
					case "stop":
						if (ingame == true) {
							// plugin.getScheduler().cancelTask(game.getTaskId());
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
