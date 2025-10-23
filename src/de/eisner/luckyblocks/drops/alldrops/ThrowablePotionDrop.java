package de.eisner.luckyblocks.drops.alldrops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class ThrowablePotionDrop extends Drop {

	private List<PotionEffect> effects;

	public ThrowablePotionDrop(String name, List<PotionEffect> effects) {
		super(name, createDiscription(effects), DropType.GOOD, 0);
		this.effects = effects;
	}

	private static List<String> createDiscription(List<PotionEffect> effects) {
		List<String> result = new ArrayList<>();
		result.add("Drops a throwable potion with: ");
		for (PotionEffect effect : effects) {
			result.add("  - " + effect.getType().getKey().getKey() + ", lasting " + (effect.getDuration() / 20) + "s at level " + effect.getAmplifier() + ".");
		}
		return result;
	}

	@Override
	public void execute(Player p, Location loc) {
		ItemStack item = new ItemStack(Material.SPLASH_POTION);
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		this.effects.forEach(effect -> meta.addCustomEffect(effect, true));
		meta.setDisplayName("§6" + getName());
		item.setItemMeta(meta);
		loc.getWorld().dropItemNaturally(loc, item);
	}

}
