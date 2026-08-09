package handsome.world;

import handsome.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * 在世界生成时，将已生成的铁矿 / 深层铁矿按概率替换为钠矿 / 深层钠矿。
 * 约束：不会把钠矿放在「距离液体 5 格以内且视线无方块阻隔」的位置。
 */
public class SodiumOreReplaceFeature extends Feature<DefaultFeatureConfig> {
    // 铁矿被替换为钠矿的概率
    private static final float REPLACE_CHANCE = 0.6F;
    // 与液体之间的最大距离（方块，切比雪夫半径）
    private static final int LIQUID_RADIUS = 5;

    public SodiumOreReplaceFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        // 当前区块 16x16 范围（不越界读取邻居区块）
        int baseX = (origin.getX() >> 4) << 4;
        int baseZ = (origin.getZ() >> 4) << 4;
        int minY = world.getBottomY();
        int maxY = world.getTopY();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
                    BlockState state = world.getBlockState(pos);

                    Block replace = null;
                    if (state.isOf(Blocks.IRON_ORE)) {
                        replace = ModBlocks.SODIUM_ORE;
                    } else if (state.isOf(Blocks.DEEPSLATE_IRON_ORE)) {
                        replace = ModBlocks.DEEPSLATE_SODIUM_ORE;
                    }

                    if (replace != null
                            && random.nextFloat() < REPLACE_CHANCE
                            && canPlaceSodium(world, pos)) {
                        world.setBlockState(pos, replace.getDefaultState(), 2);
                    }
                }
            }
        }
        return true;
    }

    /**
     * 是否允许在 pos 放置钠矿：
     * 若存在 5 格以内的液体，且从该位置到该液体之间没有任何实心方块阻隔（视线通透），
     * 则不允许放置（即钠矿不会「暴露」在液体旁）。
     */
    private boolean canPlaceSodium(StructureWorldAccess world, BlockPos pos) {
        for (BlockPos p : BlockPos.iterateOutwards(pos, LIQUID_RADIUS, LIQUID_RADIUS, LIQUID_RADIUS)) {
            if (!world.getBlockState(p).getFluidState().isEmpty()) {
                if (hasLineOfSight(world, pos, p)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 两点之间（不含端点）是否没有任何实心方块遮挡。 */
    private boolean hasLineOfSight(StructureWorldAccess world, BlockPos a, BlockPos b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double dz = b.getZ() - a.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = (int) Math.ceil(dist * 2.0);
        if (steps <= 1) {
            return true;
        }
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            BlockPos p = new BlockPos(
                    (int) Math.round(a.getX() + dx * t),
                    (int) Math.round(a.getY() + dy * t),
                    (int) Math.round(a.getZ() + dz * t));
            if (p.equals(a) || p.equals(b)) {
                continue;
            }
            if (world.getBlockState(p).isSolidBlock(world, p)) {
                return false;
            }
        }
        return true;
    }
}
