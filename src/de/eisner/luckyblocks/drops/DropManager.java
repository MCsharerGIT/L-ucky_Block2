package de.eisner.luckyblocks.drops;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.eisner.luckyblocks.drops.alldrops.*;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import de.eisner.luckyblocks.drops.alldrops.BlockDrop;
import de.eisner.luckyblocks.drops.alldrops.ChairDrop;
import de.eisner.luckyblocks.drops.alldrops.ClimberLeggings;
import de.eisner.luckyblocks.drops.alldrops.CobwebStackDrop;
import de.eisner.luckyblocks.drops.alldrops.CopperGolemDrop;
import de.eisner.luckyblocks.drops.alldrops.CropFieldDrop;
import de.eisner.luckyblocks.drops.alldrops.DiamondOreDrop;
import de.eisner.luckyblocks.drops.alldrops.DoorTrapDrop;
import de.eisner.luckyblocks.drops.alldrops.FallingTridentDrop;
import de.eisner.luckyblocks.drops.alldrops.FlowerDrop;
import de.eisner.luckyblocks.drops.alldrops.FunnySheepDrop;
import de.eisner.luckyblocks.drops.alldrops.HeartSword;
import de.eisner.luckyblocks.drops.alldrops.HorseDrop;
import de.eisner.luckyblocks.drops.alldrops.HotbarSwap;
import de.eisner.luckyblocks.drops.alldrops.InventoryDropDrop;
import de.eisner.luckyblocks.drops.alldrops.ItemDrop;
import de.eisner.luckyblocks.drops.alldrops.KnockbackStickDrop;
import de.eisner.luckyblocks.drops.alldrops.LampDrop;
import de.eisner.luckyblocks.drops.alldrops.MagmaFieldDrop;
import de.eisner.luckyblocks.drops.alldrops.MooshroomDrop;
import de.eisner.luckyblocks.drops.alldrops.MusicDrop;
import de.eisner.luckyblocks.drops.alldrops.OneHitSwordDrop;
import de.eisner.luckyblocks.drops.alldrops.ParticleSphere;
import de.eisner.luckyblocks.drops.alldrops.ParticleSpiral;
import de.eisner.luckyblocks.drops.alldrops.PrisonDrop;
import de.eisner.luckyblocks.drops.alldrops.RandomItemDrop;
import de.eisner.luckyblocks.drops.alldrops.RandomPotionDrop;
import de.eisner.luckyblocks.drops.alldrops.SpawnDrop;
import de.eisner.luckyblocks.drops.alldrops.SpeedSword;
import de.eisner.luckyblocks.drops.alldrops.SuperBootsDrop;
import de.eisner.luckyblocks.drops.alldrops.SuperHelmetDrop;
import de.eisner.luckyblocks.drops.alldrops.TNTTrapDrop;
import de.eisner.luckyblocks.drops.alldrops.ThrowablePotionDrop;
import de.eisner.luckyblocks.drops.alldrops.TreeDrop;
import de.eisner.luckyblocks.drops.alldrops.VaultDrop;


public class DropManager {

	static {
		registerDrop(new RandomItemDrop("Resources", new RandomAmountItem(Material.DIAMOND, 3), new RandomAmountItem(Material.IRON_INGOT, 10)));
		registerDrop(new RandomPotionDrop("Good Potion", DropType.GOOD, PotionEffectType.STRENGTH, PotionEffectType.REGENERATION, PotionEffectType.HASTE, PotionEffectType.SPEED, PotionEffectType.JUMP_BOOST));
		registerDrop(new RandomPotionDrop("Bad Potion", DropType.BAD, PotionEffectType.BLINDNESS, PotionEffectType.HUNGER, PotionEffectType.MINING_FATIGUE, PotionEffectType.UNLUCK));
		registerDrop(new SpawnDrop("Bat", DropType.NEUTRAL, EntityType.BAT));
		registerDrop(new SpawnDrop("Golems", DropType.GOOD, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM));
		registerDrop(new SpawnDrop("Mobs", DropType.BAD, EntityType.CREEPER, EntityType.SKELETON, EntityType.SKELETON, EntityType.BLAZE));
		registerDrop(new RandomItemDrop("Milk", new RandomAmountItem(Material.MILK_BUCKET, 1)));
		registerDrop(new PrisonDrop());
		registerDrop(new FlowerDrop());
		registerDrop(new HorseDrop());
		registerDrop(new SpawnDrop("TNT", DropType.BAD, EntityType.TNT));
		registerDrop(new SpawnDrop("Warden", DropType.BAD, EntityType.WARDEN));
		registerDrop(new SpawnDrop("Wandering Trader", DropType.NEUTRAL, EntityType.WANDERING_TRADER));
		registerDrop(new RandomItemDrop("Apples", new RandomAmountItem(Material.APPLE, 5), new RandomAmountItem(Material.GOLDEN_APPLE, 1)));
		registerDrop(new RandomItemDrop("Redstone", new RandomAmountItem(Material.REDSTONE, 45), new RandomAmountItem(Material.REDSTONE_TORCH, 5), new RandomAmountItem(Material.REPEATER, 5),
				new RandomAmountItem(Material.LEVER, 5), new RandomAmountItem(Material.TNT, 4), new RandomAmountItem(Material.REDSTONE_LAMP, 4), new RandomAmountItem(Material.PISTON, 8),
				new RandomAmountItem(Material.PISTON, 8), new RandomAmountItem(Material.DROPPER, 6), new RandomAmountItem(Material.OBSERVER, 2)));
		registerDrop(new MagmaFieldDrop());
		registerDrop(new SuperBootsDrop());
		registerDrop(new RandomItemDrop("Windcharges", new RandomAmountItem(Material.WIND_CHARGE, 12)));
		registerDrop(new ThrowablePotionDrop("Miner Potion", Arrays.asList(new PotionEffect(PotionEffectType.HASTE, 20 * 60 * 3, 3), new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 3, 0))));
		registerDrop(new ThrowablePotionDrop("Void Potion", Arrays.asList(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 10, 200))));
		registerDrop(new ThrowablePotionDrop("Movement Potion", Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 5), new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 60, 5))));
		registerDrop(new ThrowablePotionDrop("Super Potion", Arrays.asList(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 2, 0), new PotionEffect(PotionEffectType.REGENERATION, 20 * 60 * 3, 0),
				new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 3, 0))));
		registerDrop(new RandomItemDrop("Resources 2", new RandomAmountItem(Material.GOLD_INGOT, 16), new RandomAmountItem(Material.COPPER_INGOT, 32)));
		registerDrop(new RandomItemDrop("Food", new RandomAmountItem(Material.BREAD, 16), new RandomAmountItem(Material.BAKED_POTATO, 16)));
		registerDrop(new RandomItemDrop("Turtle", new RandomAmountItem(Material.TURTLE_HELMET, 1), new RandomAmountItem(Material.TURTLE_EGG, 4)));
		registerDrop(new RandomItemDrop("Enderperls", new RandomAmountItem(Material.ENDER_PEARL, 8), new RandomAmountItem(Material.ENDER_EYE, 16)));
		registerDrop(new RandomItemDrop("Bow", new RandomAmountItem(Material.BOW, 1), new RandomAmountItem(Material.ARROW, 16)));
		registerDrop(new BlockDrop("Cake", Material.CAKE, DropType.GOOD));
		registerDrop(new BlockDrop("Ironblock", Material.IRON_BLOCK, DropType.GOOD));
		registerDrop(new BlockDrop("Bedrockblock", Material.BEDROCK, DropType.NEUTRAL));
		registerDrop(new BlockDrop("Dirtblock", Material.DIRT, DropType.NEUTRAL));
		registerDrop(new BlockDrop("Tntblock", Material.TNT, DropType.NEUTRAL));
		registerDrop(new SpawnDrop("Ghast", DropType.BAD, EntityType.GHAST));
		registerDrop(new SpawnDrop("Animals", DropType.NEUTRAL, EntityType.PIG, EntityType.SHEEP, EntityType.COW, EntityType.CHICKEN));
		registerDrop(new ItemDrop("Gold Armor", DropType.GOOD, Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET));
		registerDrop(new ItemDrop("Iron Armor", DropType.GOOD, Material.IRON_HELMET, Material.IRON_BOOTS));
		registerDrop(new ItemDrop("Candles", DropType.NEUTRAL, Material.CANDLE, Material.BLACK_CANDLE, Material.BLUE_CANDLE, Material.BROWN_CANDLE, Material.CYAN_CANDLE, Material.GRAY_CANDLE, Material.GREEN_CANDLE,
				Material.LIGHT_BLUE_CANDLE, Material.LIGHT_GRAY_CANDLE, Material.LIME_CANDLE, Material.MAGENTA_CANDLE, Material.ORANGE_CANDLE, Material.PINK_CANDLE, Material.PURPLE_CANDLE, Material.RED_CANDLE,
				Material.WHITE_CANDLE, Material.YELLOW_CANDLE));
		registerDrop(new MooshroomDrop());
		registerDrop(new BlockDrop("Lavablock", Material.LAVA, DropType.BAD));
		registerDrop(new ItemDrop("Ironsword", DropType.GOOD, Material.IRON_SWORD));
		registerDrop(new ItemDrop("Ironaxe", DropType.GOOD, Material.IRON_AXE));
		registerDrop(new ItemDrop("Chainmal Armor", DropType.GOOD, Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET));
		registerDrop(new KnockbackStickDrop());
		registerDrop(new RandomItemDrop("EXP Bottles", new RandomAmountItem(Material.EXPERIENCE_BOTTLE, 32)));
		registerDrop(new SpawnDrop("Spiders", DropType.BAD, EntityType.CAVE_SPIDER, EntityType.CAVE_SPIDER, EntityType.CAVE_SPIDER, EntityType.SPIDER, EntityType.SPIDER));
		registerDrop(new ItemDrop("Totem of Undying", DropType.GOOD, Material.TOTEM_OF_UNDYING));
		registerDrop(new SpawnDrop("Mini Raid", DropType.BAD, EntityType.VEX, EntityType.VEX, EntityType.PILLAGER, EntityType.PILLAGER, EntityType.WITCH));
		registerDrop(new CopperGolemDrop());
		registerDrop(new BlockDrop("Emerald Block", Material.EMERALD_BLOCK, DropType.GOOD));
		registerDrop(new VaultDrop());
		registerDrop(new CobwebStackDrop());
		registerDrop(new ThrowablePotionDrop("Freeze Potion", Arrays.asList(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 30, 9), new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 30, 250))));
		registerDrop(new CropFieldDrop());
		registerDrop(new RandomItemDrop("Brush", new RandomAmountItem(Material.BRUSH, 1), new RandomAmountItem(Material.SUSPICIOUS_SAND, 16)));
		registerDrop(new TNTTrapDrop());
		registerDrop(new SpawnDrop("Boat", DropType.NEUTRAL, EntityType.BOAT));
		registerDrop(new SpawnDrop("Lightning", DropType.BAD, EntityType.LIGHTNING_BOLT));
		registerDrop(new BlockDrop("Beacon", Material.BEACON, DropType.NEUTRAL));
		registerDrop(new SpawnDrop("End Crystal", DropType.NEUTRAL, EntityType.END_CRYSTAL));
		registerDrop(new FunnySheepDrop());
		registerDrop(new SpeedSword());
		registerDrop(new ClimberLeggings());
		registerDrop(new ParticleSpiral());
		registerDrop(new ParticleSphere());
		registerDrop(new LampDrop());
		registerDrop(new HeartSword());
		registerDrop(new FallingTridentDrop());
		registerDrop(new SuperHelmetDrop());
		registerDrop(new OneHitSwordDrop());
		registerDrop(new TreeDrop());
		registerDrop(new DiamondOreDrop());
		registerDrop(new MusicDrop());
		registerDrop(new InventoryDropDrop());
		registerDrop(new HotbarSwap());
		registerDrop(new DoorTrapDrop());
		registerDrop(new ChairDrop());
		registerDrop(new NetherworldDrop());
		registerDrop(new FishCannonDrop());
		registerDrop(new OrbOfStrengthDrop());
		registerDrop(new AnvilDrop());
	}

	public static final Random RANDOM = new Random();

	private static void registerDrop(Drop drop) {
		if (getDropByName(drop.getName()) != null) {
			throw new IllegalArgumentException("Cant add drop! There is already a drop called " + drop.getName());
		}
		drop.getType().getDrops().add(drop);
	}

	public static Drop getDropByName(String name) {
		for (DropType dt : DropType.values()) {
			for (Drop drop : dt.getDrops()) {
				if (drop.getName().equals(name)) {
					return drop;
				}
			}
		}
		return null;
	}

	/*
	 * All drops beeing disabled will cause a crash since it causes an endless
	 * recursion.
	 */
	public static int getEnabledAmont() {
		int amount = getAllDrops().size();
		for (DropType dt : DropType.values()) {
			for (Drop drop : dt.getDrops()) {
				if (!drop.isEnabled()) {
					amount--;
				}
			}
		}
		return amount;
	}

	public static void setChances(int good, int neutral, int bad) {
		if (good + bad + neutral != 100) {
			return;
		}
		DropType.GOOD.setChance(good);
		DropType.NEUTRAL.setChance(neutral);
		DropType.BAD.setChance(bad);

	}

	private static Drop pickRandom(HashSet<Drop> origin) {
		int element = RANDOM.nextInt(origin.size());
		int index = 0;
		for (Drop drop : origin) {
			if (index == element) {
				return drop;
			}
			index++;
		}
		return null;
	}

	public static Drop getRandomDrop() {
		int random = RANDOM.nextInt(99) + 1;
		int sum = 0;
		for (DropType dt : DropType.values()) {
			sum += dt.getChance();
			if (random <= sum) {
				Drop result = pickRandom(dt.getDrops());
				return result.isEnabled() ? result : getRandomDrop();
			}
		}
		return null;
	}

	public static Set<Drop> getAllDrops() {
		HashSet<Drop> result = new HashSet<>();
		for (DropType type : DropType.values()) {
			result.addAll(type.getDrops());
		}
		return result;
	}

}
