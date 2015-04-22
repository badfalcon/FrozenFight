package com.gmail.badfalcon610.FrozenFight;

import java.util.ArrayList;
import java.util.List;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public class FFRunnableFinish extends BukkitRunnable {

	private FrozenFight plugin;
	BukkitTask send;
	FFSpectator spec;

	public FFRunnableFinish(FrozenFight plugin) {
		this.plugin = plugin;
		spec = new FFSpectator(plugin);
	}

	public void run() {

		Player[] players = plugin.getServer().getOnlinePlayers();
		ItemStack[] clear = { new ItemStack(Material.AIR),
				new ItemStack(Material.AIR), new ItemStack(Material.AIR),
				new ItemStack(Material.AIR) };

		// スコア表示

		List<OfflinePlayer> winnerTeams = new ArrayList<OfflinePlayer>();
		List<String> teamNames = plugin.getConfig().getStringList("Team.Names");
		int winnerscore = 0;
		for (String teamName : teamNames) {
			Team t = FrozenFight.board.getTeam(teamName);
			OfflinePlayer team = Bukkit.getOfflinePlayer(t.getPrefix()
					+ teamName + t.getSuffix());
			Score tsc = FrozenFight.board.getObjective("Tscore").getScore(
					team);
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
			String winnerName = winnerTeam.getPrefix() + winner.getName()
					+ winnerTeam.getSuffix();
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
							mvp.getPlayer().getMetadata("TeamName").get(0)
									.asString());
			String mvpName = mvpTeam.getPrefix() + mvp.getName()
					+ mvpTeam.getSuffix();
			mvpNames += mvpName;
			if (!mvp.equals(mvpPlayers.get(mvpPlayers.size() - 1))) {
				mvpNames += ",";
			}
		}

		plugin.getServer().broadcastMessage(
				FrozenFight.messagePrefix + "ゲームが終了しました。");
		if (winnerTeams.size() == 1) {
			plugin.getServer().broadcastMessage(
					FrozenFight.messagePrefix + "チーム" + winnerNames
							+ "の勝利です！ スコア:" + winnerscore + "pt");
		} else {
			plugin.getServer().broadcastMessage(
					FrozenFight.messagePrefix + "チーム" + winnerNames
							+ "による同点に終わりました。 スコア:" + winnerscore + "pt");
		}

		plugin.getServer().broadcastMessage(
				FrozenFight.messagePrefix + "この試合のMVPは" + mvpNames
						+ "でした。スコア:" + mvpscore + "pt");

		for (Player player : players) {
			if (!FFSpectator.isSpectating(player)) {

				// 個人成績の表示とゲームデータクリア

				Score personal = FrozenFight.board.getObjective("Pscore")
						.getScore(player);
				FrozenFight.board.getTeam(
						player.getMetadata("TeamName").get(0).asString())
						.removePlayer(player);
				player.sendMessage(FrozenFight.messagePrefix + "あなたのスコアは "
						+ personal.getScore() + "pt でした。");
				player.getInventory().setArmorContents(clear);
				player.getInventory().clear();
				player.setLevel(0);
				player.setExp(0);
				player.removeMetadata("TeamName", plugin);
				// SnowBallBattle.board.getObjective("Tscore").setDisplayName("Time  finished");
				BarAPI.setMessage(player, "残り時間  finished");
			}
			for (Player player1 : players) {
				if (!player.canSee(player1)) {
					player.showPlayer(player1);
				}
			}
			player.sendMessage(FrozenFight.messagePrefix + "ロビーへ転送します。");
		}
		World world = Bukkit.getWorlds().get(0);
		world.removeMetadata("ingame", plugin);
		send = new sendToLobby().runTaskLater(this.plugin, 60);
	}

	public class sendToLobby extends BukkitRunnable {

		public sendToLobby() {

		}

		public void run() {
			Player[] players = plugin.getServer().getOnlinePlayers();
			FFScoreboard snowboard = new FFScoreboard(plugin);
			snowboard.hideScore();
			snowboard.resetScore();
			for (Player player : players) {
				BarAPI.removeBar(player);
				if(FFSpectator.isSpectating(player)){
					spec.removeSpectate(player);
				}
				if (spec.isSpectator(player.getName())) {
					spec.removeSpectate(player);
				}
				new FFLobby(plugin).warpLobby(player);
			}
		}
	}
}
