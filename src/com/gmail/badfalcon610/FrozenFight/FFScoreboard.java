package com.gmail.badfalcon610.FrozenFight;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class FFScoreboard {

	FrozenFight plugin;
	static Scoreboard board;

	public FFScoreboard(FrozenFight plugin) {
		this.plugin = plugin;
		board = FrozenFight.board;
	}

	World world = Bukkit.getWorlds().get(0);

	public void setScoreboard() {
		String[] teamNames = plugin.getConfig().getStringList("Team.Names")
				.toArray(new String[0]);

		Objective Tscore = board.registerNewObjective("Tscore", "dummy");
		Objective Pscore = board.registerNewObjective("Pscore", "dummy");

		int MaxTime = plugin.getConfig().getInt("Game.GameTime") * 60;
		int gamemin = MaxTime / 60;
		int gamesec = MaxTime % 60;

		String gamesecString;
		if (gamesec < 10) {
			gamesecString = "0" + String.valueOf(gamesec);
		} else {
			gamesecString = String.valueOf(gamesec);
		}

		Tscore.setDisplayName("Time  " + gamemin + ":" + gamesecString);
		Pscore.setDisplayName("points");

		for (String teamName : teamNames) {

			Team team = board.registerNewTeam(teamName);
			ChatColor teamColor = getChatColor(plugin.getConfig().getString(
					teamName + ".Color"));
			team.setPrefix(teamColor.toString());
			team.setSuffix(ChatColor.RESET.toString());
			team.setAllowFriendlyFire(false);

			OfflinePlayer teamPlayer = Bukkit.getOfflinePlayer(team.getPrefix()
					+ teamName + team.getSuffix());

			team.addPlayer(teamPlayer);
			Tscore.getScore(teamPlayer).setScore(0);

			if (plugin.getConfig().contains(teamName + "respawn")) {

				// リスポーン設定を含む場合

				Vector res = plugin.getConfig().getVector(teamName + "Respawn");
				float resyaw = plugin.getConfig()
						.getFloatList(teamName + "Yaw").get(0);
				world.setMetadata(teamName + "Resx", new FixedMetadataValue(
						plugin, res.getX()));
				world.setMetadata(teamName + "Resy", new FixedMetadataValue(
						plugin, res.getY()));
				world.setMetadata(teamName + "Resz", new FixedMetadataValue(
						plugin, res.getZ()));
				world.setMetadata(teamName + "Resyaw", new FixedMetadataValue(
						plugin, resyaw));
				world.setMetadata(teamName + "Set", new FixedMetadataValue(
						plugin, true));
			}
		}
	}

	ChatColor getChatColor(String str) {
		ChatColor[] colorNames = ChatColor.values();
		for (ChatColor color : colorNames) {
			if (color.name().equals(str)) {
				return color;
			}
		}
		return null;

	}

	public void resetScore() {
		Objective Pscores = board.getObjective("Pscore");
		for (Player player : Bukkit.getOnlinePlayers()) {
			Score Pscore = Pscores.getScore(player);
			Pscore.setScore(0);
		}
		Objective Tscores = board.getObjective("Tscore");
		List<String> teamNames = plugin.getConfig().getStringList("Team.Names");
		for (String teamName : teamNames) {
			Team team = board.getTeam(teamName);
			Set<OfflinePlayer> members = team.getPlayers();

			for (OfflinePlayer player : members) {
				team.removePlayer(player);
			}

			Score teamscore = Tscores.getScore(Bukkit.getOfflinePlayer(team
					.getPrefix() + teamName + team.getSuffix()));
			teamscore.setScore(0);
		}
	}

	public void showResult() {
		Player[] players = Bukkit.getOnlinePlayers();
		FFScoreboard.showScore();

		List<OfflinePlayer> winnerTeams = new ArrayList<OfflinePlayer>();
		List<String> teamNames = plugin.getConfig().getStringList("Team.Names");
		int winnerscore = 0;
		for (String teamName : teamNames) {
			Team t = FrozenFight.board.getTeam(teamName);
			OfflinePlayer team = Bukkit.getOfflinePlayer(t.getPrefix()
					+ teamName + t.getSuffix());
			Score tsc = FrozenFight.board.getObjective("Tscore").getScore(team);
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
				Score sc = FrozenFight.board.getObjective("Pscore").getScore(
						player);
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
				FrozenFight.messagePrefix + "この試合のMVPは" + mvpNames + "でした。スコア:"
						+ mvpscore + "pt");

		for (OfflinePlayer mvp : mvpPlayers) {
			if (mvp.isOnline()) {
				Player player = (Player) mvp;
				new FFFireworks(player, 20).runTaskTimer(plugin, 0, 10);
			}
		}
		Objective pscore = FrozenFight.board.getObjective("Pscore");

		for (Player player : players) {
			Score personal = pscore.getScore(player);
			if (!FFSpectator.isSpectating(player)) {

				// 個人成績の表示とゲームデータクリア

				FrozenFight.board.getTeam(
						player.getMetadata("TeamName").get(0).asString())
						.removePlayer(player);
				player.sendMessage(FrozenFight.messagePrefix + "あなたのスコアは "
						+ personal.getScore() + "pt でした。");
				// SnowBallBattle.board.getObjective("Tscore").setDisplayName("Time  finished");
				player.removeMetadata("TeamName", plugin);
			}
		}

	}

	public static void showScore() {
		Objective Pscore = board.getObjective("Pscore");
		Objective Tscore = board.getObjective("Tscore");
		Pscore.setDisplaySlot(DisplaySlot.BELOW_NAME);
		Pscore.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		Tscore.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public static void hideScore() {
		board.clearSlot(DisplaySlot.BELOW_NAME);
		board.clearSlot(DisplaySlot.PLAYER_LIST);
		board.clearSlot(DisplaySlot.SIDEBAR);
	}

	public static void hideScore(DisplaySlot slot) {
		board.clearSlot(slot);
	}

	public void removePlayers() {
		Set<Team> teams = board.getTeams();
		for (Team team : teams) {
			Set<OfflinePlayer> players = team.getPlayers();
			for (OfflinePlayer player : players) {
				team.removePlayer(player);
			}
		}
	}

}
