package de.eisner.luckyblocks.drops.alldrops;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.block.Block;


public class NetherworldDrop extends Drop {

   private static final HashMap<Material, Material> transformMap = new HashMap<>();

   public NetherworldDrop() {
      super("Netherworld", Arrays.asList("Replaces the world with the nether in a certain radius"), DropType.BAD, 1L);
   }

   @Override
   public void execute(Player p, Location loc) {
      final World world = loc.getWorld();
      if (world == null) return;

      final int maxRadius     = 20;
      final long delayPerRing = 15L;   // ticks
      final int verticalRange = 20;

      final de.eisner.luckyblocks.util.NetherTransformRegistry.Theme theme =
              de.eisner.luckyblocks.util.NetherTransformRegistry.chooseThemeAt(loc);
      final Biome targetBiome = biomeForTheme(theme);

      final int baseX = loc.getBlockX();
      final int baseY = loc.getBlockY();
      final int baseZ = loc.getBlockZ();

      final int minY = Math.max(world.getMinHeight(), baseY - verticalRange);
      final int maxY = Math.min(world.getMaxHeight() - 1, baseY + verticalRange);

      final org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("LuckyBlocks");
      if (plugin == null) return; // avoid NPE if name mismatch

      // 0) Center column first (and set biome for the whole column)
      Bukkit.getScheduler().runTask(plugin, () -> {
         // Set biome for the entire center column once
         for (int y = minY; y <= maxY; y++) {
            world.setBiome(baseX, y, baseZ, targetBiome);
         }

         // Replace blocks + fx
         for (int y = minY; y <= maxY; y++) {
            Block b = world.getBlockAt(baseX, y, baseZ);
            if (!b.getType().isAir()) {
               de.eisner.luckyblocks.util.NetherTransformRegistry.dryIfWaterlogged(b);
               Material replacement = de.eisner.luckyblocks.util.NetherTransformRegistry.getReplacement(b, theme);
               if (b.getType() != replacement) b.setType(replacement, false);

               world.spawnParticle(Particle.SMOKE, baseX + 0.5, y + 0.5, baseZ + 0.5, 2, 0.25, 0.25, 0.25, 0.01);
               world.spawnParticle(Particle.FLAME, baseX + 0.5, y + 0.5, baseZ + 0.5, 1, 0.15, 0.15, 0.15, 0.01);
            }
         }

         // Refresh the chunk so biome visuals update immediately
         world.refreshChunk(baseX >> 4, baseZ >> 4);

         world.playSound(loc, Sound.BLOCK_NETHERRACK_PLACE, 0.6f, 1.0f);
         world.playSound(loc, Sound.BLOCK_LAVA_POP,        0.6f, 0.9f);
         world.playSound(loc, Sound.BLOCK_PORTAL_AMBIENT,  0.2f, 0.8f);
      });

      // 1..R) True integer rings: (r-1)^2 < dx^2+dz^2 <= r^2
      for (int r = 1; r <= maxRadius; r++) {
         final int ring = r;
         final int r2   = ring * ring;
         final int r1   = (ring - 1) * (ring - 1);

         Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // track chunks touched this ring so we only refresh each once
            final java.util.HashSet<Long> chunksToRefresh = new java.util.HashSet<>();

            for (int y = minY; y <= maxY; y++) {
               for (int dx = -ring; dx <= ring; dx++) {
                  int maxDz = (int) Math.floor(Math.sqrt(r2 - dx * dx));
                  for (int dz = -maxDz; dz <= maxDz; dz++) {
                     int dist2 = dx * dx + dz * dz;
                     if (dist2 <= r2 && dist2 > r1) { // ring shell only
                        int x = baseX + dx;
                        int z = baseZ + dz;

                        // Set biome for this column exactly once (when y==minY)
                        if (y == minY) {
                           for (int yy = minY; yy <= maxY; yy++) {
                              world.setBiome(x, yy, z, targetBiome);
                           }
                           int cx = x >> 4, cz = z >> 4;
                           chunksToRefresh.add(chunkKey(cx, cz));
                        }

                        Block b = world.getBlockAt(x, y, z);
                        if (!b.getType().isAir()) {
                           de.eisner.luckyblocks.util.NetherTransformRegistry.dryIfWaterlogged(b);
                           Material replacement = de.eisner.luckyblocks.util.NetherTransformRegistry.getReplacement(b, theme);
                           if (b.getType() != replacement) b.setType(replacement, false);

                           world.spawnParticle(Particle.SMOKE, x + 0.5, y + 0.8, z + 0.5, 3, 0.25, 0.25, 0.25, 0.01);
                           world.spawnParticle(Particle.FLAME, x + 0.5, y + 0.8, z + 0.5, 2, 0.15, 0.15, 0.15, 0.01);

                        }
                     }
                  }
               }
            }

            // Force client biome visuals to update for touched chunks
            for (long key : chunksToRefresh) {
               int cx = (int)(key >> 32);
               int cz = (int)(key & 0xFFFFFFFFL);
               world.refreshChunk(cx, cz);
            }

            world.playSound(loc, Sound.BLOCK_NETHERRACK_PLACE, 0.6f, 1.0f);
            world.playSound(loc, Sound.BLOCK_LAVA_POP,        0.6f, 0.9f);
            world.playSound(loc, Sound.BLOCK_PORTAL_AMBIENT,  0.2f, 0.8f);
         }, ring * delayPerRing);
      }

      // Spawn a ghast once the animation is fully finished
      long totalDelay = maxRadius * delayPerRing + 5L; // small buffer
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
         // Find a decent spawn Y (clear 3-block air column) above the origin
         int tryFromY = Math.min(world.getMaxHeight() - 3, baseY + 8);
         int tryToY   = Math.min(world.getMaxHeight() - 3, baseY + 16);

         Location spawn = new Location(world, baseX + 0.5, tryFromY, baseZ + 0.5);
         boolean found = false;
         for (int y = tryFromY; y <= tryToY; y++) {
            Block b1 = world.getBlockAt(baseX, y,     baseZ);
            Block b2 = world.getBlockAt(baseX, y + 1, baseZ);
            Block b3 = world.getBlockAt(baseX, y + 2, baseZ);
            if (b1.getType().isAir() && b2.getType().isAir() && b3.getType().isAir()) {
               spawn.setY(y + 0.01);
               found = true;
               break;
            }
         }
         if (!found) spawn.setY(tryFromY); // fallback

         org.bukkit.entity.Entity e = world.spawnEntity(spawn, org.bukkit.entity.EntityType.GHAST);

         // Make it stick around and (optionally) target the player
         if (e instanceof org.bukkit.entity.Mob mob) {
            mob.setPersistent(true);
            try { mob.setTarget(p); } catch (Throwable ignored) {} // Ghast supports setTarget; guard just in case
         }
         // Entrance flair
         world.playSound(spawn, Sound.ENTITY_GHAST_SCREAM, 0.9f, 1.0f);
         world.spawnParticle(Particle.LARGE_SMOKE, spawn, 20, 1.2, 1.2, 1.2, 0.02);
         world.spawnParticle(Particle.FLAME,       spawn, 30, 0.8, 0.8, 0.8, 0.01);
      }, totalDelay);
   }



   /** Map your locked Theme to the target Nether Biome. */
   private static Biome biomeForTheme(de.eisner.luckyblocks.util.NetherTransformRegistry.Theme theme) {
      return switch (theme) {
         case CRIMSON -> Biome.CRIMSON_FOREST;
         case WARPED  -> Biome.WARPED_FOREST;
         case SOUL    -> Biome.SOUL_SAND_VALLEY;
         case BASALT  -> Biome.BASALT_DELTAS;
         default      -> Biome.NETHER_WASTES; // WASTES (or fallback)
      };
   }

   /** Packs chunk (cx,cz) into a long key. */
   private static long chunkKey(int cx, int cz) {
      return (((long) cx) << 32) ^ (cz & 0xFFFFFFFFL);
   }







//public static void registerDefaultTransforms() {
//   transformMap.clear();
//
//   transformMap.put(Material.STONE, Material.NETHERRACK);
//   transformMap.put(Material.GRASS_BLOCK, Material.CRIMSON_NYLIUM);
//   transformMap.put(Material.DIRT, Material.SOUL_SOIL);
//   transformMap.put(Material.SAND, Material.SOUL_SAND);
//   transformMap.put(Material.OAK_LOG, Material.WARPED_STEM);
//   transformMap.put(Material.BIRCH_LOG, Material.CRIMSON_STEM);
//   transformMap.put(Material.WATER, Material.LAVA);
//   transformMap.put(Material.GRAVEL, Material.MAGMA_BLOCK);
//   transformMap.put(Material.COAL_ORE, Material.NETHER_QUARTZ_ORE);
//   transformMap.put(Material.IRON_ORE, Material.ANCIENT_DEBRIS);
//
//}
//
//public static Material getReplacement(Material original) {
//      Bukkit.getLogger().info("Getting replacement for " + original);
//   return transformMap.getOrDefault(original, Material.NETHERRACK);
//}

}