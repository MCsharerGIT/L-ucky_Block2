package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class ChairDrop extends Drop {

	public ChairDrop() {
		super("Chair", Arrays.asList("Places a chair."), DropType.NEUTRAL, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		loc.getBlock().setType(Material.OAK_STAIRS);
		loc.add(1, 0, 0).getBlock().setType(Material.OAK_WALL_SIGN);
		WallSign right = (WallSign) loc.getBlock().getBlockData();
		right.setFacing(BlockFace.EAST);
		loc.getBlock().setBlockData(right);
		loc.add(-2, 0, 0).getBlock().setType(Material.OAK_WALL_SIGN);
		WallSign left = (WallSign) loc.getBlock().getBlockData();
		left.setFacing(BlockFace.WEST);
		loc.getBlock().setBlockData(left);

	}

}
