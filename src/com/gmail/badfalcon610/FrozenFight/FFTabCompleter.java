package com.gmail.badfalcon610.FrozenFight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class FFTabCompleter implements TabCompleter {

	FrozenFight plugin;

	public FFTabCompleter(FrozenFight plugin) {
		this.plugin = plugin;
	}

	String[] commands;
	List<String> tab;

	public List<String> getTeams() {
		return plugin.getConfig().getStringList("Team.Names");
	}

	public List<String> getItems() {
		FFItem[] snowItem = FFItem.values();
		List<String> list = new ArrayList<String>();
		for (FFItem itemName : snowItem) {
			list.add(itemName.name());
		}
		return list;
	}

	public List<String> getColors() {
		ChatColor[] chatColor = ChatColor.values();
		List<String> list = new ArrayList<String>();
		for (ChatColor colorName : chatColor) {
			list.add(colorName.name());
		}
		return list;
	}

	public List<String> getArmors() {
		List<String> list = Arrays.asList("LEATHER", "CHAINMAIL", "IRON",
				"GOLD", "DIAMOND");
		return list;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		tab = new ArrayList<String>();

		// ff

		if (command.getName().equalsIgnoreCase("ff")) {
			if (args.length == 0 || args[0].length() == 0) {
				tab = Arrays.asList("getmeta", "item", "ready", "set", "spect",
						"stop", "teams");
			}

			// help
			else if ("help".startsWith(args[0])) {
				if (args[0].equalsIgnoreCase("help")) {
					return tab;
				} else {
					tab.addAll(Arrays.asList("help"));
				}
			}

			else if ("item".startsWith(args[0])) {
				if (args[0].equalsIgnoreCase("item")) {
					if (args.length == 1) {
						return tab;
					}
					if (args[1].length() == 0) {
						tab.addAll(Arrays.asList("add", "list", "rem", "type",
								"toggle"));
					}

					// add

					else if ("add".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("add")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								List<String> items = getItems();
								tab.addAll(items);
							}
						} else {
							tab.addAll(Arrays.asList("add"));
						}
					}

					// dur

					else if ("dur".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("dur")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								List<String> items = getItems();
								tab.addAll(items);
							}
						} else {
							tab.addAll(Arrays.asList("dur"));
						}
					}

					// toggle

					else if ("toggle".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("toggle")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								List<String> items = getItems();
								tab.addAll(items);
							}
						} else {
							tab.addAll(Arrays.asList("toggle"));
						}

					}
					// rem
					else if ("rem".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("rem")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								List<String> items = getItems();
								tab.addAll(items);
							}
						} else {
							tab.addAll(Arrays.asList("rem"));
						}
					}
					// type
					else if ("type".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("type")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								List<String> items = getItems();
								tab.addAll(items);
							}
						} else {
							tab.addAll(Arrays.asList("type"));
						}
					}
					// list
					else if ("list".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("list")) {
							return tab;
						} else {
							tab.addAll(Arrays.asList("list"));
						}
					}
				} else {
					tab.addAll(Arrays.asList("item"));
				}
			}

			// ready
			else if ("ready".startsWith(args[0])) {
				if (args[0].equalsIgnoreCase("ready")) {
					return tab;
				} else {
					tab.addAll(Arrays.asList("ready"));
				}
			}

			// set
			else if ("set".startsWith(args[0])) {
				if (args[0].equalsIgnoreCase("set")) {
					if (args.length == 1) {
						return tab;
					}
					if (args[1].length() == 0) {
						tab.addAll(Arrays.asList("lobby", "spawn"));
					}
					// lobby
					else if ("lobby".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("lobby")) {
							return tab;
						} else {
							tab.addAll(Arrays.asList("lobby"));
						}
					}
					// spawn
					else if ("spawn".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("spawn")) {
							if (args.length == 2) {
								return tab;
							}
							List<String> teams = getTeams();
							if (args[2].length() == 0) {
								// teamlist
								tab = new ArrayList<String>(teams);
							} else {
								for (String teamName : teams) {
									if (teamName.startsWith(args[2])) {
										if (args[2].equalsIgnoreCase(teamName)) {
											return tab;
										} else {
											tab.addAll(Arrays.asList(teamName));
										}
									}
								}
							}
						} else {
							tab.addAll(Arrays.asList("spawn"));
						}
					}

				} else {
					tab.addAll(Arrays.asList("set"));
				}
			}

			// spect
			else if ("spect".startsWith(args[0])) {
				if (args[0].equalsIgnoreCase("spect")) {
					if (args.length == 1) {
						return tab;
					}
					if (args[1].length() == 0) {
						tab.addAll(Arrays.asList("add", "remove"));
					}
					// add
					else if ("add".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("add")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								return null;
							}
						} else {
							tab.addAll(Arrays.asList("add"));
						}
					}
					// remove
					else if ("remove".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("remove")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								return null;
							}
						} else {
							tab.addAll(Arrays.asList("remove"));
						}
					}
				} else {
					tab.addAll(Arrays.asList("spect"));
				}
			}
			// stop
			else if ("stop".startsWith(args[0])) {
				if (args[0].equalsIgnoreCase("stop")) {
					return tab;
				} else {
					tab.addAll(Arrays.asList("stop"));
				}
			}
			// teams
			else if ("teams".startsWith(args[0])) {
				if (args[0].equalsIgnoreCase("teams")) {
					if (args.length == 1) {
						return tab;
					}
					if (args[1].length() == 0) {
						tab.addAll(Arrays.asList("add", "list", "remove"));
					}
					// add
					else if ("add".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("add")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {

								// チーム名なのでsuggestはなし

							} else {
								if (args.length == 3) {
									return tab;
								}
								List<String> colorList = getColors();
								if (args[3].length() == 0) {
									tab.addAll(colorList);
								} else {
									for (String color : colorList) {
										if (color.startsWith(args[3])) {
											if (args[3].equalsIgnoreCase(color)) {
												if (args.length == 4) {
													return tab;
												}
												List<String> armorList = getArmors();
												if (args[4].length() == 0) {
													tab.addAll(armorList);
												} else {
													for (String armor : armorList) {
														if (armor
																.startsWith(args[4])) {
															if (args[4]
																	.equalsIgnoreCase(armor)) {

																return tab;

															} else {
																tab.addAll(Arrays
																		.asList(armor));
															}
														}
													}
												}
											} else {
												tab.addAll(Arrays.asList(color));
												break;
											}
										}
									}
								}
							}
						} else {
							tab.addAll(Arrays.asList("add"));
						}
					}

					// list
					if ("list".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("list")) {
							return tab;
						}
					}

					// remove
					if ("remove".startsWith(args[1])) {
						if (args[1].equalsIgnoreCase("remove")) {
							if (args.length == 2) {
								return tab;
							}
							if (args[2].length() == 0) {
								// teamlist
								List<String> teams = getTeams();
								tab = new ArrayList<String>(teams);
							}
						} else {
							tab.addAll(Arrays.asList("remove"));
						}
					}

				} else {
					tab.addAll(Arrays.asList("teams"));
				}
			}

			return tab;

		} else {
			tab.addAll(Arrays.asList("ff"));
			return tab;
		}
		// TODO 自動生成されたメソッド・スタブ
	}
}

// tab = Arrays.asList("",);
