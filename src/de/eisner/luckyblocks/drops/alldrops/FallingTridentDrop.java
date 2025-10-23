package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class FallingTridentDrop extends Drop {

	public FallingTridentDrop() {
		super("Falling Trident", Arrays.asList("Spawns a falling trident"), DropType.BAD, 15);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.OAK_SIGN);
		Sign sign = (Sign) loc.getBlock().getState();
		sign.getTargetSide(p).setLine(1, "look up.");
		sign.update();
		Location target = p.getLocation().add(0, 50, 0);
		Trident trident = (Trident) target.getWorld().spawnEntity(target, EntityType.TRIDENT);
		trident.setPickupStatus(PickupStatus.DISALLOWED);

	}

}
