package de.eisner.luckyblocks.drops.alldrops;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * PodestWithItemDrop
 *
 * Pyramid with pillar, lanterns, amethyst rings, gold/diorite accents, and
 * polished andesite sidewalks (stairs + 2-high wall rails) from bottom to top.
 */
public class PodestWithItemDrop extends Drop {

    public PodestWithItemDrop() {
        super(
                "PodestWithItem",
                Arrays.asList("Spruce + polished-stone pyramid with pillar, lanterns, amethyst rings, and 4 sidewalks."),
                DropType.GOOD,
                1L
        );
    }

    @Override
    public void execute(Player p, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        final Plugin plugin = Bukkit.getPluginManager().getPlugin("LuckyBlocks");
        if (plugin == null) return;

        // --- Build parameters ---
        final Material FILL          = Material.POLISHED_ANDESITE;
        final Material STAIRS_M      = Material.POLISHED_ANDESITE_STAIRS;
        final Material TRIM          = Material.SPRUCE_PLANKS;
        final Material PILLAR        = Material.POLISHED_ANDESITE;
        final Material TOP_TRIM_SLAB = Material.SPRUCE_SLAB;

        final int baseRadius = 4;      // half-size at ground
        final int layers     = 3;      // above-ground step layers before plateau
        final int plateauSz  = 3;      // 3x3 plateau
        final int pillarH    = 3;      // pillar height above plateau
        final int UNDER_DEPTH = 20;    // underground extension depth

        // Amethyst rings (thin ~1 block)
        final double AM_RING_SPACING = Math.PI * 1.75;
        final double AM_RING_THICK   = 0.50;
        final double AM_RING_OFFSET  = Math.PI * 0.5;

        // --- Place altar IN FRONT of the player ---
        final int forward = baseRadius + 6;
        Location base = loc.clone();
        double yawRad = Math.toRadians(loc.getYaw());
        int dfx = (int) Math.round(-Math.sin(yawRad) * forward);
        int dfz = (int) Math.round( Math.cos(yawRad) * forward);
        base.add(dfx, 0, dfz);

        final int cx = base.getBlockX();
        final int cy = Math.max(world.getMinHeight() + 1, base.getBlockY());
        final int cz = base.getBlockZ();

        final int topY = cy + layers; // plateau Y

        // --- Clear ONLY ABOVE the altar footprint to air ---
        int pad = baseRadius + 3;
        int minX = cx - pad, maxX = cx + pad;
        int minZ = cz - pad, maxZ = cz + pad;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = cy; y < world.getMaxHeight(); y++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (!b.isEmpty()) b.setType(Material.AIR, false);
                }
            }
        }

        // --- Gold placements: 4 evenly spaced depths per face ---
        final int[] GOLD_DEPTHS = evenSpacedDepths(UNDER_DEPTH, 4);

        // --- Build UNDERGROUND pyramid (solid) + decorate shell (gold/diorite/amethyst) ---
        for (int d = 1; d <= UNDER_DEPTH; d++) {
            int y = cy - d;
            if (y < world.getMinHeight()) break;
            int r = baseRadius + d; // each step down grows footprint by 1

            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    safeSet(world.getBlockAt(cx + x, y, cz + z), Material.POLISHED_ANDESITE);
                }
            }

            for (int i = -r; i <= r; i++) {
                decorateFaceCellWithStonesAndAmethyst(world, cx, cz, cx + r, y, cz + i, BlockFace.EAST,
                        i, d, r, GOLD_DEPTHS, AM_RING_SPACING, AM_RING_THICK, AM_RING_OFFSET);
                decorateFaceCellWithStonesAndAmethyst(world, cx, cz, cx - r, y, cz + i, BlockFace.WEST,
                        i, d, r, GOLD_DEPTHS, AM_RING_SPACING, AM_RING_THICK, AM_RING_OFFSET);
                decorateFaceCellWithStonesAndAmethyst(world, cx, cz, cx + i, y, cz + r, BlockFace.SOUTH,
                        i, d, r, GOLD_DEPTHS, AM_RING_SPACING, AM_RING_THICK, AM_RING_OFFSET);
                decorateFaceCellWithStonesAndAmethyst(world, cx, cz, cx + i, y, cz - r, BlockFace.NORTH,
                        i, d, r, GOLD_DEPTHS, AM_RING_SPACING, AM_RING_THICK, AM_RING_OFFSET);
            }
        }

        // --- Build stepped pyramid ABOVE ground (with inward stairs) ---
        for (int L = 0; L < layers; L++) {
            int r = baseRadius - L;
            int y = cy + L;

            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    safeSet(world.getBlockAt(cx + x, y, cz + z), FILL);
                }
            }

            // Inward-facing stairs ON TOP of the outer ring
            for (int x = -r; x <= r; x++) setStairSafe(world.getBlockAt(cx + x, y + 1, cz - r), STAIRS_M, BlockFace.SOUTH);
            for (int x = -r; x <= r; x++) setStairSafe(world.getBlockAt(cx + x, y + 1, cz + r), STAIRS_M, BlockFace.NORTH);
            for (int z = -r; z <= r; z++) setStairSafe(world.getBlockAt(cx - r, y + 1, cz + z), STAIRS_M, BlockFace.EAST);
            for (int z = -r; z <= r; z++) setStairSafe(world.getBlockAt(cx + r, y + 1, cz + z), STAIRS_M, BlockFace.WEST);

            // Spruce trim at layer corners
            safeSet(world.getBlockAt(cx - r, y + 1, cz - r), TRIM);
            safeSet(world.getBlockAt(cx - r, y + 1, cz + r), TRIM);
            safeSet(world.getBlockAt(cx + r, y + 1, cz - r), TRIM);
            safeSet(world.getBlockAt(cx + r, y + 1, cz + r), TRIM);
        }

        // --- Top plateau (polished andesite with spruce border) ---
        int halfPlat = plateauSz / 2; // 3 → 1
        for (int x = -halfPlat; x <= halfPlat; x++) {
            for (int z = -halfPlat; z <= halfPlat; z++) {
                boolean edge = (Math.abs(x) == halfPlat || Math.abs(z) == halfPlat);
                safeSet(world.getBlockAt(cx + x, topY, cz + z), edge ? TRIM : Material.POLISHED_ANDESITE);
            }
        }
        for (int x = -halfPlat; x <= halfPlat; x++) {
            for (int z = -halfPlat; z <= halfPlat; z++) {
                if (Math.abs(x) == halfPlat || Math.abs(z) == halfPlat) {
                    safeSet(world.getBlockAt(cx + x, topY + 1, cz + z), TOP_TRIM_SLAB);
                }
            }
        }

        // --- Central pillar ---
        for (int i = 1; i <= pillarH; i++) {
            safeSet(world.getBlockAt(cx, topY + i, cz), PILLAR);
        }

        // --- Sea lanterns: 4 sides (stay in place), plus one on pillar top ---
        //   ^^^ These are placed **before** sidewalks are built, but sidewalks use safe setters.
        safeSet(world.getBlockAt(cx - halfPlat - 1, topY, cz), Material.SEA_LANTERN);
        safeSet(world.getBlockAt(cx + halfPlat + 1, topY, cz), Material.SEA_LANTERN);
        safeSet(world.getBlockAt(cx, topY, cz - halfPlat - 1), Material.SEA_LANTERN);
        safeSet(world.getBlockAt(cx, topY, cz + halfPlat + 1), Material.SEA_LANTERN);
        safeSet(world.getBlockAt(cx, topY + pillarH + 1, cz), Material.SEA_LANTERN);

        buildSidewalks(world, cx, cy, cz, baseRadius, layers, UNDER_DEPTH, STAIRS_M);

        // --- Item frame (glow), attached to pillar, one block higher, facing player ---
        final BlockFace faceToPlayer = faceTowards(cx + 0.5, cz + 0.5, p.getLocation().getX(), p.getLocation().getZ());
        final int attachY = topY + Math.max(1, (pillarH / 2 + 1)) + 1;
        Block pillarBlock = world.getBlockAt(cx, attachY, cz);
        Block front = pillarBlock.getRelative(faceToPlayer);
        if (!front.getType().isAir()) front.setType(Material.AIR, false);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Location centerOfPillar = pillarBlock.getLocation().add(0.5, 0.5, 0.5);
            double off = 0.51;
            Location spawnLoc = centerOfPillar.add(faceToPlayer.getModX() * off, 0, faceToPlayer.getModZ() * off);

            ItemFrame frame = (ItemFrame) world.spawnEntity(spawnLoc, EntityType.GLOW_ITEM_FRAME);
            frame.setFacingDirection(faceToPlayer, true);
            frame.setFixed(false);
            frame.setVisible(true);

            ItemStack randomNonBlock = new ItemStack(pickRandomNonBlockItem());
            randomNonBlock.setAmount(1);
            frame.setItem(randomNonBlock, false);
        });

        // --- FX ---
        Location center = new Location(world, cx + 0.5, topY + 1.2, cz + 0.5);
        world.spawnParticle(Particle.END_ROD, center, 80, 1.5, 0.6, 1.5, 0.02);
        world.spawnParticle(Particle.ENCHANT, center, 140, 3.0, 1.0, 3.0, 0.0);
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.9f, 1.0f);
        world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.7f, 0.9f);
        world.playSound(center, Sound.BLOCK_CONDUIT_ACTIVATE, 0.9f, 1.1f);
        world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.85f, 1.2f);
    }

    /* =========================
       Decoration helpers
       ========================= */

    private int[] evenSpacedDepths(int depth, int count) {
        count = Math.min(count, Math.max(1, depth / 2));
        int[] arr = new int[count];
        double step = (depth + 1) / (double)(count + 1);
        for (int i = 0; i < count; i++) {
            arr[i] = Math.max(1, Math.min(depth, (int)Math.round((i + 1) * step)));
        }
        return arr;
    }

    private boolean isIn(int v, int[] arr) {
        for (int a : arr) if (a == v) return true;
        return false;
    }

    private void decorateFaceCellWithStonesAndAmethyst(
            World world, int cx, int cz,
            int x, int y, int z, BlockFace face,
            int i, int d, int r,
            int[] goldDepths,
            double AM_RING_SPACING, double AM_RING_THICK, double AM_RING_OFFSET
    ) {
        final Block b = world.getBlockAt(x, y, z);

        // GOLD line (centerline; 4 even depths)
        if (i == 0 && isIn(d, goldDepths)) {
            safeSet(b, Material.GOLD_BLOCK);
            return;
        }

        // Polished diorite accents (kept)
        final int ai = Math.abs(i);
        if ((ai + d) % 6 == 0 || (ai % 7 == 0 && ai > 0)) {
            safeSet(b, Material.POLISHED_DIORITE);
            return;
        }

        // AMETHYST rings (thin ≈ 1 block)
        final double dx = (x + 0.5) - (cx + 0.5);
        final double dz = (z + 0.5) - (cz + 0.5);
        final double radius = Math.sqrt(dx * dx + dz * dz);

        boolean ringA = inRing(radius, AM_RING_SPACING, AM_RING_THICK, 0.0);
        boolean ringB = inRing(radius, AM_RING_SPACING, AM_RING_THICK, AM_RING_OFFSET);

        if (ringA || ringB) {
            // don't overwrite existing gold/diorite/lanterns
            Material cur = b.getType();
            if (cur == Material.GOLD_BLOCK || cur == Material.POLISHED_DIORITE || cur == Material.SEA_LANTERN) return;

            safeSet(b, Material.AMETHYST_BLOCK);

            // very sparse outward echo to emphasize ring without flattening silhouette
            if (((ai + d) % 13 == 0)) {
                Block outside = b.getRelative(face);
                if (outside.getType().isAir()) safeSet(outside, Material.AMETHYST_BLOCK);
            }
        }
    }

    /** ring test near multiples of spacing; offset allows a second interleaved family. */
    private boolean inRing(double radius, double spacing, double thick, double phaseOffset) {
        double x = (radius + phaseOffset) / spacing;
        double frac = x - Math.floor(x);
        double distToCenter = Math.min(frac, 1.0 - frac) * spacing;
        return distToCenter <= thick;
    }

    /* =========================
       Safe setters / stairs / walls / facing / items
       ========================= */

    private void buildSidewalks(World world, int cx, int cy, int cz,
                                int baseRadius, int layers, int underDepth,
                                Material stairsMat) {
        buildSidewalk(world, cx, cy, cz, baseRadius, layers, underDepth, BlockFace.NORTH, stairsMat);
        buildSidewalk(world, cx, cy, cz, baseRadius, layers, underDepth, BlockFace.SOUTH, stairsMat);
        buildSidewalk(world, cx, cy, cz, baseRadius, layers, underDepth, BlockFace.WEST,  stairsMat);
        buildSidewalk(world, cx, cy, cz, baseRadius, layers, underDepth, BlockFace.EAST,  stairsMat);
    }

    /**
     * 3-wide polished-andesite path with continuous stairs and 2-high wall rails:
     *  - Places an approach step one block outside the base face.
     *  - Runs stairs + rails from y = (cy-underDepth) all the way to the plateau.
     *  - Rails use a polished-andesite "backer" spine so walls show connected wings on each step.
     *  - Uses safe setters (never overwrites SEA_LANTERN, etc.).
     */
    private void buildSidewalk(World world, int cx, int cy, int cz,
                               int baseRadius, int layers, int underDepth,
                               BlockFace face, Material stairsMat) {

        int plateauY = cy + layers;

        // ---------- Helper lambdas to get face-local coordinates ----------
        java.util.function.IntFunction<int[]> groundPosAtLayer = (L) -> {
            int r = baseRadius - L;
            return switch (face) {
                case NORTH -> new int[]{cx, cz - r};
                case SOUTH -> new int[]{cx, cz + r};
                case WEST  -> new int[]{cx - r, cz};
                case EAST  -> new int[]{cx + r, cz};
                default    -> new int[]{cx, cz};
            };
        };

        java.util.function.IntFunction<int[]> undergroundPosAtDepth = (d) -> {
            int r = baseRadius + d;
            return switch (face) {
                case NORTH -> new int[]{cx, cz - r};
                case SOUTH -> new int[]{cx, cz + r};
                case WEST  -> new int[]{cx - r, cz};
                case EAST  -> new int[]{cx + r, cz};
                default    -> new int[]{cx, cz};
            };
        };

        BlockFace stairFacing = switch (face) {
            case NORTH -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.NORTH;
            case WEST  -> BlockFace.EAST;
            case EAST  -> BlockFace.WEST;
            default    -> BlockFace.SOUTH;
        };

        int offX1 = 0, offZ1 = 0, offX2 = 0, offZ2 = 0;
        boolean alongX = (face == BlockFace.NORTH || face == BlockFace.SOUTH);
        if (alongX) { offX1 = -1; offX2 = +1; } else { offZ1 = -1; offZ2 = +1; }

        int railXLeft = alongX ? -2 : 0;
        int railZLeft = alongX ?  0 : -2;
        int railXRight= alongX ? +2 : 0;
        int railZRight= alongX ?  0 : +2;

        // ---------- Approach step just outside the base face ----------
        {
            int[] pos = undergroundPosAtDepth.apply(0);
            int fx = pos[0], fz = pos[1];
            switch (face) {
                case NORTH -> fz -= 1;
                case SOUTH -> fz += 1;
                case WEST  -> fx -= 1;
                case EAST  -> fx += 1;
            }

            if (alongX) {
                for (int x = cx - 1; x <= cx + 1; x++) safeSet(world.getBlockAt(x, cy - 1, fz), Material.POLISHED_ANDESITE);
                for (int x = cx - 1; x <= cx + 1; x++) setStairSafe(world.getBlockAt(x, cy, fz), stairsMat, stairFacing);

                wall2HighWithBacker(world, cx + railXLeft,  cy, fz, face, plateauY);
                wall2HighWithBacker(world, cx + railXRight, cy, fz, face, plateauY);
            } else {
                for (int z = cz - 1; z <= cz + 1; z++) safeSet(world.getBlockAt(fx, cy - 1, z), Material.POLISHED_ANDESITE);
                for (int z = cz - 1; z <= cz + 1; z++) setStairSafe(world.getBlockAt(fx, cy, z), stairsMat, stairFacing);

                wall2HighWithBacker(world, fx, cy, cz + railZLeft,  face, plateauY);
                wall2HighWithBacker(world, fx, cy, cz + railZRight, face, plateauY);
            }
        }

        // ---------- From the bottom (-underDepth) up to the plateau ----------

        // 1) Underground climb (d = underDepth .. 1)
        for (int d = underDepth; d >= 1; d--) {
            int y = cy - d;
            int[] pos = undergroundPosAtDepth.apply(d);
            int fx = pos[0], fz = pos[1];

            if (alongX) {
                safeSet(world.getBlockAt(cx,           y, fz), Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(cx + offX1,   y, fz), Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(cx + offX2,   y, fz), Material.POLISHED_ANDESITE);

                setStairSafe(world.getBlockAt(cx,           y + 1, fz), stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(cx + offX1,   y + 1, fz), stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(cx + offX2,   y + 1, fz), stairsMat, stairFacing);

                wall2HighWithBacker(world, cx + railXLeft,  y + 1, fz, face, plateauY);
                wall2HighWithBacker(world, cx + railXRight, y + 1, fz, face, plateauY);
            } else {
                safeSet(world.getBlockAt(fx, y, cz),           Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(fx, y, cz + offZ1),   Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(fx, y, cz + offZ2),   Material.POLISHED_ANDESITE);

                setStairSafe(world.getBlockAt(fx, y + 1, cz),           stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(fx, y + 1, cz + offZ1),   stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(fx, y + 1, cz + offZ2),   stairsMat, stairFacing);

                wall2HighWithBacker(world, fx, y + 1, cz + railZLeft,  face, plateauY);
                wall2HighWithBacker(world, fx, y + 1, cz + railZRight, face, plateauY);
            }
        }

        // 2) Ground to plateau (L = 0 .. layers-1)
        for (int L = 0; L < layers; L++) {
            int y = cy + L;
            int[] pos = groundPosAtLayer.apply(L);
            int fx = pos[0], fz = pos[1];

            if (alongX) {
                safeSet(world.getBlockAt(cx,           y, fz), Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(cx + offX1,   y, fz), Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(cx + offX2,   y, fz), Material.POLISHED_ANDESITE);

                setStairSafe(world.getBlockAt(cx,           y + 1, fz), stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(cx + offX1,   y + 1, fz), stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(cx + offX2,   y + 1, fz), stairsMat, stairFacing);

                wall2HighWithBacker(world, cx + railXLeft,  y + 1, fz, face, plateauY);
                wall2HighWithBacker(world, cx + railXRight, y + 1, fz, face, plateauY);
            } else {
                safeSet(world.getBlockAt(fx, y, cz),           Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(fx, y, cz + offZ1),   Material.POLISHED_ANDESITE);
                safeSet(world.getBlockAt(fx, y, cz + offZ2),   Material.POLISHED_ANDESITE);

                setStairSafe(world.getBlockAt(fx, y + 1, cz),           stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(fx, y + 1, cz + offZ1),   stairsMat, stairFacing);
                setStairSafe(world.getBlockAt(fx, y + 1, cz + offZ2),   stairsMat, stairFacing);

                wall2HighWithBacker(world, fx, y + 1, cz + railZLeft,  face, plateauY);
                wall2HighWithBacker(world, fx, y + 1, cz + railZRight, face, plateauY);
            }
        }
    }



    /** Never overwrite sea lanterns (preserves the 4 top lanterns). */
    private void safeSet(Block b, Material m) {
        Material cur = b.getType();
        if (cur == Material.SEA_LANTERN) return;
        if (cur == m) return;
        b.setType(m, false);
    }

    private void setStairSafe(Block b, Material stairMat, BlockFace facing) {
        if (b.getType() == Material.SEA_LANTERN) return; // preserve
        b.setType(stairMat, false);
        BlockData data = b.getBlockData();
        if (data instanceof Stairs stairs) {
            stairs.setFacing(facing);
            stairs.setHalf(Stairs.Half.BOTTOM);
            stairs.setShape(Stairs.Shape.STRAIGHT);
            b.setBlockData(stairs, false);
        }
    }

    /** Places a 2-block-high polished_andesite_wall column at (x, y, z). */
    private void safeWall2High(World w, int x, int y, int z) {
        Block b1 = w.getBlockAt(x, y, z);
        Block b2 = w.getBlockAt(x, y + 1, z);
        if (b1.getType() != Material.SEA_LANTERN) b1.setType(Material.ANDESITE_WALL, false);
        if (b2.getType() != Material.SEA_LANTERN) b2.setType(Material.ANDESITE_WALL, false);
    }

    private BlockFace faceTowards(double sx, double sz, double tx, double tz) {
        double dx = tx - sx;
        double dz = tz - sz;
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            return dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }

    private Material pickRandomNonBlockItem() {
        List<Material> pool = new ArrayList<>();
        for (Material m : Material.values()) {
            if (m == Material.AIR) continue;
            if (m.isBlock()) continue;
            if (m.name().contains("LEGACY")) continue;
            pool.add(m);
        }
        if (pool.isEmpty()) return Material.STICK;
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private void wall2HighWithBacker(World w, int x, int y, int z, BlockFace outward, int plateauY) {
        // Skip placing backers in the top band or near lanterns to avoid “weird andesite”
        boolean inTopBand = (y >= plateauY - 1); // right under the lantern layer
        boolean nearLantern =
                isNeighborOf(w, x, y, z, Material.SEA_LANTERN) ||
                        isNeighborOf(w, x, y + 1, z, Material.SEA_LANTERN);

        // If it isn't the sensitive area, lay the backer so walls show connected wings
        if (!inTopBand && !nearLantern) {
            // base backer
            Block base = w.getBlockAt(x, y, z);
            if (base.getType().isAir()) base.setType(Material.POLISHED_ANDESITE, false);

            // inner backer one block inward toward the center (same Y)
            int ix = x - outward.getModX();
            int iz = z - outward.getModZ();
            Block inner = w.getBlockAt(ix, y, iz);
            if (inner.getType().isAir()) inner.setType(Material.POLISHED_ANDESITE, false);
            // (No “cap” above — that was the bit that could peek near the top ring)
        }

        // Two-high wall column — **physics=true** so connections are recalculated
        Block w1 = w.getBlockAt(x, y + 1, z);
        Block w2 = w.getBlockAt(x, y + 2, z);
        if (w1.getType() != Material.SEA_LANTERN) w1.setType(Material.ANDESITE_WALL, true);
        if (w2.getType() != Material.SEA_LANTERN) w2.setType(Material.ANDESITE_WALL, true);
    }

    /** Check 6-face neighbors for a specific type. */
    private boolean isNeighborOf(World w, int x, int y, int z, Material type) {
        return w.getBlockAt(x + 1, y, z).getType() == type ||
                w.getBlockAt(x - 1, y, z).getType() == type ||
                w.getBlockAt(x, y + 1, z).getType() == type ||
                w.getBlockAt(x, y - 1, z).getType() == type ||
                w.getBlockAt(x, y, z + 1).getType() == type ||
                w.getBlockAt(x, y, z - 1).getType() == type;
    }
}



