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

public class ClimberLeggings extends Drop {

	private static final NamespacedKey KEY = new NamespacedKey(Main.plugin, "climberleggings");

	public ClimberLeggings() {
		super("Climbers Leggings", Arrays.asList("Leggings that let you step higher."), DropType.GOOD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack item = new ItemStack(Material.DIAMOND_LEGGINGS);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + getName());
		meta.addAttributeModifier(Attribute.GENERIC_STEP_HEIGHT, new AttributeModifier(KEY, 2, Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS));
		item.setItemMeta(meta);
		loc.getWorld().dropItemNaturally(loc, item);
	}
}
