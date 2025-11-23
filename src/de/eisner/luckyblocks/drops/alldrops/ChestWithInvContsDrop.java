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

        // Pick an item the player actually has
        ItemStack chosen = pickRandomPlayerItem(p.getInventory());
        if (chosen == null) {
            p.sendMessage("§7You don't have any items to copy.");
            return;
        }

        final ItemStack fillStack = chosen.clone();
        fillStack.setAmount(Math.min(Math.max(fillStack.getAmount(), 1), fillStack.getMaxStackSize()));
        Bukkit.getLogger().info("stack: " + fillStack);

        // Place chest at target
        final Block target = loc.getBlock();
        target.setType(Material.CHEST, false);

        // Face chest *toward the player* & force SINGLE type
        {
            org.bukkit.block.data.type.Chest data = (org.bukkit.block.data.type.Chest) target.getBlockData();
            data.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
            data.setFacing(faceTowards(target.getLocation(), p.getLocation()));
            target.setBlockData(data, false);
        }

        // Fill inventory *next tick* and re-fetch the Chest state then
        final org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("LuckyBlocks");
        if (plugin == null) return;

        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BlockState stateNow = target.getState();
            if (!(stateNow instanceof Chest chest)) return;
            Bukkit.getLogger().info("Its a chest!");

            Inventory inv = chest.getBlockInventory();
            Bukkit.getLogger().info("Chest inv!: " + inv);
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, fillStack.clone());
            }

            world.playSound(target.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_CHEST_OPEN, 0.9f, 1.1f);
        }, 1L); // <-- NOTE: wait 1 tick (not 0)
    }

    /** Map chest->player vector to nearest horizontal BlockFace so the chest FRONT looks at the player. */
    private BlockFace faceTowards(Location chest, Location player) {
        double dx = player.getX() - (chest.getBlockX() + 0.5);
        double dz = player.getZ() - (chest.getBlockZ() + 0.5);
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            return dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
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

