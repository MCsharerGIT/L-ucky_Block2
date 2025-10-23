package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class FlowerDrop extends Drop {

	public FlowerDrop() {
		super("Flower", Arrays.asList("Places a flower."), DropType.NEUTRAL, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.GRASS_BLOCK);
		loc.clone().add(0, 1, 0).getBlock().setType(Material.POPPY);
	}

}
