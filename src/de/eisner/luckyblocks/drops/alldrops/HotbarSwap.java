package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class HotbarSwap extends Drop {

	private static final Integer SWAPS = 18;

	public HotbarSwap() {
		super("HotbarSwap", Arrays.asList("Swaps the hotbar randomly."), DropType.BAD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack[] items = p.getInventory().getContents();
		for (int i = 0; i < SWAPS; i++) {
			int first = DropManager.RANDOM.nextInt(9);
			int second = DropManager.RANDOM.nextInt(9);
			ItemStack temp = items[first];
			items[first] = items[second];
			items[second] = temp;
		}
		p.getInventory().setContents(items);

	}

}
