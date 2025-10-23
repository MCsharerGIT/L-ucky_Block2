package de.eisner.luckyblocks.drops.alldrops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class ItemDrop extends Drop {

	private Material[] materials;

	public ItemDrop(String name, DropType type, Material... materials) {
		super(name, createDescription(materials), type, 0);
		this.materials = materials;
	}

	private static List<String> createDescription(Material[] materials) {
		List<String> result = new ArrayList<>();
		result.add("Will drop the following items: ");
		for (Material material : materials) {
			result.add("  - " + material);
		}
		return result;
	}

	@Override
	public void execute(Player p, Location loc) {
		for (Material material : materials) {
			loc.getWorld().dropItemNaturally(loc, new ItemStack(material));
		}

	}

}
