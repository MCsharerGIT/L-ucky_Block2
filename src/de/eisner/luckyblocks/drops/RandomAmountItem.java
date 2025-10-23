package de.eisner.luckyblocks.drops;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RandomAmountItem {

	private Material material;
	private Integer amount;

	public RandomAmountItem(Material material, Integer amount) {
		this.material = material;
		this.amount = amount;
	}

	public Material getMaterial() {
		return material;
	}

	public Integer getAmount() {
		return amount;
	}

	public ItemStack getItemWithRandomAmount() {
		int randomAmount = DropManager.RANDOM.nextInt(amount) + 1;
		ItemStack result = new ItemStack(material);
		result.setAmount(randomAmount);
		return result;
	}

}
