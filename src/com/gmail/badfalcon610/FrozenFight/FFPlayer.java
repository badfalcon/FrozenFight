package com.gmail.badfalcon610.FrozenFight;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FFPlayer {

	public static void Respawn(Player p, FileConfiguration config) {
		// 無敵にして、リスポーン

		p.setNoDamageTicks(config.getInt("Game.InvincibleTime"));
		FFTeam.warpToTeamSpawn(p);
		// アイテムリセット

		ItemStack[] sb = new ItemStack[36];
		for (int i = 0; i < config.getInt("Game.SnowBallStacks"); i++) {
			sb[i] = new ItemStack(Material.SNOW_BALL, 16);
		}
		p.getInventory().clear();
		p.getInventory().setContents(sb);

		// 経験値リセット

		p.setExp(1.0f);

	}
}
