package de.eisner.luckyblocks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class LuckyBlockEvents implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (LuckBlockManager.isLuckyBlock(e.getBlock().getLocation())) {
			LuckBlockManager.registerLuckyBlockBreak(e.getBlock().getLocation(), e.getPlayer());
			e.setDropItems(false);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (LuckBlockManager.isLuckyBlock(e.getItemInHand())) {
			LuckBlockManager.registerLuckyBlockPlace(e.getBlock().getLocation());
		}
	}
}
