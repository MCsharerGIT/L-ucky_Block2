package de.eisner.luckyblocks.drops.alldrops;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class RandomPotionDrop extends Drop {

	private static final Integer LENGTH_IN_SECONDS = 120;
	private static final Integer AMPLIFIER = 0;

	private PotionEffectType[] effects;

	public RandomPotionDrop(String name, DropType type, PotionEffectType... effects) {
		super(name, createDescription(effects), type, 0);
		this.effects = effects;
	}

	private static List<String> createDescription(PotionEffectType[] effects) {
		List<String> result = new ArrayList<>();
		result.add("Gives a potion effect that lasts " + LENGTH_IN_SECONDS + "s at level " + AMPLIFIER + ".");
		result.add("Possible potion effects: ");
		for (PotionEffectType effect : effects) {
			result.add("  - " + effect.getKey().getKey());
		}
		return result;
	}

	@Override
	public void execute(Player p, Location loc) {
		PotionEffect effect = new PotionEffect(effects[DropManager.RANDOM.nextInt(effects.length)], 20 * LENGTH_IN_SECONDS, AMPLIFIER);
		p.addPotionEffect(effect);
	}
}
