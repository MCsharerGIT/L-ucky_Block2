package de.eisner.luckyblocks.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import de.eisner.luckyblocks.Main;
import de.eisner.luckyblocks.drops.DropType;
import de.eisner.luckyblocks.util.NumberUtils;

public class OddsCommand implements CommandExecutor, TabCompleter {

	private static final String PERMISSION = "luckyblocks.odds";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission(PERMISSION)) {
			return false;
		}

		if (args.length == 0) {
			sender.sendMessage(Main.NAME);
			for (DropType dt : DropType.values()) {
				sender.sendMessage("§e" + dt.toString() + "§8:§6 " + dt.getChance());
			}
			sender.sendMessage(Main.NAME);
		}

		if (args.length != DropType.values().length) {
			return false;
		}

		int sum = 0;
		for (String arg : args) {
			if (!NumberUtils.isInteger(arg)) {
				return false;
			}
			sum += Integer.valueOf(arg);
		}
		if (sum != 100) {
			sender.sendMessage(Main.NAME + "The odds do not add up to 100.");
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			DropType.values()[i].setChance(Integer.valueOf(args[i]));
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission(PERMISSION)) {
			int index = args.length - 1;
			if (index < DropType.values().length) {
				return Arrays.asList(DropType.values()[index].toString());
			}
		}
		return Arrays.asList("");

	}

}
