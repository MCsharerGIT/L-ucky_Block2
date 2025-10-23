package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class TreeDrop extends Drop {

	public static final Integer MAX_TRIES = TreeType.values().length * 2;

	public TreeDrop() {
		super("Tree", Arrays.asList("Generates a tree."), DropType.NEUTRAL, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		generateRandomTree(loc, 0);
	}

	public void generateRandomTree(Location loc, int tries) {
		if (tries < MAX_TRIES) {
			if (!loc.getWorld().generateTree(loc, DropManager.RANDOM, TreeType.values()[DropManager.RANDOM.nextInt(TreeType.values().length)])) {
				generateRandomTree(loc, tries + 1);
			}
			return;
		}
		loc.getBlock().setType(Material.OAK_LOG);
		loc.add(0, 1, 0).getBlock().setType(Material.OAK_LEAVES);
	}

}
