package com.gmail.badfalcon610.FrozenFight;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class FFHitParticle extends BukkitRunnable {

	private Location loc;
	private World world;
	private int times;

	public FFHitParticle(Location loc, int times) {
		this.loc = loc;
		this.world = loc.getWorld();
		this.times = times;
	}

	@Override
	public void run() {
		world.playEffect(loc, Effect.STEP_SOUND, 78);
		world.createExplosion(loc, 0, false);
		times--;
		if (times <= 0) {
			cancel();
		}
	}

}
