package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class ParticleSphere extends Drop {

	private static final Double RADIUS = 4.5;
	private static final Double STEP = 2.5;
	private static final Double CIRCLE = 360.;

	private static final Particle[] PARTICLES = { Particle.PORTAL, Particle.TOTEM_OF_UNDYING, Particle.FLASH, Particle.DRIPPING_HONEY, Particle.FALLING_SPORE_BLOSSOM, Particle.GLOW, Particle.CHERRY_LEAVES,
			Particle.OMINOUS_SPAWNING };

	public ParticleSphere() {
		super("Particle Sphere", Arrays.asList("Creates a particle sphere."), DropType.NEUTRAL, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.add(0.5, 5, 0.5);
		Particle particle = PARTICLES[DropManager.RANDOM.nextInt(PARTICLES.length)];
		for (double phi = 0; phi <= CIRCLE; phi += STEP) {
			for (double theta = 0; theta <= CIRCLE; theta += STEP) {
				double x = loc.getX() + RADIUS * Math.cos(Math.toRadians(theta)) * Math.sin(Math.toRadians(phi));
				double y = loc.getY() + RADIUS * Math.cos(Math.toRadians(phi));
				double z = loc.getZ() + RADIUS * Math.sin(Math.toRadians(theta)) * Math.sin(Math.toRadians(phi));
				loc.getWorld().spawnParticle(particle, new Location(loc.getWorld(), x, y, z), 1);
			}
		}
	}

}
