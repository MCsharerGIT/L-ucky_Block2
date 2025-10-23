package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class MooshroomDrop extends Drop {

	private static final Integer BOWL_AMOUNT = 16;

	public MooshroomDrop() {
		super("Mooshroom", Arrays.asList("Spawns a mushroom cow and gives " + BOWL_AMOUNT + " bowls."), DropType.GOOD, 0);

	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getWorld().spawnEntity(loc, EntityType.MOOSHROOM);
		loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.BOWL, 16));
	}

}
