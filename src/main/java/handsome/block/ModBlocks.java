package handsome.block;

import handsome.HandsomeMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    // 钠矿：属性对齐铁矿（硬度 3.0 / 抗爆 3.0），掉落由战利品表决定（直接掉钠）
    public static final Block SODIUM_ORE = register("sodium_ore",
            new SodiumOreBlock(Block.Settings.create()
                    .requiresTool()
                    .strength(3.0F, 3.0F)
                    .sounds(BlockSoundGroup.STONE)
                    .mapColor(MapColor.STONE)));

    // 深层钠矿：属性对齐深层铁矿（硬度 4.5 / 抗爆 3.0）
    public static final Block DEEPSLATE_SODIUM_ORE = register("deepslate_sodium_ore",
            new SodiumOreBlock(Block.Settings.create()
                    .requiresTool()
                    .strength(4.5F, 3.0F)
                    .sounds(BlockSoundGroup.STONE)
                    .mapColor(MapColor.DEEPSLATE_GRAY)));

    private static Block register(String name, Block block) {
        Identifier id = new Identifier(HandsomeMod.MOD_ID, name);
        // 注册方块本体
        Registry.register(Registries.BLOCK, id, block);
        // 同步注册对应的 BlockItem，使物品栏/创造模式可获取
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void registerModBlocks() {
        HandsomeMod.LOGGER.info("Registering Mod Blocks for " + HandsomeMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS)
                .register(entries -> {
                    entries.add(SODIUM_ORE);
                    entries.add(DEEPSLATE_SODIUM_ORE);
                });
    }
}
