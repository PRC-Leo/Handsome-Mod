package handsome;

import handsome.block.ModBlocks;
import handsome.item.ModItems;
import handsome.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandsomeMod implements ModInitializer {
	public static final String MOD_ID = "handsome-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		// 注册本模组的方块（钠矿 / 深层钠矿）
		ModBlocks.registerModBlocks();

		// 注册本模组的物品（含「钠」）
		ModItems.registerModItems();

		// 注册世界生成（铁矿按 30% 替换为钠矿）
		ModWorldGen.generateModWorldGen();
	}
}