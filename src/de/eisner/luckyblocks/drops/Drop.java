package de.eisner.luckyblocks.drops;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Drop {

	private String name;
	private List<String> description;
	private DropType type;
	private boolean enabled;
	private long delay;

	public Drop(String name, List<String> description, DropType type, long delay) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.delay = delay;
		setEnabled(true);
	}

	public String getName() {
		return name;
	}

	public List<String> getDescription() {
		return description;
	}

	public DropType getType() {
		return type;
	}

	public abstract void execute(Player p, Location loc);

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Drop drop) {
			return drop.getName().equals(name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getDelay() {
		return delay;
	}

}
