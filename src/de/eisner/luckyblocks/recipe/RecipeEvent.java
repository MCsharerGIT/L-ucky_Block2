package de.eisner.luckyblocks.recipe;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import de.eisner.luckyblocks.Main;

public class RecipeEvent implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p) || !p.hasPermission(RecipeManager.PERMISSION)) {
			return;
		}
		if (e.getClickedInventory() == null || e.getView() == null || e.getView().getTitle() == null || !e.getView().getTitle().equals(RecipeManager.GUI_NAME)) {
			return;
		}
		if (e.getClickedInventory().getType() == InventoryType.CHEST && !Arrays.asList(12, 13, 14, 21, 22, 23, 30, 31, 32).contains(e.getSlot())) {
			e.setCancelled(true);
		}
		if (e.getSlot() == RecipeManager.DISCARD_SLOT) {
			p.closeInventory();
			p.sendMessage(Main.NAME + "Recipe was removed.");
			RecipeManager.removeRecipe();
			return;
		}
		if (e.getSlot() == RecipeManager.CONFIRM_SLOT) {
			RecipeManager.removeRecipe();
			Material[] recipe = new Material[RecipeManager.RECIPE_SIZE];
			for (int i = 0; i < RecipeManager.RECIPE_SIZE; i++) {
				int index = 12 + i % 3 + (i / 3) * 9;
				ItemStack item = e.getClickedInventory().getItem(index);
				recipe[i] = item == null ? Material.AIR : item.getType();
			}
			if (!isValid(recipe)) {
				return;
			}
			RecipeManager.setRecipe(recipe);
			p.sendMessage(Main.NAME + "Recipe was set.");
			p.closeInventory();
			return;
		}
	}

	private boolean isValid(Material[] recipe) {
		for (Material material : recipe) {
			if (material != null && material != Material.AIR) {
				return true;
			}
		}
		return false;
	}
}
