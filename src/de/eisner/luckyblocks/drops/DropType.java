package de.eisner.luckyblocks.drops;

import java.util.HashSet;

public enum DropType {

	GOOD(34), NEUTRAL(33), BAD(33);

	private HashSet<Drop> drops;
	private Integer chance;

	private DropType(Integer chance) {
		this.setChance(chance);
		this.drops = new HashSet<>();
	}

	public Integer getChance() {
		return chance;
	}

	public void setChance(Integer chance) {
		this.chance = chance;
	}

	public HashSet<Drop> getDrops() {
		return drops;
	}

}
