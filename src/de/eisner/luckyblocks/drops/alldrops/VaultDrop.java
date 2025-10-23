package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class VaultDrop extends Drop {

	public VaultDrop() {
		super("Vault", Arrays.asList("Places a vault and gives a key."), DropType.GOOD, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.VAULT);
		loc.getWorld().dropItemNaturally(loc.add(0, 1, 0), new ItemStack(Material.TRIAL_KEY));
	}

}
