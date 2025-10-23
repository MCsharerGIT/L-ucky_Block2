package de.eisner.luckyblocks.drops.settings;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;
import de.eisner.luckyblocks.util.ItemUtils;

public class SettingsManager {

	public static final String GUI_NAME = Main.NAME + " Settings§8 - §7";
	public static final Integer GUI_SIZE = 54;
	public static final String PERMISSION = "luckyblocks.settings";
	public static final String EMPTY_ITEM_NAME = " ";
	public static final String ITEM_PEFIX = "§6";
	public static final Integer ITEMS_PER_PAGE = GUI_SIZE - 9;
	public static final String NEXT_PAGE_ITEM_NAME = "§8\u2192";
	public static final String LAST_PAGE_ITEM_NAME = "§8\u2190";

	public static Inventory createGUI(int page) {
		Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_NAME + page);
		List<Drop> drops = createDropList();
		for (int i = 0; i < ITEMS_PER_PAGE; i++) {
			int index = i + page * ITEMS_PER_PAGE;
			if (index >= drops.size()) {
				break;
			}
			Drop drop = drops.get(index);
			Material icon = drop.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
			gui.setItem(i, ItemUtils.buildItem(icon, 1, ITEM_PEFIX + drop.getName(), createLore(drop)));
		}
		gui.setItem(GUI_SIZE - 9, ItemUtils.buildItem(Material.ARROW, 1, LAST_PAGE_ITEM_NAME, null));
		gui.setItem(GUI_SIZE - 8, ItemUtils.buildItem(Material.ARROW, 1, NEXT_PAGE_ITEM_NAME, null));
		for (int i = 0; i < GUI_SIZE; i++) {
			if (gui.getItem(i) == null) {
				gui.setItem(i, ItemUtils.buildItem(i < GUI_SIZE - 9 ? Material.WHITE_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, 1, EMPTY_ITEM_NAME, null));
			}
		}
		return gui;
	}

	private static List<String> createLore(Drop drop) {
		List<String> result = new ArrayList<>();
		result.add("§7§oType§8:§e " + drop.getType());
		drop.getDescription().forEach(element -> result.add("§7§o" + element));
		return result;
	}

	public static List<Drop> createDropList() {
		List<Drop> result = new ArrayList<>();
		DropManager.getAllDrops().forEach(drop -> result.add(drop));
		return result;
	}

}
