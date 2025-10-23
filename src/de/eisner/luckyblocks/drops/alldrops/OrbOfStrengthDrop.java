package de.eisner.luckyblocks.drops.alldrops;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OrbOfStrengthDrop extends Drop implements Listener {
   private static final NamespacedKey KEY;

   // Effect tuning
   private static final int DURATION_TICKS = 20 * 10;   // 10 seconds
   private static final int RADIUS = 4;                  // horizontal radius
   private static final int HEIGHT_ABOVE = 4;            // how high above yBase to shatter
   private static final int MAX_BLOCKS_PER_TICK = 120;   // perf cap per player per tick
   // --- Aura tuning (zig-zag) ---
   private static final double AURA_RADIUS = 0.48;          // close to player hitbox
   private static final double AURA_Y_MIN = 0.20;           // above feet
   private static final double AURA_Y_MAX = 1.80;           // below head
   private static final int    AURA_SEGMENTS = 10;          // vertical steps per bolt
   private static final int    AURA_BOLTS = 4;              // how many vertical bolts around the body

   private static final double ZIG_LATERAL = 0.18;          // side offset per segment (zig-zag amplitude)
   private static final double ZIG_FORWARD = 0.10;          // forward/back offset (adds kink)
   private static final int    ZIG_SPEED_TICKS = 3;         // animation speed (lower = faster)

   private static final double BRANCH_CHANCE = 0.35;        // per segment
   private static final double BRANCH_LEN = 0.35;           // length of side-branch
   private static final int    BRANCH_POINTS = 3;           // particles along a branch

   private static final double AURA_FACE_CONE_DEG = 55.0;   // don't spawn directly in front of face
   private static final int    AURA_RANDOM_SPARKS = 6;      // extra crackle per tick

   private static final Random rngdingens = new Random();


   // Active players: player UUID -> yBase (rememberedY - 1)
   private static final Map<UUID, Integer> ACTIVE = new ConcurrentHashMap<>();
   private static volatile boolean GLOBAL_TASK_STARTED = false;

   public OrbOfStrengthDrop() {
      super("Orb of Strength",
              Arrays.asList("Drops an orb that allows the player to destroy blocks by walking for 10 seconds."),
              DropType.GOOD, 0L);
      Bukkit.getPluginManager().registerEvents(this, Main.plugin);
      startGlobalShatterLoop();
   }

   @Override
   public void execute(Player p, Location loc) {
      ItemStack item = new ItemStack(Material.MAGMA_CUBE_SPAWN_EGG);
      ItemMeta meta = item.getItemMeta();

      meta.setDisplayName(ChatColor.RED + this.getName());
      meta.setUnbreakable(true);

      // Mark this as our orb
      meta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);

      // Lore: orange "Consumable" + yellow description
      List<String> lore = new ArrayList<>();
      lore.add(ChatColor.GOLD + "Consumable");
      lore.add(ChatColor.YELLOW + "Right-click to gain Strength I for 10s.");
      lore.add(ChatColor.YELLOW + "While active, walking shatters blocks around you.");
      meta.setLore(lore);

      item.setItemMeta(meta);
      loc.getWorld().dropItemNaturally(loc, item);
   }

   @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
   public void onUse(PlayerInteractEvent e) {
      // Only main-hand to avoid double firing
      if (e.getHand() != EquipmentSlot.HAND) return;

      Action a = e.getAction();
      if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

      ItemStack item = e.getItem();
      if (item == null) return;

      ItemMeta meta = item.getItemMeta();
      if (meta == null) return;

      if (!meta.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE)) return;

      Player p = e.getPlayer();
      e.setUseItemInHand(Event.Result.DENY);
      e.setUseInteractedBlock(Event.Result.DENY);
      e.setCancelled(true);

      // Consume ONE orb
      if (item.getAmount() > 1) {
         item.setAmount(item.getAmount() - 1);
      } else {
         // remove the single item from hand
         p.getInventory().setItemInMainHand(null);
      }

      // Apply Strength I (amplifier 0) for 10s
      p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, DURATION_TICKS, 0, true, true, true));

      // Remember yBase = floor(Y) - 1 at activation time
      int yBase = (int) Math.floor(p.getLocation().getY());
      ACTIVE.put(p.getUniqueId(), yBase);

      // Schedule removal after duration
      new BukkitRunnable() {
         @Override public void run() {
            ACTIVE.remove(p.getUniqueId());
         }
      }.runTaskLater(Main.plugin, DURATION_TICKS);
   }

   // Global loop that runs every tick and shatters blocks for any active players
   private void startGlobalShatterLoop() {
      if (GLOBAL_TASK_STARTED) return;
      GLOBAL_TASK_STARTED = true;

      new BukkitRunnable() {
         @Override
         public void run() {
            if (ACTIVE.isEmpty()) return;

            for (UUID uuid : new ArrayList<>(ACTIVE.keySet())) {
               Player p = Bukkit.getPlayer(uuid);
               if (p == null || !p.isOnline()) { ACTIVE.remove(uuid); continue; }

               int yBase = currentTopYBase(p); // ← recompute every tick
               shatterAroundPlayer(p, yBase);
               renderAura(p);
            }
         }
      }.runTaskTimer(Main.plugin, 1L, 1L);

   }

   private void shatterAroundPlayer(Player p, int yBase) {
      Location ploc = p.getLocation();
      World w = ploc.getWorld();
      if (w == null) return;

      int px = ploc.getBlockX();
      int pz = ploc.getBlockZ();

      int processed = 0;
      int r2 = RADIUS * RADIUS;

      // We’ll sweep from yBase upward to yBase + HEIGHT_ABOVE
      for (int y = yBase; y <= yBase + HEIGHT_ABOVE; y++) {
         for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
               if (dx * dx + dz * dz > r2) continue; // circle, not square
               Block b = w.getBlockAt(px + dx, y, pz + dz);
               if (!isBreakable(b)) continue;

               // Turn the block into a falling block “flying away in an arc”
               launchAsFallingBlock(b, ploc);

               processed++;
               if (processed >= MAX_BLOCKS_PER_TICK) return; // perf cap
            }
         }
      }
   }

   private boolean isBreakable(Block b) {
      Material m = b.getType();
      if (m.isAir()) return false;
      if (m == Material.WATER || m == Material.LAVA) return false;

      // skip indestructibles / portals / commands
      switch (m) {
         case BEDROCK:
         case BARRIER:
         case END_PORTAL:
         case END_PORTAL_FRAME:
         case NETHER_PORTAL:
         case COMMAND_BLOCK:
         case CHAIN_COMMAND_BLOCK:
         case REPEATING_COMMAND_BLOCK:
            return false;
         default:
      }

      // avoid tile entities/containers to be safe
      BlockState state = b.getState();
      if (state instanceof Container) return false;

      return true;
   }

   private void launchAsFallingBlock(Block b, Location playerLoc) {
      World w = b.getWorld();
      // Capture block data then clear the original
      var data = b.getBlockData();
      Location spawn = b.getLocation().add(0.5, 0.5, 0.5);
      b.setType(Material.AIR, false);

      FallingBlock fb;
      try {
         fb = w.spawnFallingBlock(spawn, data);
      } catch (IllegalArgumentException ex) {
         // Some blocks cannot be turned into FallingBlock (just drop item instead)
         w.dropItemNaturally(spawn, new ItemStack(data.getMaterial()));
         return;
      }

      fb.setDropItem(true);
      fb.setHurtEntities(false);
      fb.setGravity(true);

      // Outward direction from player to block center (horizontal), with some randomness
      Vector out = spawn.toVector().subtract(playerLoc.toVector());
      out.setY(0);
      if (out.lengthSquared() < 0.0001) {
         // If the block is exactly under the player, push in their facing direction
         out = playerLoc.getDirection();
         out.setY(0);
      }
      out.normalize();

      double horizSpeed = 0.35 + Math.random() * 0.65;  // 0.35 - 0.70
      double upSpeed    = 0.45 + Math.random() * 0.35;  // 0.45 - 0.80

      // Add a tiny sideways wobble for a nicer arc
      Vector right = new Vector(-out.getZ(), 0, out.getX()); // perpendicular
      double wobble = (Math.random() - 0.5) * 0.3;            // -0.15..0.15

      Vector vel = out.multiply(horizSpeed).add(right.multiply(wobble));
      vel.setY(upSpeed);

      fb.setVelocity(vel);
   }

   private int currentTopYBase(Player p) {
      World w = p.getWorld();
      int x = p.getLocation().getBlockX();
      int z = p.getLocation().getBlockZ();
      //return w.getHighestBlockYAt(HeightMap.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;

      return p.getLocation().getBlockY();
   }

   private void renderAura(Player p) {
      World w = p.getWorld();
      if (w == null) return;

      // Bases & orientation
      Location base = p.getLocation();            // feet
      Location eye  = p.getEyeLocation();
      double yFeet  = base.getY();
      double yMin   = yFeet + AURA_Y_MIN;
      double yMax   = yFeet + AURA_Y_MAX;

      Vector fwd = base.getDirection();
      fwd.setY(0);
      if (fwd.lengthSquared() < 1e-6) fwd = new Vector(0, 0, 1);
      fwd.normalize();
      Vector right = new Vector(-fwd.getZ(), 0, fwd.getX());

      double faceCos = Math.cos(Math.toRadians(AURA_FACE_CONE_DEG));
      Particle particle = Particle.CRIT;
      Random r = rngdingens;

      // Animate zig-zag sign flip over time
      int phase = (p.getTicksLived() / ZIG_SPEED_TICKS) & 1;

      // Build N vertical bolts around the ring, each with zig-zagging lateral/forward offsets
      for (int b = 0; b < AURA_BOLTS; b++) {
         double ang = (Math.PI * 2.0 * b) / AURA_BOLTS;                   // ring angle
         Vector radial = right.clone().multiply(Math.cos(ang)).add(
                 fwd.clone().multiply(Math.sin(ang)));            // out from body
         Vector tangent = new Vector(-radial.getZ(), 0, radial.getX());  // sideways to radial

         for (int i = 0; i < AURA_SEGMENTS; i++) {
            double t = (double) i / (AURA_SEGMENTS - 1);
            double y = yMin + (yMax - yMin) * t;

            // Sharp zig-zag: alternate side each segment (+ randomness)
            int sign = (((i + phase) & 1) == 0) ? 1 : -1;
            double side = (ZIG_LATERAL + (r.nextDouble() - 0.5) * 0.06) * sign;
            double fwdKink = (ZIG_FORWARD + (r.nextDouble() - 0.5) * 0.04) * -sign; // alternate opposite for kink

            Vector pos = base.toVector()
                    .add(radial.clone().multiply(AURA_RADIUS))
                    .add(tangent.clone().multiply(side))
                    .add(radial.clone().multiply(fwdKink))  // small push along radial for “broken” look
                    .add(new Vector(0, y - yFeet, 0));

            // Face cull near head
            Vector toPoint = pos.clone().subtract(eye.toVector());
            double lenSq = toPoint.lengthSquared();
            if (lenSq > 1e-9) {
               Vector dir = toPoint.multiply(1.0 / Math.sqrt(lenSq));
               if (dir.dot(eye.getDirection()) > faceCos && y > (yFeet + 1.2)) continue;
            }

            // Main bolt particle(s)
            w.spawnParticle(particle, pos.getX(), y, pos.getZ(), 1, 0, 0, 0, 0);

            // Occasional side-branch (short, angled burst)
            if (r.nextDouble() < BRANCH_CHANCE && i < AURA_SEGMENTS - 1) {
               Vector branchDir = tangent.clone().multiply(sign).add(new Vector(0, 0.3, 0)).normalize();
               for (int j = 1; j <= BRANCH_POINTS; j++) {
                  double f = (double) j / BRANCH_POINTS;
                  Vector bp = pos.clone().add(branchDir.clone().multiply(BRANCH_LEN * f));
                  w.spawnParticle(particle, bp.getX(), y + 0.02 * j, bp.getZ(), 1, 0, 0, 0, 0);
               }
            }
         }
      }

      // Extra random sparks hugging the ring to avoid “just lines”
      for (int k = 0; k < AURA_RANDOM_SPARKS; k++) {
         double ang = r.nextDouble() * Math.PI * 2.0;
         double rad = AURA_RADIUS + (r.nextDouble() - 0.5) * 0.06;
         double y = yMin + r.nextDouble() * (yMax - yMin);

         Vector ring = right.clone().multiply(Math.cos(ang) * rad)
                 .add(fwd.clone().multiply(Math.sin(ang) * rad));
         Vector pos = base.toVector().add(ring).add(new Vector(0, y - yFeet, 0));

         // face cull high points
         Vector toPoint = pos.clone().subtract(eye.toVector());
         double lenSq = toPoint.lengthSquared();
         if (lenSq > 1e-9) {
            Vector dir = toPoint.multiply(1.0 / Math.sqrt(lenSq));
            if (dir.dot(eye.getDirection()) > faceCos && y > (yFeet + 1.2)) continue;
         }

         w.spawnParticle(particle, pos.getX(), y, pos.getZ(), 1, 0, 0, 0, 0);
      }
   }




   static {
      KEY = new NamespacedKey(Main.plugin, "orb_of_strength");
   }
}
