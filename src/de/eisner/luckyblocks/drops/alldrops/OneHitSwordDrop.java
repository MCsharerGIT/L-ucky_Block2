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
import org.bukkit.inventory.meta.Damageable;
import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class OneHitSwordDrop extends Drop {

	private static final NamespacedKey KEY_HEALTH = new NamespacedKey(Main.plugin, "onehitsword-health");
	private static final NamespacedKey KEY_DAMAGE = new NamespacedKey(Main.plugin, "onehitsword-damage");

	public OneHitSwordDrop() {
		super("Onehit Sword", Arrays.asList("Drops one hit sword."), DropType.GOOD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack item = new ItemStack(Material.WOODEN_SWORD);
		Damageable meta = (Damageable) item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + getName());
		meta.setDamage(Material.WOODEN_SWORD.getMaxDurability() - 1);
		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(KEY_HEALTH, -19.5, Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(KEY_DAMAGE, 10000, Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
		item.setItemMeta(meta);
		loc.getWorld().dropItemNaturally(loc, item);
	}

}