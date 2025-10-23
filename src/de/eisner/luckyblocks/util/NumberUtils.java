package de.eisner.luckyblocks.util;

public class NumberUtils {

	public static boolean isInteger(String s) {
		try {
			Integer.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
