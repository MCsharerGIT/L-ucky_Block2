package de.eisner.luckyblocks.drops.alldrops;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishCannonDrop extends Drop implements Listener {
   private static final NamespacedKey KEY;

   // === Config knobs ===
   private static final int VOLLEY_COUNT = 14;          // fish per shot
   private static final float SPREAD_DEGREES = 30f;    // ± spread
   private static final int MAX_BOUNCES = 9;           // per fish
   private static final double DECAY = 0.80;           // bounce decay
   private static final double CONTACT_DAMAGE = 1.0;   // 0.5 heart
   private static final int CONTACT_RATE_TICKS = 5;   // 1 second
   private static final double CONTACT_AABB = 0.5;     // “touch” radius
   private static final int FAILSAFE_SECONDS = 15;     // per-fish bounce task cap
   // Extra cushion for contact checks (the “puffer” effect)
   private static final double PUFFER_EXPAND_XZ = 0.35;
   private static final double PUFFER_EXPAND_Y  = 0.25;
   private static final double TARGET_EXPAND    = 0.10; // slight padding for targets


   // Track dangerous fish and their owners (player who fired)
   private static final Map<UUID, UUID> DANGEROUS_FISH_OWNERS = new ConcurrentHashMap<>();
   private static volatile boolean CONTACT_TASK_STARTED = false;

   private final Random rng = new Random();

   public FishCannonDrop() {
      super("Fish Cannon", Collections.singletonList("Drops a fish cannon"), DropType.GOOD, 0L);
      Bukkit.getPluginManager().registerEvents(this, Main.plugin);
      startGlobalContactTask();
   }

   @Override
   public void execute(Player p, Location loc) {
      ItemStack item = new ItemStack(Material.TROPICAL_FISH);
      ItemMeta meta = item.getItemMeta();

      ChatColor color = ChatColor.DARK_BLUE;
      meta.setDisplayName(color + this.getName());

      // Lore/instructions (yellow text)
      meta.setLore(Collections.singletonList(
              ChatColor.YELLOW + "Right click to launch fish at your enemies."));

      // Slight movement speed debuff while held (mainhand or offhand)
      meta.addAttributeModifier(
              Attribute.GENERIC_MOVEMENT_SPEED,
              new AttributeModifier(KEY, -0.05, Operation.ADD_SCALAR, EquipmentSlotGroup.HAND)
      );

      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
      meta.setUnbreakable(true);

      // Tag this item so only the cannon triggers the effect
      meta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);

      item.setItemMeta(meta);
      loc.getWorld().dropItemNaturally(loc, item);
   }

   @EventHandler(ignoreCancelled = false)
   public void onUse(PlayerInteractEvent e) {
      Action a = e.getAction();
      if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

      ItemStack item = e.getItem();
      if (item == null) return;

      ItemMeta meta = item.getItemMeta();
      if (meta == null) return;

      // Only trigger if it's our Fish Cannon
      if (!meta.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE)) return;

      Player p = e.getPlayer();

      // Integrated Minecraft cooldown (10s = 200 ticks)
      if (p.hasCooldown(item.getType())) {
         p.spigot().sendMessage(
                 ChatMessageType.ACTION_BAR,
                 new TextComponent(ChatColor.RED + "Fish Cannon is recharging!")
         );
         return;
      }

      e.setCancelled(true); // prevent eating/using the fish item
      e.setUseItemInHand(Event.Result.DENY);
      e.setUseInteractedBlock(Event.Result.DENY);
      p.setCooldown(item.getType(), 200);

      // Fire a fish volley with spread
      fireVolley(p);
   }

   private void fireVolley(Player p) {
      Location eye = p.getEyeLocation();
      for (int i = 0; i < VOLLEY_COUNT; i++) {
         // Random yaw/pitch within ±SPREAD_DEGREES
         float yawOffset = (rng.nextFloat() * 2f - 1f) * SPREAD_DEGREES;
         float pitchOffset = (rng.nextFloat() * 2f - 1f) * SPREAD_DEGREES;

         Location shotLoc = eye.clone();
         shotLoc.setYaw(shotLoc.getYaw() + yawOffset);
         shotLoc.setPitch(clampPitch(shotLoc.getPitch() + pitchOffset));

         Vector dir = shotLoc.getDirection().normalize();

         // Slight velocity variation for a nicer spray feel
         double speed = 0.7 + rng.nextDouble() * 0.4; // 0.7 - 1.1
         Vector initial = dir.clone().multiply(speed);
         initial.setY(initial.getY() + 0.35);

         spawnFishProjectile(p, p.getWorld(), shotLoc, dir, initial);
      }
   }

   private void spawnFishProjectile(Player owner, World world, Location spawnLoc, Vector dir, Vector initial) {
      EntityType type = pickFishType(rng);
      LivingEntity fish = (LivingEntity) world.spawnEntity(spawnLoc, type);

      // Name tag (red) instead of scoreboard tags
      fish.setCustomName(ChatColor.RED + "Dangerous Fish");
      fish.setCustomNameVisible(true);

      // Double HP for survivability (applies to any fish)
      AttributeInstance max = fish.getAttribute(Attribute.GENERIC_MAX_HEALTH);
      if (max != null) {
         max.setBaseValue(max.getBaseValue() * 2.0);
         fish.setHealth(max.getBaseValue());
      }

      fish.setRemoveWhenFarAway(false);
      fish.setVelocity(initial);

      // Track ownership for kill credit & contact damage
      DANGEROUS_FISH_OWNERS.put(fish.getUniqueId(), owner.getUniqueId());

      // Per-fish bounce runnable (contact damage handled globally)
      new BukkitRunnable() {
         int bounces = 0;
         boolean wasOnGround = false;
         int ticks = 0;

         final Vector baseDir = dir.clone().normalize();
         final double baseHoriz = 0.5;
         final double baseVert = 0.35;

         @Override
         public void run() {
            if (!fish.isValid() || fish.isDead()) { cancel(); return; }
            ticks++;

            boolean onGround = fish.isOnGround();
            if (onGround && !wasOnGround) {
               if (bounces < MAX_BOUNCES) {
                  bounces++;
                  double factor = Math.pow(DECAY, bounces - 1);
                  Vector bounce = baseDir.clone().multiply(baseHoriz * factor);
                  bounce.setY(baseVert * factor);
                  fish.setVelocity(bounce);
               } else {
                  cancel();
                  return;
               }
            }
            wasOnGround = onGround;

            if (ticks > 20 * FAILSAFE_SECONDS) cancel();
         }
      }.runTaskTimer(Main.plugin, 1L, 1L);
   }

   // Global contact-damage “event-like” loop (runs once per second, independent of bounce logic)
   private void startGlobalContactTask() {
      if (CONTACT_TASK_STARTED) return;
      CONTACT_TASK_STARTED = true;

      new BukkitRunnable() {
         @Override
         public void run() {
            if (DANGEROUS_FISH_OWNERS.isEmpty()) return;

            Iterator<Map.Entry<UUID, UUID>> it = DANGEROUS_FISH_OWNERS.entrySet().iterator();
            while (it.hasNext()) {
               Map.Entry<UUID, UUID> entry = it.next();
               UUID fishId = entry.getKey();
               UUID ownerId = entry.getValue();

               Entity e = Bukkit.getEntity(fishId);
               if (!(e instanceof LivingEntity fish) || !fish.isValid() || fish.isDead()) {
                  it.remove();
                  continue;
               }

               applyContactDamageTick(fish, ownerId);
            }
         }
      }.runTaskTimer(Main.plugin, CONTACT_RATE_TICKS, CONTACT_RATE_TICKS);
   }

   private void applyContactDamageTick(LivingEntity fish, UUID ownerId) {
      BoundingBox baseBox;
      try {
         baseBox = fish.getBoundingBox();
      } catch (NoSuchMethodError err) {
         baseBox = null; // fallback path will use distance check
      }

      // Widen the search radius to match the “puffer” expansion
      final double scan = Math.max(CONTACT_AABB, Math.max(PUFFER_EXPAND_XZ, PUFFER_EXPAND_Y)) + CONTACT_AABB;
      Collection<Entity> nearby = fish.getNearbyEntities(scan, scan, scan);
      if (nearby.isEmpty()) return;

      Player owner = ownerId != null ? Bukkit.getPlayer(ownerId) : null;
      Entity damageSource = (owner != null && owner.isOnline()) ? owner : fish;

      // Precompute the expanded (“puffed”) fish box if available
      final BoundingBox puffedFishBox =
              (baseBox != null) ? baseBox.clone().expand(PUFFER_EXPAND_XZ, PUFFER_EXPAND_Y, PUFFER_EXPAND_XZ) : null;

      final double distSqFallback = scan * scan;

      for (Entity e : nearby) {
         if (!(e instanceof LivingEntity target)) continue;
         if (isFish(target.getType())) continue; // don’t hurt other fish

         boolean touching;

         if (puffedFishBox != null) {
            try {
               BoundingBox targetBox = target.getBoundingBox().clone().expand(TARGET_EXPAND);
               touching = puffedFishBox.overlaps(targetBox);
            } catch (NoSuchMethodError err) {
               // Older API fallback
               touching = fish.getLocation().distanceSquared(target.getLocation()) <= distSqFallback;
            }
         } else {
            // No bounding boxes available: distance-only cushion
            touching = fish.getLocation().distanceSquared(target.getLocation()) <= distSqFallback;
         }

         if (touching) {
            // Attribute damage (and kill credit) to the owner when possible
            target.damage(CONTACT_DAMAGE, damageSource);
         }
      }
   }


   @EventHandler
   public void onFishDeath(EntityDeathEvent e) {
      // Clean up map as soon as a dangerous fish dies
      DANGEROUS_FISH_OWNERS.remove(e.getEntity().getUniqueId());
   }

   // --- helpers ---

   private static boolean isFish(EntityType t) {
      return t == EntityType.COD
              || t == EntityType.SALMON
              || t == EntityType.PUFFERFISH
              || t == EntityType.TROPICAL_FISH;
   }

   private EntityType pickFishType(Random r) {
      // Weighted: mostly cod
      double x = r.nextDouble();
      if (x < 0.30) return EntityType.COD;          // 30%
      if (x < 0.60) return EntityType.SALMON;       // 25%
      if (x < 0.70) return EntityType.PUFFERFISH;   // 10%
      return EntityType.TROPICAL_FISH;              // 35%
   }

   private static float clampPitch(float pitch) {
      if (pitch > 89f) return 89f;
      if (pitch < -89f) return -89f;
      return pitch;
   }

   static {
      KEY = new NamespacedKey(Main.plugin, "fishcannon");
   }
}
