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

public class KnockbackStickDrop extends Drop {

	private static final Integer LEVEL = 10;

	public KnockbackStickDrop() {
		super("Knockback Stick", Arrays.asList("Drops a stick with Knockback " + LEVEL), DropType.GOOD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack item = new ItemStack(Material.STICK);
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.KNOCKBACK, LEVEL, true);
		meta.setDisplayName(ChatColor.GOLD + getName());
		item.setItemMeta(meta);
		loc.getWorld().dropItemNaturally(loc, item);

	}

}
