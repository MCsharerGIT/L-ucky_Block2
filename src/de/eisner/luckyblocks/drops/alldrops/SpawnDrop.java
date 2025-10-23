package de.eisner.luckyblocks.drops.alldrops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class SpawnDrop extends Drop {

	private EntityType[] entities;

	public SpawnDrop(String name, DropType type, EntityType... entities) {
		super(name, createDescription(entities), type, 0);
		this.entities = entities;
	}

	private static List<String> createDescription(EntityType[] entities) {
		List<String> result = new ArrayList<>();
		result.add("Spawns the following mobs: ");
		for (EntityType entity : entities) {
			result.add("  - " + entity);
		}
		return result;
	}

	@Override
	public void execute(Player p, Location loc) {
		for (EntityType entity : entities) {
			loc.getWorld().spawnEntity(loc, entity);
		}

	}

}
