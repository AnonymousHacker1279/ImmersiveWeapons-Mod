package com.anonymoushacker1279.immersiveweapons.util;

import com.anonymoushacker1279.immersiveweapons.ImmersiveWeapons;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class Config {

	private static final ForgeConfigSpec.Builder COMMON_CONFIG_BUILDER = new ForgeConfigSpec.Builder();
	private static ForgeConfigSpec COMMON_CONFIG;

	public static ForgeConfigSpec.ConfigValue<Integer> MAX_ABANDONED_FACTORY_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Integer> MIN_ABANDONED_FACTORY_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Integer> MAX_PITFALL_TRAP_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Integer> MIN_PITFALL_TRAP_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Integer> MAX_BEAR_TRAP_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Integer> MIN_BEAR_TRAP_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Integer> MAX_LANDMINE_TRAP_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Integer> MIN_LANDMINE_TRAP_DISTANCE;
	public static ForgeConfigSpec.ConfigValue<Boolean> TESLA_ARMOR_EFFECT_SOUND;
	public static ForgeConfigSpec.ConfigValue<Integer> MAX_SMOKE_BOMB_PARTICLES;
	public static ForgeConfigSpec.ConfigValue<Boolean> BULLETS_BREAK_GLASS;

	static {
		initConfig();
	}


	private static void initConfig() {
		COMMON_CONFIG_BUILDER.push(ImmersiveWeapons.MOD_ID + "-common");

		MAX_ABANDONED_FACTORY_DISTANCE = COMMON_CONFIG_BUILDER.comment("Maximum distance in chunks between Abandoned Factories - Default 120").define("max_abandoned_factory_distance", 120);
		MIN_ABANDONED_FACTORY_DISTANCE = COMMON_CONFIG_BUILDER.comment("Minimum distance in chunks between Abandoned Factories - Default 90").define("min_abandoned_factory_distance", 90);

		MAX_PITFALL_TRAP_DISTANCE = COMMON_CONFIG_BUILDER.comment("Maximum distance in chunks between Pitfall Traps - Default 8").define("max_pitfall_trap_distance", 8);
		MIN_PITFALL_TRAP_DISTANCE = COMMON_CONFIG_BUILDER.comment("Minimum distance in chunks between Pitfall Traps - Default 2").define("min_pitfall_trap_distance", 2);

		MAX_BEAR_TRAP_DISTANCE = COMMON_CONFIG_BUILDER.comment("Maximum distance in chunks between Bear Traps - Default 10").define("max_bear_trap_distance", 10);
		MIN_BEAR_TRAP_DISTANCE = COMMON_CONFIG_BUILDER.comment("Minimum distance in chunks between Bear Traps - Default 4").define("min_bear_trap_distance", 4);

		MAX_LANDMINE_TRAP_DISTANCE = COMMON_CONFIG_BUILDER.comment("Minimum distance in chunks between Landmine Traps - Default 12").define("max_landmine_trap_distance", 12);
		MIN_LANDMINE_TRAP_DISTANCE = COMMON_CONFIG_BUILDER.comment("Minimum distance in chunks between Landmine Traps - Default 6").define("min_landmine_trap_distance", 6);

		TESLA_ARMOR_EFFECT_SOUND = COMMON_CONFIG_BUILDER.comment("Enable/Disable the Tesla Armor effect sound - Default true").define("tesla_armor_effect_sound", true);

		MAX_SMOKE_BOMB_PARTICLES = COMMON_CONFIG_BUILDER.comment("Set the maximum number of particles produced by the smoke bomb - Default 96\nSetting this higher can make clients laggy, setting to 0 effectively disables it").define("max_smoke_bomb_particles", 96);

		BULLETS_BREAK_GLASS = COMMON_CONFIG_BUILDER.comment("Enable/Disable bullets breaking glass - Default true").define("bullets_break_glass", true);


		COMMON_CONFIG_BUILDER.pop();
		COMMON_CONFIG = COMMON_CONFIG_BUILDER.build();

	}

	public static void setup(Path path) {
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();

		configData.load();
		COMMON_CONFIG.setConfig(configData);
	}

}