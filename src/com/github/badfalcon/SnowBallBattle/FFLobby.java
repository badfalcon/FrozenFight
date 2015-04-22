package com.github.badfalcon.SnowBallBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class FFLobby {

	FrozenFight plugin;

	public FFLobby(FrozenFight plugin) {
		this.plugin = plugin;
	}

	World world = Bukkit.getWorlds().get(0);
	Location lobby = null;
	boolean set = true;

	public void warpLobby(Player player) {
		if (world.hasMetadata("lobbyset")) {
			double lobbyx = world.getMetadata("lobbyresx").get(0).asDouble();
			double lobbyy = world.getMetadata("lobbyresy").get(0).asDouble();
			double lobbyz = world.getMetadata("lobbyresz").get(0).asDouble();
			float lobbyyaw = world.getMetadata("lobbyyaw").get(0).asFloat();
			lobby = new Location(world, lobbyx, lobbyy + 1, lobbyz, lobbyyaw, 0);
			player.teleport(lobby);
			player.sendMessage(FrozenFight.messagePrefix + "ロビーへワープしました。");
		} else {
			Bukkit.getLogger().info(ChatColor.RED + "ロビーが設定されていません。");
		}
	}

	public void setLobby(Vector place, float yaw) {
		world.setMetadata("lobbyresx",
				new FixedMetadataValue(plugin, place.getX()));
		world.setMetadata("lobbyresy",
				new FixedMetadataValue(plugin, place.getY()));
		world.setMetadata("lobbyresz",
				new FixedMetadataValue(plugin, place.getZ()));
		world.setMetadata("lobbyyaw", new FixedMetadataValue(plugin, yaw));
		world.setMetadata("lobbyset", new FixedMetadataValue(plugin, true));
	}
}
