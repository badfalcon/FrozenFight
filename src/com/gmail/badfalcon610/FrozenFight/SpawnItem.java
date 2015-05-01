package com.gmail.badfalcon610.FrozenFight;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpawnItem extends BukkitRunnable {
	/**
	 * 花火の形状の候補
	 */
	static private FireworkEffect.Type[] types = { FireworkEffect.Type.BALL,
			FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST,
			FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR, };
	static private Random rand = new Random();

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

		// 花火を作る
		Firework firework = location.getWorld().spawn(location, Firework.class);

		// 花火の設定情報オブジェクトを取り出す
		FireworkMeta meta = firework.getFireworkMeta();
		Builder effect = FireworkEffect.builder();

		// 形状をランダムに決める
		effect.with(types[rand.nextInt(types.length)]);

		// 基本の色を単色～5色以内でランダムに決める
		effect.withColor(getRandomCrolors(1 + rand.nextInt(5)));

		// 余韻の色を単色～3色以内でランダムに決める
		effect.withFade(getRandomCrolors(1 + rand.nextInt(3)));

		// 爆発後に点滅するかをランダムに決める
		effect.flicker(rand.nextBoolean());

		// 爆発後に尾を引くかをランダムに決める
		effect.trail(rand.nextBoolean());

		// 打ち上げ高さを1以上4以内でランダムに決める
		meta.setPower(1 + rand.nextInt(4));

		// 花火の設定情報を花火に設定
		meta.addEffect(effect.build());
		firework.setFireworkMeta(meta);

		item.setVelocity(new Vector(0, 0, 0));
	}

	/**
	 * ランダムな色の配列を作って返す
	 *
	 * @param length
	 *            配列の長さ
	 * @return ランダムな色の配列
	 */
	private Color[] getRandomCrolors(int length) {
		// 配列を作る
		Color[] colors = new Color[length];

		// 配列の要素を順に処理していく
		for (int n = 0; n != length; n++) {
			// 24ビットカラーの範囲でランダムな色を決める
			colors[n] = Color.fromBGR(rand.nextInt(1 << 24));
		}

		// 配列を返す
		return colors;
	}
}