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

public class SpeedSword extends Drop {

	private static final NamespacedKey KEY = new NamespacedKey(Main.plugin, "speedsword");

	public SpeedSword() {
		super("Speed Sword", Arrays.asList("A sword that lets you walk faster."), DropType.GOOD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + getName());
		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(KEY, 0.125, Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
		item.setItemMeta(meta);
		loc.getWorld().dropItemNaturally(loc, item);
	}

}
