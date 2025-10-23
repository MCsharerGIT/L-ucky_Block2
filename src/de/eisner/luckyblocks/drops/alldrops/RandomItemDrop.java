package de.eisner.luckyblocks.drops.alldrops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;
import de.eisner.luckyblocks.drops.RandomAmountItem;

public class RandomItemDrop extends Drop {

	private RandomAmountItem[] items;

	public RandomItemDrop(String name, RandomAmountItem... items) {
		super(name, createDescription(items), DropType.GOOD, 0);
		this.items = items;
	}

	private static List<String> createDescription(RandomAmountItem[] items) {
		List<String> result = new ArrayList<>();
		result.add("Can drop the following items: ");
		for (RandomAmountItem item : items) {
			result.add("  - " + item.getMaterial() + ", " + (item.getAmount() == 1 ? "1x" : "1-" + item.getAmount() + "x"));
		}
		return result;
	}

	@Override
	public void execute(Player p, Location loc) {
		for (RandomAmountItem item : items) {
			loc.getWorld().dropItemNaturally(loc, item.getItemWithRandomAmount());
		}

	}

}
