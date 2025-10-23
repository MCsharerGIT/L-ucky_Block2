package de.eisner.luckyblocks.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.eisner.luckyblocks.LuckBlockManager;
import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.util.ItemUtils;

public class RecipeManager {

	public static final NamespacedKey KEY = new NamespacedKey(Main.plugin, "luckyblockrecipe");
	public static final String GUI_NAME = Main.NAME + "Recipe";
	public static final String PERMISSION = "luckyblocks.recipe";
	public static final String EMPTY_ITEM_NAME = "§r";
	public static final Integer RECIPE_SIZE = 9;
	public static final Integer DISCARD_SLOT = 37;
	public static final Integer CONFIRM_SLOT = 43;

	public static void setRecipe(Material[] items) {
		if (hasRecipe()) {
			removeRecipe();
		}
		ShapedRecipe recipe = new ShapedRecipe(KEY, LuckBlockManager.getLuckyBlock(1));
		recipe.shape("012", "345", "678");
		for (int i = 0; i < items.length; i++) {
			if (items[i] == Material.AIR || items[i] == null) {
				continue;
			}
			recipe.setIngredient(String.valueOf(i).charAt(0), items[i]);
		}
		Bukkit.addRecipe(recipe);
		Bukkit.getOnlinePlayers().forEach(p -> p.discoverRecipe(KEY));
	}

	public static boolean hasRecipe() {
		return Bukkit.getRecipe(KEY) != null;
	}

	public static void removeRecipe() {
		if (!hasRecipe()) {
			return;
		}
		Bukkit.removeRecipe(KEY);
	}

	public static Material[] getRecipeArray() {
		Material[] result = new Material[RECIPE_SIZE];
		if (hasRecipe()) {
			ShapedRecipe recipe = (ShapedRecipe) Bukkit.getRecipe(KEY);
			int columns = (int) Math.sqrt(RECIPE_SIZE);
			for (int index = 0; index < RECIPE_SIZE; index++) {
				int row = index / columns;
				int column = index % columns;
				if (row >= recipe.getShape().length || column >= recipe.getShape()[row].length()) {
					continue;
				}
				ItemStack item = recipe.getIngredientMap().get(recipe.getShape()[row].charAt(column));
				result[index] = item == null ? Material.AIR : item.getType();
			}
		}
		return result;
	}

	public static Inventory createGUI() {
		Inventory gui = Bukkit.createInventory(null, 54, GUI_NAME);
		for (int i = 0; i < gui.getSize(); i++) {
			gui.setItem(i, ItemUtils.buildItem(Material.WHITE_STAINED_GLASS_PANE, 1, EMPTY_ITEM_NAME, null));
		}
		Material[] recipe = getRecipeArray();
		for (int i = 0; i < RECIPE_SIZE; i++) {
			int index = 12 + i % 3 + (i / 3) * 9;
			gui.setItem(index, new ItemStack(recipe[i] == null ? Material.AIR : recipe[i]));
		}
		gui.setItem(37, ItemUtils.buildItem(Material.RED_DYE, 1, "§cDiscard Recipe", null));
		gui.setItem(43, ItemUtils.buildItem(Material.LIME_DYE, 1, "§aConfirm Recipe", null));
		return gui;
	}

}
