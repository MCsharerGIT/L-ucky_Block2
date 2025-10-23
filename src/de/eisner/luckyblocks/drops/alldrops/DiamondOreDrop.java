package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class DiamondOreDrop extends Drop {

	public DiamondOreDrop() {
		super("Diamond Ore", Arrays.asList("Places a diamand ore", "... and gives a wooden pick axe."), DropType.GOOD, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.DIAMOND_ORE);
		loc.getWorld().dropItem(loc.add(0, 1, 0), new ItemStack(Material.WOODEN_PICKAXE));
		
	}

}
