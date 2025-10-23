package de.eisner.luckyblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.drops.Drop;
import de.eisner.luckyblocks.drops.DropManager;

public class ExecuteDropCommand implements CommandExecutor, TabCompleter {

	private static final String PERMISSION = "luckyblocks.drops";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player p && p.hasPermission(PERMISSION)) {
			if (args.length == 0) {
				return false;
			}
			HashMap<String, Drop> drops = new HashMap<>();
			DropManager.getAllDrops().forEach(drop -> drops.put(drop.getName(), drop));
			StringBuilder name = new StringBuilder();
			for (String arg : args) {
				name.append(arg + " ");
			}
			name.deleteCharAt(name.length() - 1);
			if (drops.containsKey(name.toString())) {
				drops.get(name.toString()).execute(p, p.getLocation());
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player p && p.hasPermission(PERMISSION)) {
			List<String> result = new ArrayList<>();
			DropManager.getAllDrops().forEach(drop -> result.add(drop.getName()));
			return result;
		}
		return Arrays.asList("");
	}

}
