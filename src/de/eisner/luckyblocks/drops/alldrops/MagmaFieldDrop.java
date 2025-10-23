package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class MagmaFieldDrop extends Drop {

	private static final Integer MAX_RECURSION = 25;

	private static final List<Material> IGNORE = Arrays.asList(Material.AIR, Material.SHORT_GRASS, Material.TALL_GRASS, Material.SNOW, Material.POPPY, Material.DANDELION, Material.FERN);

	public MagmaFieldDrop() {
		super("Magma Field", Arrays.asList("Creates a magma field."), DropType.BAD, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		placeField(loc.subtract(0, 1, 0), 0);
	}

	private void placeField(Location loc, int r) {
		if ((r >= MAX_RECURSION) || !IGNORE.contains(loc.clone().add(0, 1, 0).getBlock().getType()) || (loc.getBlock().getType() == Material.MAGMA_BLOCK)) {
			return;
		}
		if (!IGNORE.contains(loc.getBlock().getType())) {
			loc.getBlock().setType(Material.MAGMA_BLOCK);
			Bukkit.getScheduler().runTaskLater(Main.plugin, new Runnable() {

				@Override
				public void run() {
					placeField(loc.clone().add(1, 0, 0), r + 1);
					placeField(loc.clone().add(0, 0, 1), r + 1);
					placeField(loc.clone().add(-1, 0, 0), r + 1);
					placeField(loc.clone().add(0, 0, -1), r + 1);
					placeField(loc.clone().add(1, 1, 0), r + 1);
					placeField(loc.clone().add(0, 1, 1), r + 1);
					placeField(loc.clone().add(-1, 1, 0), r + 1);
					placeField(loc.clone().add(0, 1, -1), r + 1);
					placeField(loc.clone().add(1, -1, 0), r + 1);
					placeField(loc.clone().add(0, -1, 1), r + 1);
					placeField(loc.clone().add(-1, -1, 0), r + 1);
					placeField(loc.clone().add(0, -1, -1), r + 1);
				}
			}, 5);

		}
	}

}
