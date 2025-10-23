package de.eisner.luckyblocks.drops.settings;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;

public class SettingsEvent implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p) || !p.hasPermission(SettingsManager.PERMISSION)) {
			return;
		}
		if (e.getView() == null || e.getView().getTitle() == null || !e.getView().getTitle().startsWith(SettingsManager.GUI_NAME)) {
			return;
		}
		e.setCancelled(true);
		if (e.getCurrentItem() == null) {
			return;
		}
		String itemName = e.getCurrentItem().getItemMeta().getDisplayName();
		int page = Integer.valueOf(e.getView().getTitle().replace(SettingsManager.GUI_NAME, ""));
		if (itemName.equals(SettingsManager.LAST_PAGE_ITEM_NAME)) {
			p.openInventory(SettingsManager.createGUI(page > 0 ? page - 1 : page));
			return;
		}
		if (itemName.equals(SettingsManager.NEXT_PAGE_ITEM_NAME)) {
			p.openInventory(SettingsManager.createGUI(page + 1));
			return;
		}
		if (itemName.equals(SettingsManager.EMPTY_ITEM_NAME)) {
			return;
		}
		itemName = itemName.substring(SettingsManager.ITEM_PEFIX.length());
		Drop drop = DropManager.getDropByName(itemName);
		if (DropManager.getEnabledAmont() == 1 && drop.isEnabled()) {
			return;
		}
		drop.setEnabled(!drop.isEnabled());
		p.openInventory(SettingsManager.createGUI(page));
	}

}
