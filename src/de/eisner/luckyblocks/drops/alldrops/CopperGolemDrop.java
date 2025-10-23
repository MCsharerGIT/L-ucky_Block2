package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class CopperGolemDrop extends Drop {

	public CopperGolemDrop() {
		super("Copper Golem", Arrays.asList("Builds a copper golem."), DropType.NEUTRAL, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.COPPER_BLOCK);
		loc.add(0, 1, 0).getBlock().setType(Material.COPPER_BLOCK);
		loc.add(0, 1, 0).getBlock().setType(Material.CARVED_PUMPKIN);
		loc.add(1, -1, 0).getBlock().setType(Material.COPPER_BLOCK);
		loc.add(-2, 0, 0).getBlock().setType(Material.COPPER_BLOCK);

	}

}
