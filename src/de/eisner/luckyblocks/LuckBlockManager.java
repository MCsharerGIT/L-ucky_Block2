package de.eisner.luckyblocks;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.util.ColorUtil;

public class LuckBlockManager {

	private static final HashSet<Location> LOCATIONS = new HashSet<>();
	public static final Material BLOCK_TYPE = Material.SPONGE;
	public static final String ITEM_NAME = ColorUtil.createTextWithFade("Luckyblock", 212, 108, 11, 0, 10, 0);
	public static final NamespacedKey ITEM_KEY = new NamespacedKey(Main.plugin, "luckyblock");

	public static boolean isLuckyBlock(Location loc) {
		return loc.getBlock().getType() == BLOCK_TYPE && LOCATIONS.contains(loc);
	}

	public static boolean isLuckyBlock(ItemStack item) {
		return item.getType() == BLOCK_TYPE && item.getItemMeta().getPersistentDataContainer().has(ITEM_KEY);
	}

	public static ItemStack getLuckyBlock(int amount) {
		ItemStack item = new ItemStack(BLOCK_TYPE, amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ITEM_NAME);
		meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BOOLEAN, true);
		item.setItemMeta(meta);
		return item;
	}

	public static void registerLuckyBlockPlace(Location loc) {
		LOCATIONS.add(loc);

	}

	public static void registerLuckyBlockBreak(Location loc, Player p) {
		LOCATIONS.remove(loc);
		Drop drop = DropManager.getRandomDrop();
		if (drop.getDelay() == 0) {
			drop.execute(p, loc);
			return;
		}
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> drop.execute(p, loc), drop.getDelay());

	}

}
