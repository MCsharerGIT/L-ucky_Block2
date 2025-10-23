package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class HeartSword extends Drop {

	private static final NamespacedKey KEY = new NamespacedKey(Main.plugin, "heartsword");

	public HeartSword() {
		super("Heart Sword", Arrays.asList("A sword that gives you hearts."), DropType.GOOD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack item = new ItemStack(Material.IRON_SWORD);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + getName());
		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(KEY, 10, Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		loc.getWorld().dropItemNaturally(loc, item);
	}

}
