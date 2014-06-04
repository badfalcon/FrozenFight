package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class SnowTabCompleter implements TabCompleter {

	SnowBallBattle plugin;

	public SnowTabCompleter(SnowBallBattle plugin) {
		this.plugin = plugin;
	}

	String[] commands;
	List<String> tab;
	List<String> teams;

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		tab = new ArrayList<String>();
		teams = plugin.getConfig().getStringList("Team.TeamNames");
		// sbb
		if (command.getName().equalsIgnoreCase("sbb")) {
			if (args.length == 1 && args[0].length() == 0) {
				tab = Arrays.asList("getmeta", "maxplayers", "ready",
						"rearrange", "set", "spectators", "stop");
			} else {
				// getmeta
				if ("getmeta".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("getmeta")) {
						if (args.length == 2 && args[1].length() == 0) {
							tab.addAll(Arrays.asList("world","player"));
						} else {
							// world
							if ("world".startsWith(args[1])) {
								if (args[1].equalsIgnoreCase("world")) {
									if (args.length == 3
											&& args[2].length() == 0) {
										// tab = nothing;
										tab.addAll(teams);
										tab.add("lobby");
										Collections.sort(tab);
									}
								} else {
									tab.addAll(Arrays.asList("world"));
								}
							}
							// player
							if ("player".startsWith(args[1])) {
								if (args[1].equalsIgnoreCase("player")) {
									if (args.length == 3
											&& args[2].length() == 0) {
										return null;
									}
								} else {
									tab.addAll(Arrays.asList("player"));
								}
							}
						}
					} else {
						tab.addAll(Arrays.asList("getmeta"));
					}
				}
				// maxplayers
				if ("maxplayers".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("maxplayers")) {
						// last argument
					} else {
						tab.addAll(Arrays.asList("maxplayers"));
					}
				}
				// ready
				if ("ready".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("ready")) {
						// last argument
					} else {
						tab.addAll(Arrays.asList("ready"));
					}
				}
				// rearrange
				if ("rearrange".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("rearrange")) {
						// last argument
					} else {
						tab.addAll(Arrays.asList("rearrange"));
					}
				}
				// set
				if ("set".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("set")) {
						if (args.length == 2 && args[1].length() == 0) {
							tab.addAll(Arrays.asList("lobby", "spawn"));
						} else {
							// lobby
							if ("lobby".startsWith(args[1])) {
								if (args[1].equalsIgnoreCase("lobby")) {
									// last argument
								} else {
									tab.addAll(Arrays.asList("lobby"));
								}
							}
							// spawn
							if ("spawn".startsWith(args[1])) {
								if (args[1].equalsIgnoreCase("spawn")) {
									if (args.length == 3
											&& args[2].length() == 0) {
										// teamlist
										tab = new ArrayList<String>(teams);
									}
								} else {
									tab.addAll(Arrays.asList("spawn"));
								}
							}
						}
					} else {
						tab.addAll(Arrays.asList("set"));
					}
				}
				// spectateheight
				if ("spectateheight".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("spectateheight")) {
						// last argument
					} else {
						tab.addAll(Arrays.asList("spectateheight"));
					}
				}

				// spectators
				if ("spectators".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("spectators")) {
						if (args.length == 2 && args[1].length() == 0) {
							tab.addAll(Arrays.asList("add", "remove"));
						} else {
							// add
							if ("add".startsWith(args[1])) {
								if (args[1].equalsIgnoreCase("add")) {
									if (args.length == 3
											&& args[2].length() == 0) {
										tab = null;
									}
								} else {
									tab.addAll(Arrays.asList("add"));
								}
							}
							// remove
							if ("remove".startsWith(args[1])) {
								if (args[1].equalsIgnoreCase("remove")) {
									if (args.length == 3
											&& args[2].length() == 0) {
										tab = null;
									}
								} else {
									tab.addAll(Arrays.asList("remove"));
								}
							}
						}
					} else {
						tab.addAll(Arrays.asList("spectators"));
					}
				}
				// stop
				if ("stop".startsWith(args[0])) {
					if (args[0].equalsIgnoreCase("stop")) {
						// last argument
					} else {
						tab.addAll(Arrays.asList("stop"));
					}
				}
			}
			return tab;

		}
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}

// tab = Arrays.asList("",);
