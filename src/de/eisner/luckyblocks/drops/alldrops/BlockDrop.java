package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class BlockDrop extends Drop {

	private Material material;

	public BlockDrop(String name, Material material, DropType type) {
		super(name, Arrays.asList("Replaces the luckyblock with " + material.toString() + "."), type, 1);
		this.material = material;
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(material);
	}

}
