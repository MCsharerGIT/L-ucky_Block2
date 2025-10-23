package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.drops.DropType;

public class MusicDrop extends Drop {

	private static final Material[] POSSIBLE_DISCS = { Material.MUSIC_DISC_13, Material.MUSIC_DISC_5, Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_BLOCKS, Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_CREATOR,
			Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI, Material.MUSIC_DISC_OTHERSIDE, Material.MUSIC_DISC_PIGSTEP, Material.MUSIC_DISC_PRECIPICE, Material.MUSIC_DISC_RELIC,
			Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_WARD, Material.MUSIC_DISC_STAL };

	public MusicDrop() {
		super("Music", Arrays.asList("Places a jukebox"), DropType.NEUTRAL, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.JUKEBOX);
		loc.getWorld().dropItemNaturally(loc, new ItemStack(POSSIBLE_DISCS[DropManager.RANDOM.nextInt(POSSIBLE_DISCS.length)]));

	}

}
