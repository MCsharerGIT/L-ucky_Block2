package de.eisner.luckyblocks.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.eisner.luckyblocks.LuckBlockManager;
import de.eisner.luckyblocks.util.ItemUtils;
import de.eisner.luckyblocks.util.NumberUtils;

public class GiveCommand implements CommandExecutor, TabCompleter {

	private static final String PERMISSION = "luckyblocks.give";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 2) {
			return false;
		}
		if (sender instanceof Player p && p.hasPermission(PERMISSION)) {
			if (args[0].equalsIgnoreCase("me") && NumberUtils.isInteger(args[1])) {
				ItemUtils.giveItem(p, LuckBlockManager.getLuckyBlock(Integer.valueOf(args[1])));
				return false;
			}
			if (args[0].equalsIgnoreCase("all") && NumberUtils.isInteger(args[1])) {
				Bukkit.getOnlinePlayers().forEach(r -> ItemUtils.giveItem(r, LuckBlockManager.getLuckyBlock(Integer.valueOf(args[1]))));
				return false;
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission(PERMISSION)) {
			return Arrays.asList("");
		}
		if (args.length == 1) {
			return Arrays.asList("me", "all");
		}
		if (args.length == 2) {
			return Arrays.asList("amount");

		}
		return Arrays.asList("");
	}

}
