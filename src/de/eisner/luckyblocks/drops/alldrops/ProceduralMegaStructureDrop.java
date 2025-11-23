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
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ProceduralMegaStructureDrop
 *
 * Randomly chooses a 5-block palette and generates a large, symmetrical structure
 * using nested loops and math functions (sin/cos, waves, spirals, triangles, circles),
 * with optional recursive components. The result is different each time.
 *
 * Written for Spigot 1.21.8. Uses only Bukkit/Spigot API.
 */
public class ProceduralMegaStructureDrop extends Drop {

    // ===========================
    // Tunables (feel free to tweak)
    // ===========================

    /** Max total blocks to queue per execution (safety cap). */
    private static final int MAX_PLACEMENTS = 250_000;

    /** How many block placements to apply per tick (balance performance). */
    private static final int PLACEMENTS_PER_TICK = 4000; // up from 2500

    /** Radius-ish scale: base extents for X/Z (width/depth). */
    private static final int BASE_RADIUS_XZ = 42;        // down from 60 (tighter)

    /** Height scale for Y extents. */
    private static final int BASE_HEIGHT = 120;

    /** Whether to hollow large volumes (saves blocks). */
    private static final boolean HOLLOW_LARGE_VOLUMES = true;

    /** Probability to mirror (kept) */
    private static final double MIRROR_PROBABILITY = 0.85;

    /** Lift a bit above ground */
    private static final int Y_LIFT = 6;

    /** FX on/off */
    private static final boolean FX_ENABLED = true;

    /** New: compactness multiplier (applied to many horizontal radii) */
    private static final double COMPACT = 0.72; // 0.6–0.8 feels good

    /** New: density multiplier for loops (1=original, 2=twice as many passes) */
    private static final int DENSITY = 2;       // try 2–3

    // ===========================
    // Construction
    // ===========================

    public ProceduralMegaStructureDrop() {
        super(
                "ProceduralMegaStructure",
                Arrays.asList(
                        "Spawns a massive, math-driven, random mega-structure with a 5-block palette.",
                        "Always symmetrical, often recursive, and looks totally different each time."
                ),
                DropType.NEUTRAL,
                1L
        );
    }

    // ===========================
    // Entry
    // ===========================

    @Override
    public void execute(Player p, Location loc) {
        final World world = loc.getWorld();
        if (world == null) return;

        final Plugin plugin = Bukkit.getPluginManager().getPlugin("LuckyBlocks");
        if (plugin == null) return;

        final Random rnd = ThreadLocalRandom.current();

        // Base (center) point. Slightly lift to prevent burying into terrain.
        final int baseX = loc.getBlockX();
        final int baseY = Math.max(world.getMinHeight() + 8, loc.getBlockY() + Y_LIFT);
        final int baseZ = loc.getBlockZ();

        // Build context
        final StructureContext ctx = new StructureContext(
                plugin,
                world,
                new Location(world, baseX + 0.5, baseY - 7.0, baseZ + 0.5),
                rnd,
                chooseSymmetry(rnd),
                choosePalette(world, rnd)
        );

        // Plan the structure
        StructurePlan plan = randomPlan(ctx, rnd);

        // Generate all placements (queued), then start the runner
        plan.generate(ctx);

        if (FX_ENABLED) {
            world.playSound(ctx.origin, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.0f);
            world.spawnParticle(Particle.PORTAL, ctx.origin, 80, 3.0, 3.0, 3.0, 0.05);
        }

        // Start applying placements in batches
        new PlacementRunner(ctx).runTaskTimer(plugin, 1L, 1L);
    }

    // ===========================
    // Context & helpers
    // ===========================

    /** Symmetry modes. */
    private enum Symmetry {
        NONE,          // no mirroring
        MIRROR_X,      // mirror across X axis
        MIRROR_Z,      // mirror across Z axis
        MIRROR_BOTH,   // mirror across both X and Z
        RADIAL_4,      // 4-way radial symmetry (quadrants)
        RADIAL_8       // 8-way radial symmetry (octants on X/Z)
    }

    /** Holds the 5-block palette with weighted selection. */
    private static class BlockPalette {
        final Material main1;
        final Material main2;
        final Material secondary;
        final Material accent1;
        final Material accent2;

        // Weighted indices for fast weighted pick
        private final Material[] weighted;

        private BlockPalette(Material main1, Material main2, Material secondary, Material accent1, Material accent2) {
            this.main1 = main1;
            this.main2 = main2;
            this.secondary = secondary;
            this.accent1 = accent1;
            this.accent2 = accent2;

            // Build weighted array: main blocks very frequent, secondary often, accents rare
            List<Material> list = new ArrayList<>();
            addRepeat(list, main1, 12);
            addRepeat(list, main2, 12);
            addRepeat(list, secondary, 7);
            addRepeat(list, accent1, 3);
            addRepeat(list, accent2, 2);
            this.weighted = list.toArray(new Material[0]);
        }

        private static void addRepeat(List<Material> dst, Material m, int n) {
            for (int i = 0; i < n; i++) dst.add(m);
        }

        /** Weighted random choose. */
        Material pick(Random rnd) {
            return weighted[rnd.nextInt(weighted.length)];
        }

        /** Alternating main block for stripes/ribs. */
        Material altMain(int i) {
            return (i & 1) == 0 ? main1 : main2;
        }

        /** Accent with bias. */
        Material accent(Random rnd) {
            return rnd.nextBoolean() ? accent1 : accent2;
        }
    }

    /** Simple integer vector. */
    private static final class Vec3i {
        final int x, y, z;
        Vec3i(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }

        Vec3i add(int dx, int dy, int dz) {
            return new Vec3i(x + dx, y + dy, z + dz);
        }
    }

    /** Placement record. */
    private static final class Placement {
        final int x, y, z;
        final Material mat;
        Placement(int x, int y, int z, Material mat) {
            this.x = x; this.y = y; this.z = z; this.mat = mat;
        }
    }

    /** Shared generation context. */
    private static final class StructureContext {
        final Plugin plugin;
        final World world;
        final Location origin;
        final Random rnd;
        final Symmetry symmetry;
        final BlockPalette palette;

        final Deque<Location> originStack = new ArrayDeque<>();
        final ArrayDeque<Placement> queue = new ArrayDeque<>(131072);
        final Set<Long> placedKeys = new HashSet<>(131072); // prevent duplicates
        int placementCount = 0;

        StructureContext(Plugin plugin, World world, Location origin, Random rnd, Symmetry symmetry, BlockPalette palette) {
            this.plugin = plugin;
            this.world = world;
            this.origin = origin.clone();
            this.rnd = rnd;
            this.symmetry = symmetry;
            this.palette = palette;
        }

        void pushOrigin(Location loc) {
            // save current origin
            originStack.push(origin.clone());
            // move origin to new location
            origin.setWorld(loc.getWorld());
            origin.setX(loc.getX());
            origin.setY(loc.getY());
            origin.setZ(loc.getZ());
            origin.setYaw(loc.getYaw());
            origin.setPitch(loc.getPitch());
        }

        void popOrigin() {
            if (!originStack.isEmpty()) {
                Location prev = originStack.pop();
                origin.setWorld(prev.getWorld());
                origin.setX(prev.getX());
                origin.setY(prev.getY());
                origin.setZ(prev.getZ());
                origin.setYaw(prev.getYaw());
                origin.setPitch(prev.getPitch());
            }
        }


        /** Enqueue a placement, with symmetry applied. */
        void place(int rx, int ry, int rz, Material mat) {
            if (placementCount >= MAX_PLACEMENTS) return;

            // Absolute coords
            int ax = (int) Math.floor(origin.getX()) + rx;
            int ay = (int) Math.floor(origin.getY()) + ry;
            int az = (int) Math.floor(origin.getZ()) + rz;

            // Inside world?
            if (ay < world.getMinHeight() || ay >= world.getMaxHeight()) return;

            // primary
            enqueue(ax, ay, az, mat);

            // mirrors
            switch (symmetry) {
                case MIRROR_X -> {
                    enqueue(-rx + (int) Math.floor(origin.getX()), ay, az, mat);
                }
                case MIRROR_Z -> {
                    enqueue(ax, ay, -rz + (int) Math.floor(origin.getZ()), mat);
                }
                case MIRROR_BOTH -> {
                    enqueue(-rx + (int) Math.floor(origin.getX()), ay, az, mat);
                    enqueue(ax, ay, -rz + (int) Math.floor(origin.getZ()), mat);
                    enqueue(-rx + (int) Math.floor(origin.getX()), ay, -rz + (int) Math.floor(origin.getZ()), mat);
                }
                case RADIAL_4 -> {
                    // quadrants at 90° (mirror x, mirror z, both)
                    enqueue(-rx + (int) Math.floor(origin.getX()), ay, az, mat);
                    enqueue(ax, ay, -rz + (int) Math.floor(origin.getZ()), mat);
                    enqueue(-rx + (int) Math.floor(origin.getX()), ay, -rz + (int) Math.floor(origin.getZ()), mat);
                }
                case RADIAL_8 -> {
                    // 8 rotations: we simulate by mirroring X/Z and swapping coordinates
                    // Base 4 mirrors (as RADIAL_4)
                    int ox = (int) Math.floor(origin.getX());
                    int oz = (int) Math.floor(origin.getZ());
                    int mx = -rx + ox;
                    int mz = -rz + oz;

                    enqueue(mx, ay, az, mat);
                    enqueue(ax, ay, mz, mat);
                    enqueue(mx, ay, mz, mat);

                    // Swaps (rotate 90°)
                    int sx1 = oz - originBlockZ() + rz + ox - originBlockX(); // tricky but keep it simple
                    int sz1 = originBlockZ() - oz + rx + oz - originBlockZ();

                    // Alternatively, simpler: approximate more variants with small permutations:
                    enqueue((int) Math.floor(origin.getX()) + rz, ay, (int) Math.floor(origin.getZ()) + rx, mat);
                    enqueue((int) Math.floor(origin.getX()) - rz, ay, (int) Math.floor(origin.getZ()) + rx, mat);
                    enqueue((int) Math.floor(origin.getX()) + rz, ay, (int) Math.floor(origin.getZ()) - rx, mat);
                    enqueue((int) Math.floor(origin.getX()) - rz, ay, (int) Math.floor(origin.getZ()) - rx, mat);
                }
                default -> { /* NONE */ }
            }
        }

        private int originBlockX() { return (int) Math.floor(origin.getX()); }
        private int originBlockZ() { return (int) Math.floor(origin.getZ()); }

        private void enqueue(int x, int y, int z, Material mat) {
            if (placementCount >= MAX_PLACEMENTS) return;
            long key = packKey(x, y, z);
            if (placedKeys.add(key)) {
                queue.addLast(new Placement(x, y, z, mat));
                placementCount++;
            }
        }

        private static long packKey(int x, int y, int z) {
            // pack into 64-bit: 22 bits per coord (enough for world range in practice)
            return (((long) (x & 0x3FFFFF)) << 42) | (((long) (y & 0x3FFFFF)) << 21) | ((long) (z & 0x3FFFFF));
        }
    }

    /** Applies queued placements gradually to prevent lag. */
    private static final class PlacementRunner extends BukkitRunnable {
        private final StructureContext ctx;
        private int ticks = 0;

        PlacementRunner(StructureContext ctx) { this.ctx = ctx; }

        @Override
        public void run() {
            int n = 0;
            while (n < PLACEMENTS_PER_TICK && !ctx.queue.isEmpty()) {
                Placement p = ctx.queue.pollFirst();
                if (p != null) {
                    Block b = ctx.world.getBlockAt(p.x, p.y, p.z);
                    b.setType(p.mat, false);
                }
                n++;
            }

            // FX dribble
            if (FX_ENABLED && ticks % 10 == 0) {
                ctx.world.spawnParticle(Particle.PORTAL, ctx.origin, 30, 2.0, 2.0, 2.0, 0.05);
                ctx.world.playSound(ctx.origin, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.2f, 1.2f);
            }

            ticks++;
            if (ctx.queue.isEmpty()) {
                if (FX_ENABLED) {
                    ctx.world.playSound(ctx.origin, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.1f);
                    ctx.world.spawnParticle(Particle.DRAGON_BREATH, ctx.origin, 60, 3.0, 3.0, 3.0, 0.02);
                }
                cancel();
            }
        }
    }

    // ===========================
    // Planning & generation
    // ===========================

    /** High-level plan object. */
    private interface StructurePlan {
        void generate(StructureContext ctx);
    }

    /** Produces a randomized plan that composes several generators. */
    private StructurePlan randomPlan(StructureContext ctx, Random rnd) {
        // Randomly choose a framework:
        int choice = rnd.nextInt(6);

        return switch (choice) {
            case 0 -> (c) -> plan_SpiralCitadel(c, rnd);
            case 1 -> (c) -> plan_CircleWavesAndRibs(c, rnd);
            case 2 -> (c) -> plan_ArchesAndTriangles(c, rnd);
            case 3 -> (c) -> plan_OrbWeaveNested(c, rnd);
            case 4 -> (c) -> plan_FractalPyramidsAndBridges(c, rnd);
            default -> (c) -> plan_MultiLayerGardenDome(c, rnd);
        };
    }

    // ===========================
    // Palette & symmetry selection
    // ===========================

    private Symmetry chooseSymmetry(Random rnd) {
        double r = rnd.nextDouble();
        if (r < 0.15) return Symmetry.NONE;
        if (r < 0.45) return Symmetry.MIRROR_BOTH;
        if (r < 0.65) return Symmetry.MIRROR_X;
        if (r < 0.85) return Symmetry.MIRROR_Z;
        return rnd.nextBoolean() ? Symmetry.RADIAL_4 : Symmetry.RADIAL_8;
    }

    /** Choose a 5-block palette from all block IDs, biasing towards useful building blocks. */
    private BlockPalette choosePalette(World world, Random rnd) {
        // Gather valid block materials
        List<Material> candidates = new ArrayList<>();
        for (Material m : Material.values()) {
            if (!m.isBlock()) continue;
            if (m == Material.AIR) continue;
            // Avoid fluids and command/technical nonsense
            String n = m.name();
            if (n.contains("BARRIER") || n.contains("STRUCTURE_BLOCK") || n.contains("STRUCTURE_VOID")) continue;
            if (n.contains("WATER") || n.contains("LAVA") || n.contains("POWDER_SNOW")) continue;
            if (n.contains("FIRE") || n.contains("LIGHT")) continue;
            if (n.endsWith("_SIGN") || n.endsWith("WALL_SIGN")) continue;
            candidates.add(m);
        }

        // If the random list ends up small (shouldn't), add some staples
        if (candidates.size() < 64) {
            Collections.addAll(candidates, Material.STONE, Material.DEEPSLATE, Material.SMOOTH_STONE,
                    Material.POLISHED_BLACKSTONE, Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS,
                    Material.QUARTZ_BLOCK, Material.CALCITE, Material.BASALT, Material.SMOOTH_BASALT,
                    Material.BRICKS, Material.SANDSTONE, Material.CUT_SANDSTONE);
        }

        // Pick 5 distinct
        Collections.shuffle(candidates, rnd);
        Material a = candidates.get(0);
        Material b = candidates.get(1);
        Material c = candidates.get(2);
        Material d = candidates.get(3);
        Material e = candidates.get(4);

        // Ensure at least two solids for primaries (fallback if not solid)
        if (!a.isSolid()) a = Material.GREEN_CONCRETE;
        if (!b.isSolid()) b = Material.LIME_WOOL;

        return new BlockPalette(a, b, c, d, e);
    }

    // ===========================
    // Plans (high-level orchestrators)
    // ===========================

    /** Plan 0: Spiral Citadel with radial ribs and sine wave galleries. */
    private void plan_SpiralCitadel(StructureContext ctx, Random rnd) {
        int rx = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(20)) * COMPACT);
        int rz = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(20)) * COMPACT);
        int h  = BASE_HEIGHT + rnd.nextInt(60);

        spiralTower(ctx, 0, 0, 0, Math.max(4, rx / 4), h, 0.18, ctx.palette.main1);

        int ribs = 12 + rnd.nextInt(6); // more ribs
        for (int i = 0; i < ribs; i++) {
            double ang = (Math.PI * 2.0) * (i / (double) ribs);
            int ox = (int) Math.round(Math.cos(ang) * (rx * 0.45)); // was 0.6
            int oz = (int) Math.round(Math.sin(ang) * (rz * 0.45));
            for (int k = 0; k < DENSITY; k++) {
                waveRibbonVertical(ctx, ox, 0, oz, h, 0.22 + rnd.nextDouble() * 0.2, ctx.palette.altMain(i + k));
            }
        }

        for (int level = 12; level < h; level += 12 + rnd.nextInt(6)) { // closer levels
            circleBand(ctx, 0, level, 0, rx - 3, 3, ctx.palette.secondary, true);
            for (int k = 0; k < DENSITY; k++) {
                sinCosDecorRing(ctx, 0, level + 1 + k, 0, rx - 2, ctx.palette.accent(ctx.rnd));
            }
        }

        dome(ctx, 0, h, 0, Math.max(6, rx / 2), ctx.palette.secondary, true);
        spiresCross(ctx, 0, h + rx / 2, 0, rx / 2 + 8, ctx.palette.accent(ctx.rnd));
    }


    /** Four tapering spires in a cross shape from (ox,oy,oz). */
    private void spiresCross(StructureContext ctx, int ox, int oy, int oz, int radius, Material mat) {
        // Each step outwards builds a shorter vertical column -> taper
        for (int r = 0; r <= radius; r++) {
            int height = Math.max(1, (int) Math.round((radius - r) * 0.75));

            // +X arm
            for (int y = 0; y < height; y++) ctx.place(ox + r, oy + y, oz, (y % 5 == 0) ? ctx.palette.secondary : mat);
            // -X arm
            for (int y = 0; y < height; y++) ctx.place(ox - r, oy + y, oz, (y % 5 == 0) ? ctx.palette.secondary : mat);
            // +Z arm
            for (int y = 0; y < height; y++) ctx.place(ox, oy + y, oz + r, (y % 5 == 0) ? ctx.palette.secondary : mat);
            // -Z arm
            for (int y = 0; y < height; y++) ctx.place(ox, oy + y, oz - r, (y % 5 == 0) ? ctx.palette.secondary : mat);

            // subtle accent rings every few steps to make it more ornate
            if (r % 6 == 0 && r > 0) {
                Material accent = ctx.palette.accent(ctx.rnd);
                // small ring around each arm tip at its current height
                int yTip = oy + height - 1;
                // around +X tip
                ringSegment(ctx, ox + r, yTip, oz, 2, accent);
                // -X tip
                ringSegment(ctx, ox - r, yTip, oz, 2, accent);
                // +Z tip
                ringSegment(ctx, ox, yTip, oz + r, 2, accent);
                // -Z tip
                ringSegment(ctx, ox, yTip, oz - r, 2, accent);
            }
        }

        // Cap the very center with a small crown
        sphere(ctx, ox, oy + (int) Math.round(radius * 0.75) + 2, oz, Math.max(2, radius / 8), ctx.palette.secondary, true);
    }


    /** Plan 1: Concentric circle waves and vertical ribs. */
    private void plan_CircleWavesAndRibs(StructureContext ctx, Random rnd) {
        Bukkit.getLogger().info("Plan Circle Waves and Ribs (dense / tan-sway)");

        // Pull in horizontally a bit for tighter composition
        int rx = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(24)) * COMPACT);
        int rz = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(24)) * COMPACT);
        int h  = BASE_HEIGHT + rnd.nextInt(30);

        // ------------------------------------------------------------------
        // (1) TAN-SWAYED RING STACKS: rings that drift on +X as Y increases
        // ------------------------------------------------------------------
        // We keep your radius loop, but for each radius we stack multiple ring-bands upward.
        // The x-offset at each Y is shifted by tan(freq * y) * amp (clamped so it can't explode).
        double baseFreq = 0.07 + rnd.nextDouble() * 0.03;  // gentle tan frequency
        int yMax = Math.min(h / 2, 48 + rnd.nextInt(20));  // how high the swayed rings go
        int yStep = 2;                                     // tighter layer spacing

        for (int r = 6; r < rx; r += 3) {
            // amplitude scaled to radius but clamped small to avoid wild swings
            int amp = clamp((int) Math.round(r * 0.10), 1, 6);

            for (int y = 0; y <= yMax; y += yStep) {
                int shiftX = tanShift(baseFreq, y, amp); // <-- the tan-based lateral drift

                // Density: lay 1..DENSITY parallel bands (tiny Y offsets) to thicken visuals
                for (int k = 0; k < DENSITY; k++) {
                    circleBand(ctx, shiftX, y + k, 0, r, 3, ctx.palette.altMain(r + k), true);
                }

                // Occasionally accent a second offset along Z to break uniformity (still symmetrical overall)
                if ((r + y) % 13 == 0) {
                    circleBand(ctx, shiftX, y, 1, r - 1, 2, ctx.palette.accent(ctx.rnd), true);
                }
            }
        }

        // -------------------------------------------------------------
        // (2) RIBS with NESTED LOOPS: verticals + random extra details
        // -------------------------------------------------------------
        int ribs = 14 + rnd.nextInt(8);
        for (int i = 0; i < ribs; i++) {
            double ang = (Math.PI * 2.0) * (i / (double) ribs);
            int ox = (int) Math.round(Math.cos(ang) * (rx * 0.55));
            int oz = (int) Math.round(Math.sin(ang) * (rz * 0.55));

            // The main rib
            verticalRib(ctx, ox, 0, oz, h, ctx.palette.altMain(i));

            // An offset secondary rib every other rib (as you had)
            if (i % 2 == 0) {
                verticalRib(ctx, ox / 2, h / 3, oz / 2, h / 2, ctx.palette.secondary);
            }

            // ---- NEW: Random nested detailing on some ribs ----
            // 60%: wrap a thin "helix" that hugs the rib
            if (rnd.nextDouble() < 0.60) {
                double pitch = 0.45 + rnd.nextDouble() * 0.25;
                int   rad    = 2; // tight
                Material m   = ctx.palette.accent(ctx.rnd);
                for (int y = 0; y < h; y += 2) {
                    double a = y * pitch;
                    int dx = (int) Math.round(Math.cos(a) * rad);
                    int dz = (int) Math.round(Math.sin(a) * rad);
                    ctx.place(ox + dx, y, oz + dz, m);
                    if ((y % 10) == 0) {
                        // micro ring around the current helix point to add sparkle
                        ringSegment(ctx, ox + dx, y, oz + dz, 2, ctx.palette.secondary);
                    }
                }
            }

            // 40%: add accent rings climbing up the rib
            if (rnd.nextDouble() < 0.40) {
                Material m = ctx.palette.accent(ctx.rnd);
                int ringEvery = 6 + rnd.nextInt(5);  // every 6..10
                for (int y = ringEvery; y < h; y += ringEvery) {
                    ringSegment(ctx, ox, y, oz, 3, m);
                }
            }
        }

        // -------------------------------------------------------------------
        // (3) INNER SIN/COS SHELLS: keep them, reduce spacing (denser shells)
        // -------------------------------------------------------------------
        for (int y = 8; y < h; y += 7 + rnd.nextInt(4)) {
            sinCosSphereShell(ctx, 0, y, 0, rx - 5, 0.18 + rnd.nextDouble() * 0.2, ctx.palette.secondary);
        }

        // -------------------------------------------------------
        // (4) ORBS with NESTED LOOPS: satellites around some orbs
        // -------------------------------------------------------
        int orbs = 18 + rnd.nextInt(10);
        for (int i = 0; i < orbs; i++) {
            int ox = rndRange(rnd, -(int)(rx * 0.7), (int)(rx * 0.7));
            int oy = rndRange(rnd, 8, h - 4);
            int oz = rndRange(rnd, -(int)(rz * 0.7), (int)(rz * 0.7));

            int mainR = 3 + rnd.nextInt(3);
            sphere(ctx, ox, oy, oz, mainR, ctx.palette.accent(rnd), HOLLOW_LARGE_VOLUMES);

            // ---- NEW: randomly add a micro-orbit of "satellite" spheres around the orb ----
            if (rnd.nextDouble() < 0.65) {
                int satellites = 3 + rnd.nextInt(4); // 3..6
                int ringRad    = 3 + rnd.nextInt(3); // 3..5 around the orb
                int satR       = 1 + rnd.nextInt(2); // tiny spheres

                for (int s = 0; s < satellites; s++) {
                    double a = (Math.PI * 2.0) * (s / (double) satellites);
                    int sx = ox + (int) Math.round(Math.cos(a) * ringRad);
                    int sz = oz + (int) Math.round(Math.sin(a) * ringRad);
                    int sy = oy + ((s % 2 == 0) ? 1 : -1);
                    sphere(ctx, sx, sy, sz, satR, (s % 2 == 0) ? ctx.palette.secondary : ctx.palette.accent(ctx.rnd), true);
                }
            }

            // 30%: add a vertical "chain" above the orb (looks like it’s tethered)
            if (rnd.nextDouble() < 0.30) {
                int chainLen = 6 + rnd.nextInt(10);
                for (int y = 1; y <= chainLen; y += 2) {
                    ctx.place(ox, oy + y, oz, ctx.palette.secondary);
                    if ((y % 4) == 0) ringSegment(ctx, ox, oy + y, oz, 2, ctx.palette.accent(ctx.rnd));
                }
            }
        }
    }



    /** Plan 2: Arches, triangle rooms, layered walkways. */
    private void plan_ArchesAndTriangles(StructureContext ctx, Random rnd) {
        Bukkit.getLogger().info("Planting Arches and Triangles");
        int rx = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(20)) * COMPACT);
        int rz = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(20)) * COMPACT);
        int h  = BASE_HEIGHT + rnd.nextInt(25);

        int floors = 4 + rnd.nextInt(3); // more floors
        for (int f = 0; f < floors; f++) {
            int y = 8 + f * (h / (floors + 1));
            int sz = rx / 2 + 8;
            for (int k = 0; k < DENSITY; k++) {
                triangleRoom(ctx, 0, y + k, 0, sz, ctx.palette.main1, ctx.palette.secondary);
            }
            ringBridge(ctx, 0, y + 2, 0, rx - 6, 3, ctx.palette.main2);
        }

        int arches = 8 + rnd.nextInt(6);
        for (int i = 0; i < arches; i++) {
            double ang = (Math.PI * 2.0) * (i / (double) arches);
            int ox = (int) Math.round(Math.cos(ang) * (rx * 0.7));
            int oz = (int) Math.round(Math.sin(ang) * (rz * 0.7));
            arch(ctx, ox, -6, oz, rx / 3, h / 2, ctx.palette.main2);
        }

        sierpinskiPyramid(ctx, 0, h / 2, 0, rx / 2, 2, ctx.palette.accent(rnd));
    }


    /** Plan 3: Orb weave (as ground platform) + nested spirals with shorter recursion. */
    private void plan_OrbWeaveNested(StructureContext ctx, Random rnd) {
        Bukkit.getLogger().info("plan_OrbWeaveNested (ground platform + half height)");

        // Halve overall height
        int rx = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(16)) * COMPACT);
        int rz = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(16)) * COMPACT);
        int h  = (BASE_HEIGHT + rnd.nextInt(50)) / 2; // cut height roughly in half

        // --- Ground platform ---
        // Put the orb weave at base level (y = 0 relative to origin).
        // Denser step (4) already; keep it as the "platform" look.
        orbWeave(ctx, 0, 0, 0, rx, 4, ctx.palette.altMain(0), ctx.palette.secondary);

        // Add a couple of thin ring-bands at ground to visually "seat" the platform.
        for (int r = rx - 6; r <= rx - 2; r += 2) {
            circleBand(ctx, 0, 0, 0, r, 2, ctx.palette.main1, true);
        }
        // Slight raised trim at y=1
        circleBand(ctx, 0, 1, 0, Math.max(6, (int) (rx * 0.8)), 1, ctx.palette.accent(ctx.rnd), true);

        // --- Nested spirals (use shorter height to match our halved profile) ---
        int spiralRadius = Math.max(4, rx / 3);
        int spiralHeight = Math.max(24, (int) (h * 0.9));  // keep them tall-ish but within new cap
        double spiralPitch = 0.12;

        spiralTower(ctx,  rx / 5, 0,  0, spiralRadius, spiralHeight, spiralPitch, ctx.palette.main1);
        spiralTower(ctx, -rx / 5, 0,  0, spiralRadius, spiralHeight, spiralPitch, ctx.palette.main2);
        spiralTower(ctx,  0,      0,  rz / 5, spiralRadius, spiralHeight, spiralPitch, ctx.palette.secondary);

        // --- Shorter recursion (smaller steps) ---
        // Reduce initial length and recursion depth so the vertical spread is visibly smaller.
        int branchLen = Math.max(7, rx / 3);   // was ~rx/2
        int branchDepth = 4;                   // was 4
        recursiveBranch(ctx, 0, spiralHeight / 2, 0, branchLen, branchDepth, ctx.palette.accent(rnd));
    }



    /** Plan 4: Fractal pyramids, bridges, radial towers. */
    private void plan_FractalPyramidsAndBridges(StructureContext ctx, Random rnd) {
        Bukkit.getLogger().info("Plan Fractal Pyramids and Bridges:");
        int rx = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(24)) * COMPACT);
        int rz = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(24)) * COMPACT);
        int h  = BASE_HEIGHT + rnd.nextInt(40);

        sierpinskiPyramid(ctx, 0, 0, 0, rx, 4, ctx.palette.main1); // +1 depth

        int towers = 8 + rnd.nextInt(6); // more towers
        for (int i = 0; i < towers; i++) {
            double ang = (Math.PI * 2.0) * (i / (double) towers);
            int ox = (int) Math.round(Math.cos(ang) * (rx * 0.6));
            int oz = (int) Math.round(Math.sin(ang) * (rz * 0.6));
            spiralTower(ctx, ox, 0, oz, rx / 5, h, 0.16, ctx.palette.secondary);
            ringBridge(ctx, ox, h / 2, oz, rx / 3, 3, ctx.palette.altMain(i));
        }

        dome(ctx, 0, h + 8, 0, rx / 2, ctx.palette.secondary, true);
    }


    /** Plan 5: Layered garden dome with wave canopies and ribs. */
    private void plan_MultiLayerGardenDome(StructureContext ctx, Random rnd) {
        Bukkit.getLogger().info("Plan MultiLayer Garden Dome");
        int rx = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(20)) * COMPACT);
        int rz = (int) Math.round((BASE_RADIUS_XZ + rnd.nextInt(20)) * COMPACT);
        int h  = BASE_HEIGHT + rnd.nextInt(30);

        for (int level = 0; level < h / 2; level += 3 + rnd.nextInt(4)) { // tighter step
            int rad = Math.max(8, rx - (level / 3));
            for (int k = 0; k < DENSITY; k++) {
                circleBand(ctx, 0, level + k, 0, rad, 3, ctx.palette.altMain(level + k), true);
            }
            waveCanopy(ctx, 0, level - 9, 0, rad - 2, 0.22, 4 + rnd.nextInt(3), ctx.palette.secondary);
        }

        int ribs = 16 + rnd.nextInt(10);
        for (int i = 0; i < ribs; i++) {
            double ang = (Math.PI * 2.0) * (i / (double) ribs);
            int ox = (int) Math.round(Math.cos(ang) * (rx * 0.5));
            int oz = (int) Math.round(Math.sin(ang) * (rz * 0.5));
            verticalRib(ctx, ox, 0, oz, h, ctx.palette.main1);
        }

        dome(ctx, 0, h, 0, Math.max(10, rx - 8), ctx.palette.main2, true);
        floatingOrbs(ctx, 0, h + 4, 0, rx / 3, 12, ctx.palette.accent(ctx.rnd)); // more orbs, closer
    }


    // ===========================
    // Low-level shapes & features
    // ===========================

    /** Fill a sphere (optionally hollow). */
    private void sphere(StructureContext ctx, int ox, int oy, int oz, int r, Material mat, boolean hollow) {
        int r2 = r * r;
        int ir = Math.max(0, r - 1);
        int ir2 = ir * ir;

        for (int x = -r; x <= r; x++) {
            int xx = x * x;
            for (int y = -r; y <= r; y++) {
                int yy = y * y;
                for (int z = -r; z <= r; z++) {
                    int zz = z * z;
                    int d2 = xx + yy + zz;
                    if (d2 <= r2) {
                        if (hollow && d2 < ir2) continue;
                        ctx.place(ox + x, oy + y, oz + z, mat);
                    }
                }
            }
        }
    }

    /** Build a dome (half-sphere). */
    private void dome(StructureContext ctx, int ox, int oy, int oz, int r, Material mat, boolean hollow) {
        int r2 = r * r;
        int ir = Math.max(0, r - 1);
        int ir2 = ir * ir;
        for (int x = -r; x <= r; x++) {
            int xx = x * x;
            for (int y = 0; y <= r; y++) {
                int yy = y * y;
                for (int z = -r; z <= r; z++) {
                    int zz = z * z;
                    int d2 = xx + yy + zz;
                    if (d2 <= r2) {
                        if (hollow && d2 < ir2) continue;
                        ctx.place(ox + x, oy + y, oz + z, mat);
                    }
                }
            }
        }
    }

    /** Thin circle band in XZ at given Y. thickness ~ t. */
    /** Thin circle band in XZ at given Y (denser). */
    private void circleBand(StructureContext ctx, int ox, int oy, int oz, int r, int t, Material mat, boolean ellipsize) {
        double a = r, b = ellipsize ? (r * 0.85) : r;
        int thickness = Math.max(1, t);
        double step = Math.PI / 512.0; // was /256 → denser
        for (double ang = 0.0; ang < Math.PI * 2.0; ang += step) {
            int x = (int) Math.round(Math.cos(ang) * a);
            int z = (int) Math.round(Math.sin(ang) * b);
            for (int dy = 0; dy < thickness + 1; dy++) { // +1 → slightly thicker
                ctx.place(ox + x, oy + dy, oz + z, mat);
            }
        }
    }

    /** Sine-decor on a ring (denser). */
    private void sinCosDecorRing(StructureContext ctx, int ox, int oy, int oz, int r, Material mat) {
        double step = Math.PI / 512.0; // denser
        for (double ang = 0.0; ang < Math.PI * 2.0; ang += step) {
            int x = (int) Math.round(Math.cos(ang) * r);
            int z = (int) Math.round(Math.sin(ang) * r);
            int y = oy + (int) Math.round(Math.sin(ang * 4.0) * 3.0);
            ctx.place(ox + x, y, oz + z, mat);
        }
    }

    /** Small ring segment for detailing around a point (denser). */
    private void ringSegment(StructureContext ctx, int cx, int cy, int cz, int r, Material mat) {
        double step = Math.PI / 16.0; // was /8 → more detail around each segment
        for (double ang = 0.0; ang < Math.PI * 2.0; ang += step) {
            int x = (int) Math.round(Math.cos(ang) * r);
            int z = (int) Math.round(Math.sin(ang) * r);
            ctx.place(cx + x, cy, cz + z, mat);
        }
    }


    /** Vertical rib with alternating blocks. */
    private void verticalRib(StructureContext ctx, int ox, int oy, int oz, int height, Material base) {
        for (int y = oy; y < oy + height; y++) {
            Material m = (y % 5 == 0) ? ctx.palette.secondary : base;
            ctx.place(ox, y, oz, m);
        }
    }

    /** A wavy vertical ribbon, sin-based. */
    private void waveRibbonVertical(StructureContext ctx, int ox, int oy, int oz, int height, double freq, Material mat) {
        int amp = 2; // tighter sway
        for (int y = 0; y < height; y++) {
            double ang = y * (freq * 1.15);
            int dx = (int) Math.round(Math.sin(ang) * amp);
            int dz = (int) Math.round(Math.cos(ang * 0.75) * amp);
            ctx.place(ox + dx, oy + y, oz + dz, mat);
            if (y % 5 == 0) ctx.place(ox + dx, oy + y, oz + dz + 1, ctx.palette.accent(ctx.rnd));
            if (y % 7 == 0) ctx.place(ox + dx + 1, oy + y, oz + dz, ctx.palette.secondary);
        }
    }

    /** Wave canopy (more spokes, smaller radius → compact). */
    private void waveCanopy(StructureContext ctx, int ox, int oy, int oz, int r, double freq, int amp, Material mat) {
        int rr = Math.max(4, (int) Math.round(r * COMPACT)); // shrink ring
        double step = Math.PI / 384.0; // denser spokes
        for (double ang = 0; ang < Math.PI * 2.0; ang += step) {
            int x = (int) Math.round(Math.cos(ang) * rr);
            int z = (int) Math.round(Math.sin(ang) * rr);
            int off = (int) Math.round(Math.sin(ang * freq * 24.0) * Math.max(2, amp - 1));
            lineVertical(ctx, ox + x, oy, oz + z, oy + off, mat, true);
        }
    }


    /** Spiral tower around Y. */
    private void spiralTower(StructureContext ctx, int ox, int oy, int oz, int radius, int height, double pitch, Material mat) {
        int r = Math.max(2, (int) Math.round(radius * COMPACT)); // shrink horizontally
        for (int y = 0; y < height; y++) {
            double ang = y * pitch;
            int x = (int) Math.round(Math.cos(ang) * r);
            int z = (int) Math.round(Math.sin(ang) * r);
            ctx.place(ox + x, oy + y, oz + z, mat);

            // Ornament more frequently and with smaller segments
            if (y % 2 == 0) {
                ringSegment(ctx, ox + x, oy + y, oz + z, 3, ctx.palette.altMain(y));
            }
            if (y % 6 == 0) {
                // micro struts inward to the axis to add density
                ctx.place(ox + (int) Math.signum(x) * Math.max(0, Math.abs(x) - 1), oy + y, oz + z, ctx.palette.secondary);
                ctx.place(ox + x, oy + y, oz + (int) Math.signum(z) * Math.max(0, Math.abs(z) - 1), ctx.palette.secondary);
            }
        }
        dome(ctx, ox, oy + height, oz, Math.max(4, r / 2), ctx.palette.secondary, true);
    }




    /** Draw a vertical line (with or without absolute). */
    private void lineVertical(StructureContext ctx, int x, int y1, int z, int y2, Material mat, boolean inclusive) {
        int a = Math.min(y1, y2);
        int b = Math.max(y1, y2);
        for (int y = a; y <= b; y++) {
            ctx.place(x, y, z, mat);
        }
        if (!inclusive) {
            // no-op currently; placeholder for future rules
        }
    }

    /** Sine/Cos based spherical shell banding. */
    private void sinCosSphereShell(StructureContext ctx, int ox, int oy, int oz, int radius, double freq, Material mat) {
        int r = radius;
        for (double a = 0; a < Math.PI; a += Math.PI / 160.0) {
            for (double b = 0; b < Math.PI * 2.0; b += Math.PI / 160.0) {
                double s = Math.sin(a);
                int x = (int) Math.round(Math.cos(b) * s * r);
                int y = (int) Math.round(Math.cos(a) * r);
                int z = (int) Math.round(Math.sin(b) * s * r);
                int off = (int) Math.round(Math.sin((a + b) * freq * 6.0) * 2.0);
                ctx.place(ox + x, oy + y + off, oz + z, mat);
            }
        }
    }

    /** Orbs lattice. */
    private void orbWeave(StructureContext ctx, int ox, int oy, int oz, int radius, int step, Material main, Material sec) {
        int rr = Math.max(10, (int) Math.round(radius * COMPACT));
        int s = Math.max(3, step - 2); // 6 → 4 (or 3) → denser
        for (int x = -rr; x <= rr; x += s) {
            for (int z = -rr; z <= rr; z += s) {
                int rad = Math.max(2, (int) Math.round(3 + (Math.abs(x) + Math.abs(z)) * 0.015));
                sphere(ctx, ox + x, oy, oz + z, rad, ((x + z) & 2) == 0 ? main : sec, HOLLOW_LARGE_VOLUMES);
            }
        }
    }


    /** Floating orbs sprinkled around. */
    private void floatingOrbs(StructureContext ctx, int ox, int oy, int oz, int radius, int count, Material mat) {
        for (int i = 0; i < count; i++) {
            double ang = (Math.PI * 2.0) * (i / (double) count);
            int r = (int) (radius * 0.6);
            int x = ox + (int) Math.round(Math.cos(ang) * r);
            int z = oz + (int) Math.round(Math.sin(ang) * r);
            int y = oy + (i % 2 == 0 ? 8 : -4);
            sphere(ctx, x, y, z, 4 + (i % 3), mat, true);
        }
    }

    /** A circular bridge path. */
    private void ringBridge(StructureContext ctx, int ox, int oy, int oz, int r, int width, Material mat) {
        for (int w = -width; w <= width; w++) {
            for (double ang = 0; ang < Math.PI * 2.0; ang += Math.PI / 256.0) {
                int x = (int) Math.round(Math.cos(ang) * (r + w));
                int z = (int) Math.round(Math.sin(ang) * (r + w));
                ctx.place(ox + x, oy, oz + z, mat);
            }
        }
    }

    /** True equilateral(ish) triangle room:
     *  - Proper triangular floor (centered at ox,oz)
     *  - Trimmed edge
     *  - Walls that get more interesting as they rise: shrinking outlines + small Sierpiński-style sub-triangles
     */
    private void triangleRoom(StructureContext ctx, int ox, int oy, int oz, int size, Material wall, Material trim) {
        // Define an equilateral-ish triangle on XZ:
        // base from (-size, 0) to (+size, 0), apex at (0, heightZ)
        int heightZ = (int) Math.round(size * 1.732); // ≈ sqrt(3) * size
        int ax = ox - size, az = oz;           // A
        int bx = ox + size, bz = oz;           // B
        int cx = ox,         cz = oz + heightZ; // C

        // --- Floor fill ---
        fillTriangleXZ(ctx, oy, ax, az, bx, bz, cx, cz, wall);

        // --- Edge trim just above floor ---
        outlineTriangleXZ(ctx, oy + 1, ax, az, bx, bz, cx, cz, trim);

        // --- Fancy walls: shrinking outlines + sub-triangle fractal accents ---
        int wallH = Math.max(4, size / 3); // overall wall height
        // centroid (for shrinking)
        double gx = (ax + bx + cx) / 3.0;
        double gz = (az + bz + cz) / 3.0;

        for (int h = 1; h <= wallH; h++) {
            double t = (h / (double) wallH);      // 0..1 up the wall
            double shrink = 1.0 - 0.45 * t;       // shrink toward centroid as y rises

            // Shrink each vertex toward centroid
            int axh = (int) Math.round(lerp(ax, gx, 1.0 - shrink));
            int ayh = oy + h;
            int azh = (int) Math.round(lerp(az, gz, 1.0 - shrink));

            int bxh = (int) Math.round(lerp(bx, gx, 1.0 - shrink));
            int byh = oy + h;
            int bzh = (int) Math.round(lerp(bz, gz, 1.0 - shrink));

            int cxh = (int) Math.round(lerp(cx, gx, 1.0 - shrink));
            int cyh = oy + h;
            int czh = (int) Math.round(lerp(cz, gz, 1.0 - shrink));

            // Alternate wall & trim for the shrinking outline
            Material ringMat = (h % 2 == 0) ? wall : trim;
            outlineTriangleXZ(ctx, ayh, axh, azh, bxh, bzh, cxh, czh, ringMat);

            // Every few layers, draw inner sub-triangles (Sierpiński-style) for extra interest
            if (h % 3 == 0) {
                // Depth 1–2 fractal depending on size; alternate materials to pop the pattern
                int depth = (size > 28) ? 2 : 1;
                Material m1 = (h % 6 == 0) ? trim : wall;
                Material m2 = (m1 == trim) ? wall : trim;
                sierpinskiOutlineLayerXZ(ctx, ayh, axh, azh, bxh, bzh, cxh, czh, depth, m1, m2);
            }
        }
    }

    // ---- small math utils ----
    private double lerp(double a, double b, double t) { return a + (b - a) * t; }

    // ---- triangle fill & outlines on XZ-plane ----
    private void fillTriangleXZ(StructureContext ctx, int y, int ax, int az, int bx, int bz, int cx, int cz, Material mat) {
        int minX = Math.min(ax, Math.min(bx, cx));
        int maxX = Math.max(ax, Math.max(bx, cx));
        int minZ = Math.min(az, Math.min(bz, cz));
        int maxZ = Math.max(az, Math.max(bz, cz));

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (pointInTriangle(x, z, ax, az, bx, bz, cx, cz)) {
                    ctx.place(x, y, z, mat);
                }
            }
        }
    }

    private void outlineTriangleXZ(StructureContext ctx, int y, int ax, int az, int bx, int bz, int cx, int cz, Material mat) {
        lineXZ(ctx, y, ax, az, bx, bz, mat);
        lineXZ(ctx, y, bx, bz, cx, cz, mat);
        lineXZ(ctx, y, cx, cz, ax, az, mat);
    }

    private boolean pointInTriangle(int px, int pz, int ax, int az, int bx, int bz, int cx, int cz) {
        // Barycentric sign tests
        boolean b1 = sign(px, pz, ax, az, bx, bz) < 0.0;
        boolean b2 = sign(px, pz, bx, bz, cx, cz) < 0.0;
        boolean b3 = sign(px, pz, cx, cz, ax, az) < 0.0;
        return (b1 == b2) && (b2 == b3);
    }

    private double sign(int px, int pz, int x1, int z1, int x2, int z2) {
        return (px - x2) * (z1 - z2) - (x1 - x2) * (pz - z2);
    }

    // Simple integer Bresenham line on XZ at fixed Y
    private void lineXZ(StructureContext ctx, int y, int x1, int z1, int x2, int z2, Material mat) {
        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dz = Math.abs(z2 - z1), sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;

        int x = x1, z = z1;
        while (true) {
            ctx.place(x, y, z, mat);
            if (x == x2 && z == z2) break;
            int e2 = 2 * err;
            if (e2 > -dz) { err -= dz; x += sx; }
            if (e2 <  dx) { err += dx; z += sz; }
        }
    }

    // ---- fractal sub-triangle outlines (Sierpiński-like) ----
    private void sierpinskiOutlineLayerXZ(StructureContext ctx, int y,
                                          int ax, int az, int bx, int bz, int cx, int cz,
                                          int depth, Material m1, Material m2) {
        if (depth <= 0) {
            // Base: outline the triangle with m1, then add a smaller inner trim with m2
            outlineTriangleXZ(ctx, y, ax, az, bx, bz, cx, cz, m1);

            // Draw a slightly inset outline to add richness (midpoints nudged toward centroid)
            double gx = (ax + bx + cx) / 3.0;
            double gz = (az + bz + cz) / 3.0;
            int iax = (int) Math.round(lerp(ax, gx, 0.15));
            int iaz = (int) Math.round(lerp(az, gz, 0.15));
            int ibx = (int) Math.round(lerp(bx, gx, 0.15));
            int ibz = (int) Math.round(lerp(bz, gz, 0.15));
            int icx = (int) Math.round(lerp(cx, gx, 0.15));
            int icz = (int) Math.round(lerp(cz, gz, 0.15));
            outlineTriangleXZ(ctx, y, iax, iaz, ibx, ibz, icx, icz, m2);
            return;
        }

        // Midpoints of edges
        int abx = (ax + bx) >> 1, abz = (az + bz) >> 1;
        int bcx = (bx + cx) >> 1, bcz = (bz + cz) >> 1;
        int cax = (cx + ax) >> 1, caz = (cz + az) >> 1;

        // Recurse into the 3 corner triangles (classic Sierpiński layout)
        sierpinskiOutlineLayerXZ(ctx, y, ax, az, abx, abz, cax, caz, depth - 1, m1, m2);
        sierpinskiOutlineLayerXZ(ctx, y, abx, abz, bx, bz, bcx, bcz, depth - 1, m1, m2);
        sierpinskiOutlineLayerXZ(ctx, y, cax, caz, bcx, bcz, cx, cz, depth - 1, m1, m2);

        // Optional: faint outline for the central inverted triangle to emphasize the pattern
        if (depth == 1) {
            outlineTriangleXZ(ctx, y, abx, abz, bcx, bcz, cax, caz, m2);
        }
    }



    /** Simple arch from a base point. */
    private void arch(StructureContext ctx, int ox, int oy, int oz, int span, int height, Material mat) {
        for (int x = -span; x <= span; x++) {
            double t = (x / (double) span);
            int y = oy + (int) Math.round(Math.sin((1 - Math.abs(t)) * Math.PI) * height);
            ctx.place(ox + x, y, oz, mat);
            // thicken
            ctx.place(ox + x, y - 1, oz, mat);
        }
    }

    /** Sierpinski-like pyramid (recursive). */
    private void sierpinskiPyramid(StructureContext ctx, int ox, int oy, int oz, int size, int depth, Material mat) {
        if (depth <= 0 || size <= 2) {
            solidPyramid(ctx, ox, oy, oz, size, mat, HOLLOW_LARGE_VOLUMES);
            return;
        }
        int half = size / 2;
        sierpinskiPyramid(ctx, ox, oy, oz, half, depth - 1, mat);
        sierpinskiPyramid(ctx, ox + half, oy, oz, half, depth - 1, mat);
        sierpinskiPyramid(ctx, ox, oy, oz + half, half, depth - 1, mat);
        sierpinskiPyramid(ctx, ox + half, oy, oz + half, half, depth - 1, mat);
        // apex
        sierpinskiPyramid(ctx, ox + half / 2, oy + half, oz + half / 2, half, depth - 1, mat);
    }

    /** Solid (or hollow) pyramid. */
    private void solidPyramid(StructureContext ctx, int ox, int oy, int oz, int size, Material mat, boolean hollow) {
        for (int y = 0; y <= size; y++) {
            int edge = size - y;
            for (int x = -edge; x <= edge; x++) {
                for (int z = -edge; z <= edge; z++) {
                    if (hollow) {
                        if (x == -edge || x == edge || z == -edge || z == edge) {
                            ctx.place(ox + x, oy + y, oz + z, mat);
                        }
                    } else {
                        ctx.place(ox + x, oy + y, oz + z, mat);
                    }
                }
            }
        }
    }

    /** Recursively branching ribs. */
    private void recursiveBranch(StructureContext ctx, int ox, int oy, int oz, int len, int depth, Material mat) {
        if (depth <= 0 || len <= 0) return;
        // main arm
        for (int i = 0; i < len; i++) {
            ctx.place(ox + i, oy + i / 2, oz, mat);
        }
        // branches
        recursiveBranch(ctx, ox + len / 2, oy + len / 3, oz + len / 4, len / 2, depth - 1, mat);
        recursiveBranch(ctx, ox + len / 2, oy + len / 3, oz - len / 4, len / 2, depth - 1, mat);
    }

    // ===========================
    // Utilities
    // ===========================

    private int rndRange(Random rnd, int a, int b) {
        if (a > b) { int t = a; a = b; b = t; }
        return a + rnd.nextInt(b - a + 1);
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Safe tan-based shift: tan(freq * y) * amp, clamped to +/-amp. */
    private int tanShift(double freq, int y, int amp) {
        double t = Math.tan(freq * y);
        // Avoid infinities: clamp tan output to [-1, 1] before scaling, then clamp to [-amp, amp]
        double safe = Math.max(-1.0, Math.min(1.0, t));
        int shift = (int) Math.round(safe * amp);
        return clamp(shift, -amp, amp);
    }


    // ============================================
    // End of class
    // ============================================
}
