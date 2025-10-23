package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class ParticleSpiral extends Drop {

	private static final Integer HEIGHT = 10;
	private static final Double RADIUS = 1.5;
	private static final Double STEP = 2.5;
	private static final Double CIRCLE = 360.;
	private static final Integer DURATION = 120;
	private static final Integer AMOUNT_OF_LINES = 8;

	private static final Particle[] PARTICLES = { Particle.DRIPPING_WATER, Particle.DRIPPING_OBSIDIAN_TEAR, Particle.DRIPPING_LAVA };

	public ParticleSpiral() {
		super("Particle Spiral", Arrays.asList("Creates a particle spiral."), DropType.NEUTRAL, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.add(0.5, 0, 0.5);
		Particle particle = PARTICLES[DropManager.RANDOM.nextInt(PARTICLES.length)];
		for (double i = 0; i <= CIRCLE; i += STEP) {
			for (int j = 1; j <= AMOUNT_OF_LINES; j++) {
				double[] data = { i, j };
				double y = loc.getY() + (i * HEIGHT / CIRCLE);
				Bukkit.getScheduler().runTaskLater(Main.plugin, () -> spawnParticleCircle(loc, particle, data[1] * CIRCLE / AMOUNT_OF_LINES + data[0], y), (long) (i * (DURATION / CIRCLE)));
			}
		}
	}

	private void spawnParticleCircle(Location loc, Particle particle, double i, double y) {
		Location target = new Location(loc.getWorld(), loc.getX() + Math.cos(Math.toRadians(i)) * RADIUS, y, loc.getZ() + Math.sin(Math.toRadians(i)) * RADIUS);
		loc.getWorld().spawnParticle(particle, target, 1);
	}

}
