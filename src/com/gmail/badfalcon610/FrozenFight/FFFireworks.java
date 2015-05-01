package com.gmail.badfalcon610.FrozenFight;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class FFFireworks extends BukkitRunnable {

	/**
	 * 花火の形状の候補
	 */
	static private FireworkEffect.Type[] types = { FireworkEffect.Type.BALL,
			FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST,
			FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR, };

	/**
	 * 乱数ジェネレーター
	 */
	static private Random rand = new Random();

	/**
	 * 発射位置
	 */
	private Player player;

	private int times;

	/**
	 * コンストラクタ
	 *
	 * @param player
	 *            矢の発射の中心地点となるプレイヤー
	 */
	public FFFireworks(Player p, int times) {

		player = p;
		this.times = times;
	}

	/**
	 * 打ち上げ処理
	 */
	public void run() {
		Location loc = player.getLocation();

		// 花火を作る
		Firework firework = loc.getWorld().spawn(loc, Firework.class);

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

		times--;
		if (times <= 0) {
			cancel();
		}
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
