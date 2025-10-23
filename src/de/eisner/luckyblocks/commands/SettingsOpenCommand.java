package de.eisner.luckyblocks.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.settings.SettingsManager;

public class SettingsOpenCommand implements CommandExecutor, TabCompleter {


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission(SettingsManager.PERMISSION) && sender instanceof Player p) {
			p.openInventory(SettingsManager.createGUI(0));
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return Arrays.asList("");
	}

}
