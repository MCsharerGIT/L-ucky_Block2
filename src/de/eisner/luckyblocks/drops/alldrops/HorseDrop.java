package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class HorseDrop extends Drop {

	private static final Material[] HORSE_ARMOR = { Material.LEATHER_HORSE_ARMOR, Material.GOLDEN_HORSE_ARMOR, Material.IRON_HORSE_ARMOR, Material.DIAMOND_HORSE_ARMOR };

	public HorseDrop() {
		super("Horse", Arrays.asList("Summons a equipped horse."), DropType.GOOD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		Horse horse = (Horse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);
		horse.setOwner(p);
		HorseInventory inv = horse.getInventory();
		inv.setArmor(new ItemStack(HORSE_ARMOR[DropManager.RANDOM.nextInt(HORSE_ARMOR.length)]));
		inv.setSaddle(new ItemStack(Material.SADDLE));

	}

}
