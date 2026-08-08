package handsome.block;

import net.minecraft.block.Block;

/**
 * 钠矿方块本体。
 * 采掘等级由 {@code data/minecraft/tags/blocks/needs_stone_tool.json} 与
 * {@code data/minecraft/tags/blocks/mineable/pickaxe.json} 控制（1.17+ Fabric 推荐做法）。
 */
public class SodiumOreBlock extends Block {
    public SodiumOreBlock(Settings settings) {
        super(settings);
    }
}
