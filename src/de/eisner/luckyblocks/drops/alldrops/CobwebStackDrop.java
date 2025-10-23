package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class CobwebStackDrop extends Drop {

	public CobwebStackDrop() {
		super("Cobwebs", Arrays.asList("Creates a cobweb tower to trap the player."), DropType.BAD, 0);
	}

	@Override
	public void execute(Player p, Location loc) {
		for (int y = 320; y >= loc.getBlockY(); y--) {
			new Location(loc.getWorld(), loc.getBlockX(), y, loc.getBlockZ()).getBlock().setType(Material.COBWEB);
		}
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> p.teleport(new Location(loc.getWorld(), loc.getBlockX() + 0.5, 325, loc.getBlockZ() + 0.5), TeleportCause.PLUGIN), 20);
	}

}
