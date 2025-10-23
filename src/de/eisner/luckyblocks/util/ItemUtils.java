package de.eisner.luckyblocks.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

	public static ItemStack buildItem(Material material, int amount, String name, List<String> lore) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		if (lore != null) {
			meta.setLore(lore);
		}
		item.setItemMeta(meta);
		return item;
	}

	public static void giveItems(Player p, List<ItemStack> items) {
		items.forEach(item -> giveItem(p, item));
	}

	public static void giveItem(Player p, ItemStack item) {
		if (p.getInventory().firstEmpty() == -1) {
			p.getWorld().dropItem(p.getLocation(), item);
			return;
		}
		p.getInventory().addItem(item);
	}

}
