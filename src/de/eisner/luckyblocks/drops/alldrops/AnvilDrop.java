package de.eisner.luckyblocks.drops.alldrops;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;

import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AnvilDrop extends Drop {

    public AnvilDrop() {
        super(
                "AnvilDrop",
                Arrays.asList("Traps you in an iron cage and warns you to look up."),
                DropType.BAD,
                1L
        );
    }

    @Override
    public void execute(Player p, Location loc) {
        final World world = loc.getWorld();
        if (world == null) return;

        final Plugin plugin = Bukkit.getPluginManager().getPlugin("LuckyBlocks");
        if (plugin == null) return;

        // Base coords
        final int baseX = loc.getBlockX();
        final int baseY = Math.max(world.getMinHeight() + 5, loc.getBlockY());
        final int baseZ = loc.getBlockZ();

        // Center the player inside the cage footprint
        final Location center = new Location(world, baseX + 0.5, baseY, baseZ + 0.5, p.getLocation().getYaw(), 0f);
        p.teleport(center);

        // 1) Platform: 5x5 Stone Bricks one block below player feet
        final int platformY = baseY - 1;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(baseX + dx, platformY, baseZ + dz).setType(Material.STONE_BRICKS, true);
            }
        }

        // 2) Iron Bars cage: inner 3x3, height 3, with a roof BUT leave roof hole at (0,0)
        final int cageH = 3;

        // Clear interior first (no physics needed)
        for (int dy = 0; dy < cageH; dy++) {
            int y = baseY + dy;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) != 1 && Math.abs(dz) != 1) {
                        world.getBlockAt(baseX + dx, y, baseZ + dz).setType(Material.AIR, false);
                    }
                }
            }
        }

        // Walls (use physics=true so bars connect)
        for (int dy = 0; dy < cageH; dy++) {
            int y = baseY + dy;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean wall = (Math.abs(dx) == 1 || Math.abs(dz) == 1);
                    if (wall) {
                        world.getBlockAt(baseX + dx, y, baseZ + dz).setType(Material.IRON_BARS, true);
                    }
                }
            }
        }

        // Roof (iron bars) at y = baseY + cageH, but skip center (0,0) above player
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // leave hole directly above player
                world.getBlockAt(baseX + dx, baseY + cageH, baseZ + dz).setType(Material.IRON_BARS, true);
            }
        }

        // 3) Standing sign on the ground, 2 blocks in front of player, facing the player
        final BlockFace face = yawToCardinal(p.getLocation().getYaw()); // where player is facing
        final int sx = baseX + face.getModX() * 2;
        final int sy = baseY; // "on the ground" at the player's foot level (supported by platform below)
        final int sz = baseZ + face.getModZ() * 2;

        final Block signBlock = world.getBlockAt(sx, sy, sz);
        if (!world.getBlockAt(sx, sy - 1, sz).getType().isSolid()) {
            world.getBlockAt(sx, sy - 1, sz).setType(Material.STONE_BRICKS, true);
        }

        signBlock.setType(Material.OAK_SIGN, true); // standing sign

        BlockData signData = signBlock.getBlockData();
        // Set rotation to face the player (text towards player)
        BlockFace signFacing = face.getOppositeFace();
        if (signData instanceof Rotatable rot) {
            rot.setRotation(signFacing);
            signBlock.setBlockData(rot, true);
        }

        // Write text (legacy setter, avoids Adventure)
        if (signBlock.getState() instanceof Sign s) {
            @SuppressWarnings("deprecation")
            Sign legacy = s;
            legacy.setLine(0, "§lLOOK UP");
            legacy.setLine(1, "§7(seriously)");
            legacy.update(true, false);
        }

        world.playSound(center, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 0.7f, 0.9f);
        world.spawnParticle(Particle.SMOKE, center, 10, 0.6, 0.5, 0.6, 0.02);

        // 4) Drop a column of anvils from high above, immediately (no delay)
        int topY   = Math.min(world.getMaxHeight() - 3, baseY + 58); // higher than before
        int startY = Math.max(baseY + 47, platformY + 6);

        for (int y = startY; y <= topY; y += 3) {
            // ensure a clear path at and below the spawn position
            world.getBlockAt(baseX, y,     baseZ).setType(Material.AIR, true);
            world.getBlockAt(baseX, y - 1, baseZ).setType(Material.AIR, true);

            world.getBlockAt(baseX, y, baseZ).setType(Material.ANVIL, true);
        }

        world.playSound(center, Sound.BLOCK_ANVIL_PLACE, 0.9f, 0.8f);
        world.spawnParticle(Particle.SMOKE, center.clone().add(0, 1.2, 0), 12, 0.5, 0.3, 0.5, 0.01);

    }

    /** Map yaw to the nearest cardinal BlockFace (N/E/S/W). */
    private static BlockFace yawToCardinal(float yaw) {
        float rot = (yaw % 360 + 360) % 360;
        if (rot >= 45 && rot < 135)  return BlockFace.WEST;   // 90°
        if (rot >= 135 && rot < 225) return BlockFace.NORTH;  // 180°
        if (rot >= 225 && rot < 315) return BlockFace.EAST;   // 270°
        return BlockFace.SOUTH;                                // 0° / 360°
    }
}
