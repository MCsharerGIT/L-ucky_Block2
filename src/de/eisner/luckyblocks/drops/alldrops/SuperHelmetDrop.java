package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class SuperHelmetDrop extends Drop {

	public SuperHelmetDrop() {
		super("Superhelmet", Arrays.asList("Drops helmet with good helmet enchantments."), DropType.GOOD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.AQUA_AFFINITY, 10, true);
		meta.addEnchant(Enchantment.RESPIRATION, 10, true);
		meta.setUnbreakable(true);
		meta.setDisplayName(ChatColor.GOLD + getName());
		item.setItemMeta(meta);
		loc.getWorld().dropItemNaturally(loc, item);
	}

}
