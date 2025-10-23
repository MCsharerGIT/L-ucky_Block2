package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class FunnySheepDrop extends Drop {

	public FunnySheepDrop() {
		super("Rainbow Sheep", Arrays.asList("Summons a sheep named 'jeb_'."), DropType.NEUTRAL, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		Sheep sheep = (Sheep) loc.getWorld().spawnEntity(loc, EntityType.SHEEP);
		sheep.setCustomName("jeb_");
	}

}
