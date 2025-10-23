package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class PrisonDrop extends Drop {

	private static final Integer SIZE = 3;

	public PrisonDrop() {
		super("Prison", Arrays.asList("Builds an obsidian prison."), DropType.BAD, 1);

	}

	@Override
	public void execute(Player p, Location loc) {
		placeCube(p.getLocation(), SIZE, Material.CRYING_OBSIDIAN);
		placeCube(p.getLocation(), SIZE - 1, Material.AIR);
		p.getLocation().getBlock().setType(Material.LANTERN);

	}

	private static void placeCube(Location center, int size, Material material) {
		for (int x = center.getBlockX() - size; x <= center.getBlockX() + size; x++) {
			for (int y = center.getBlockY() - size; y <= center.getBlockY() + size; y++) {
				for (int z = center.getBlockZ() - size; z <= center.getBlockZ() + size; z++) {
					Location setBlock = new Location(center.getWorld(), x, y, z);
					setBlock.getBlock().setType(material);
				}
			}
		}
	}

}
