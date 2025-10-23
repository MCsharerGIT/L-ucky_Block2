package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class TNTTrapDrop extends Drop {

	public TNTTrapDrop() {
		super("TNT Trap", Arrays.asList("Builds a very simple tnt trap."), DropType.NEUTRAL, 1);
	}

	@Override
	public void execute(Player p, Location loc) {

		loc.subtract(0, 2, 0).getBlock().setType(Material.TNT);
		loc.add(0, 1, 0).getBlock().setType(Material.GRAVEL);
		loc.add(0, 1, 0).getBlock().setType(Material.STONE_PRESSURE_PLATE);

	}

}
