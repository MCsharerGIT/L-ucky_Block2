package de.eisner.luckyblocks.drops.alldrops;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

public class DoorTrapDrop extends Drop {

	public DoorTrapDrop() {
		super("Door Trap", Arrays.asList("Traps player in an old style door trap."), DropType.BAD, 1);
	}

	@Override
	public void execute(Player p, Location loc) {
		Location target = new Location(loc.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
		target.subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);
		target.add(0, 1, 0).getBlock().setType(Material.STONE_PRESSURE_PLATE);
		setDoor(target.clone().add(1, 0, 0), BlockFace.NORTH);
		setDoor(target.clone().add(-1, 0, 0), BlockFace.SOUTH);
		setDoor(target.clone().add(0, 0, 1), BlockFace.EAST);
		setDoor(target.clone().add(0, 0, -1), BlockFace.WEST);		
		target.add(0, 2, 0).getBlock().setType(Material.BEDROCK);
	}

	private void setDoor(Location loc, BlockFace face) {
		Block bottom = loc.getBlock();
		Block top = loc.getBlock().getRelative(BlockFace.UP);
		bottom.setType(Material.IRON_DOOR, false);
		top.setType(Material.IRON_DOOR, false);
		Door lower = (Door) bottom.getBlockData();
		Door upper = (Door) top.getBlockData();
		lower.setHalf(Half.BOTTOM);
		upper.setHalf(Half.TOP);
		upper.setFacing(face);
		bottom.setBlockData(lower);
		top.setBlockData(upper);

	}

}
