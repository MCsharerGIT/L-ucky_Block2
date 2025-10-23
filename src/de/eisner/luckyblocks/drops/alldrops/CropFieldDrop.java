package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class CropFieldDrop extends Drop {

	private static final Material[] POSSIBLE_CROPS = { Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS };

	public CropFieldDrop() {
		super("Crop Field", Arrays.asList("Builds a crop field."), DropType.NEUTRAL, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.subtract(0, 1, 0);
		buildSquare(Material.STONE_BRICKS, loc, 10);
		buildSquare(Material.FARMLAND, loc, 8);
		loc.add(0, 1, 0);
		buildSquare(POSSIBLE_CROPS[DropManager.RANDOM.nextInt(POSSIBLE_CROPS.length)], loc, 8);
		loc.getBlock().setType(Material.GLOWSTONE);
		loc.subtract(0, 1, 0).getBlock().setType(Material.WATER);

	}

	public void buildSquare(Material material, Location center, int size) {
		for (int x = center.getBlockX() - size / 2; x <= center.getBlockX() + size / 2; x++) {
			for (int z = center.getBlockZ() - size / 2; z <= center.getBlockZ() + size / 2; z++) {
				new Location(center.getWorld(), x, center.getBlockY(), z).getBlock().setType(material);
			}
		}
	}

}
