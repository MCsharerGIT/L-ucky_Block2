package de.eisner.luckyblocks;

import org.bukkit.plugin.java.JavaPlugin;

import de.eisner.luckyblocks.commands.ExecuteDropCommand;
import de.eisner.luckyblocks.commands.GiveCommand;
import de.eisner.luckyblocks.commands.OddsCommand;
import de.eisner.luckyblocks.commands.RecipeOpenCommand;
import de.eisner.luckyblocks.commands.SettingsOpenCommand;
import de.eisner.luckyblocks.drops.settings.SettingsEvent;
import de.eisner.luckyblocks.recipe.RecipeEvent;
import de.eisner.luckyblocks.util.ColorUtil;

public class Main extends JavaPlugin {

	public static final String NAME = ColorUtil.createTextWithFade("Luckyblocks", 212, 108, 11, 0, 10, 0) + " §8\u00BB§7 ";

	public static Main plugin;
	
	

	@Override
	public void onEnable() {
		plugin = this;
		getCommand("lbgive").setExecutor(new GiveCommand());
		getCommand("lbdrop").setExecutor(new ExecuteDropCommand());
		getCommand("lbsettings").setExecutor(new SettingsOpenCommand());
		getCommand("lbodds").setExecutor(new OddsCommand());
		getCommand("lbrecipe").setExecutor(new RecipeOpenCommand());
		getServer().getPluginManager().registerEvents(new LuckyBlockEvents(), this);
		getServer().getPluginManager().registerEvents(new SettingsEvent(), this);
		getServer().getPluginManager().registerEvents(new RecipeEvent(), this);
	}

}
