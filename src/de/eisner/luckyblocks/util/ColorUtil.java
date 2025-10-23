package de.eisner.luckyblocks.util;

public class ColorUtil {

	public static String createRGBChatColor(int red, int green, int blue) {
		StringBuilder result = new StringBuilder("§x");
		for (char c : String.format("%02x%02x%02x", red, green, blue).toCharArray()) {
			result.append("§" + c);
		}
		return result.toString();
	}

	public static String createTextWithFade(String text, int red, int green, int blue, int redInc, int greedInc, int blueInc) {
		StringBuilder result = new StringBuilder("§x");
		for (char c : text.toCharArray()) {
			result.append(createRGBChatColor(red, green, blue) + c);
			red += redInc;
			green += greedInc;
			blue += blueInc;
		}
		return result.toString();
	}

}
