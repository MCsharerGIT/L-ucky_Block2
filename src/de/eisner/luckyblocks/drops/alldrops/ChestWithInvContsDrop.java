package de.eisner.luckyblocks.drops.alldrops;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.block.Chest;

import java.util.Arrays;

public class ChestWithInvContsDrop extends Drop {

    public ChestWithInvContsDrop() {
        super(
                "ChestWithInvConts",
                Arrays.asList("Spawns a chest filled with one item the player already carries."),
                DropType.GOOD,
                1L
        );
    }

    @Override
    public void execute(Player p, Location loc) {
        final World world = loc.getWorld();
        if (world == null) return;

        // 1) Pick an item from the player's inventory
        ItemStack chosen = pickRandomPlayerItem(p.getInventory());
        if (chosen == null) {
            p.sendMessage("§7You don't have any items you prat!");
            return;
        }

        // Normalize the amount to a sane, stackable range
        int max = chosen.getMaxStackSize();
        ItemStack fillStack = chosen.clone();
        fillStack.setAmount(Math.min(Math.max(chosen.getAmount(), 1), max));

        // 2) Place a single chest at the target block position
        Block target = loc.getBlock();
        target.setType(Material.CHEST, false);

        // Face the chest toward the player, if possible
        BlockData data = target.getBlockData();
        if (data instanceof Directional dir) {
            dir.setFacing(yawToFace(p.getLocation().getYaw()));
            target.setBlockData(data, false);
        }

        // 3) Fill the chest inventory with the chosen item
        BlockState state = target.getState();
        if (state instanceof Chest chest) {
            Inventory inv = chest.getBlockInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, fillStack.clone());
            }
            chest.update(true, false);
        }

        // 4) Little feedback
        world.playSound(target.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.1f);
    }

    // ---- helpers ----

    /** Prefer main hand; else first non-empty slot; returns null if nothing usable. */
    private ItemStack pickPlayerItem(PlayerInventory inv) {
        ItemStack hand = inv.getItemInMainHand();
        if (isUsable(hand)) return hand;

        for (ItemStack it : inv.getContents()) {
            if (isUsable(it)) return it;
        }
        // Optionally also check offhand/armor if you want:
        ItemStack off = inv.getItemInOffHand();
        if (isUsable(off)) return off;

        return null;
    }

    private boolean isUsable(ItemStack it) {
        return it != null && it.getType() != Material.AIR && it.getAmount() > 0;
    }

    /** Convert yaw to the nearest horizontal BlockFace. */
    private BlockFace yawToFace(float yaw) {
        // Normalize yaw
        float rot = (yaw % 360 + 360) % 360;
        // 0/-360 = SOUTH in Minecraft player terms; map to cardinal faces for chests
        if (rot >= 315 || rot < 45)  return BlockFace.SOUTH;
        if (rot < 135)               return BlockFace.WEST;
        if (rot < 225)               return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private ItemStack pickRandomPlayerItem(PlayerInventory inv) {
        // Collect from storage/hotbar, armor, and offhand
        java.util.List<ItemStack> pool = new java.util.ArrayList<>();

        // Storage (includes hotbar)
        for (ItemStack it : inv.getStorageContents()) {
            if (isUsable(it)) pool.add(it);
        }

        // Armor
        for (ItemStack it : inv.getArmorContents()) {
            if (isUsable(it)) pool.add(it);
        }

        // Offhand
        ItemStack off = inv.getItemInOffHand();
        if (isUsable(off)) pool.add(off);

        if (pool.isEmpty()) return null;

        // Uniform random pick
        java.util.Random r = java.util.concurrent.ThreadLocalRandom.current();
        return pool.get(r.nextInt(pool.size()));
    }


}

