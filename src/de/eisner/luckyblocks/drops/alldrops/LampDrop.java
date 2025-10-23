package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class LampDrop extends Drop {

	public LampDrop() {
		super("Lamp", Arrays.asList("Builds a lamp."), DropType.NEUTRAL, 5);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.REDSTONE_LAMP);
		loc.add(0, 1, 0);
		Bukkit.getScheduler().runTaskLater(Main.plugin, new Runnable() {

			@Override
			public void run() {
				loc.getBlock().setType(Material.LEVER);
				FaceAttachable bd = (FaceAttachable) loc.getBlock().getBlockData();
				bd.setAttachedFace(AttachedFace.FLOOR);
				loc.getBlock().setBlockData(bd);
			}
		}, 25);

		Bukkit.getScheduler().runTaskLater(Main.plugin, new Runnable() {

			@Override
			public void run() {
				if(loc.getBlock().getType() != Material.LEVER) {
					return;
				}
				Powerable pbd = (Powerable) loc.getBlock().getBlockData();
				pbd.setPowered(true);
				loc.getBlock().setBlockData(pbd);
			}
		}, 45);

	}

}
