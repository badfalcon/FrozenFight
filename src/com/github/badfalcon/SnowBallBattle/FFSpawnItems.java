package com.github.badfalcon.SnowBallBattle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class FFSpawnItems extends BukkitRunnable {
	private FrozenFight plugin;
	static List<BukkitTask> spawnItems;

	public FFSpawnItems(FrozenFight plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		World world = plugin.getServer().getWorlds().get(0);
		FileConfiguration config = plugin.getConfig();

		FFItem[] itemNames = FFItem.values();
		spawnItems = new ArrayList<BukkitTask>();
		for (FFItem item : itemNames) {
			String itemName = item.name();
			Boolean itemActive = config.getBoolean("Item." + itemName
					+ ".Active");
			if (itemActive) {
				int itemNum = config.getInt("Item." + itemName + ".Numbers");
				for (int i = 1; i <= itemNum; i++) {
					ItemStack itemStack = config.getItemStack("Item."
							+ itemName + ".Item");
					ItemMeta itemMeta = itemStack.getItemMeta();
					itemMeta.setDisplayName(itemName);
					itemStack.setItemMeta(itemMeta);
					Vector locationVector = config.getVector("Item." + itemName
							+ ".num" + i + ".Spawn");
					Location itemLocation = new Location(world,
							locationVector.getX(), locationVector.getY(),
							locationVector.getZ());
					int itemAppearTime = config.getInt("Item." + itemName
							+ ".num" + i + ".SpawnTime");
					spawnItems
							.add(new SpawnItem(world, itemLocation, itemStack)
									.runTaskLater(plugin,
											20 * 60 * itemAppearTime));
				}
			}
		}
	}

	public void cancel() {
		for (BukkitTask task : spawnItems) {
			Bukkit.getServer().getScheduler()
			.cancelTask(task.getTaskId());
		}
	}

	public class SpawnItem extends BukkitRunnable {

		World world;
		Location location;
		ItemStack itemstack;

		public SpawnItem(World w, Location l, ItemStack is) {
			world = w;
			location = l;
			itemstack = is;
		}

		@Override
		public void run() {
			Item item = world.dropItem(location, itemstack);
			item.setVelocity(new Vector(0, 0, 0));
		}
	}

}
