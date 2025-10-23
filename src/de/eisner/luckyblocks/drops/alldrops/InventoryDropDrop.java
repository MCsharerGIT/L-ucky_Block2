package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class InventoryDropDrop extends Drop {

	public InventoryDropDrop() {
		super("Inventory Drop", Arrays.asList("Drops the entire inventory of the player."), DropType.BAD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack[] items = p.getInventory().getContents();
		p.getInventory().clear();
		loc.add(0, 2, 0);
		int n = 0;
		for (int i = 0; i < items.length; i++) {
			int[] temp = { i };
			if (items[temp[0]] == null) {
				continue;
			}
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> loc.getWorld().dropItemNaturally(loc, items[temp[0]]), n * 5);
			n++;
		}

	}

}
