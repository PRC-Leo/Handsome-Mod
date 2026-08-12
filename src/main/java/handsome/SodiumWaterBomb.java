package handsome;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 钠遇水爆炸机制：
 *  - 每 5 个游戏刻检测一次玩家背包（快捷栏 / 主手 / 副手 / 盔甲）是否含有「钠系物品」（由标签 handsome-mod:sodium_items 定义）。
 *  - 用每个玩家独立的布尔记录「是否持有钠」。
 *  - 当持有钠且触碰水（含淋雨）时，点燃 40 刻（2 秒）引信，播放 TNT 点燃前的嘶嘶声；引信结束在玩家位置爆炸。
 *  - 爆炸：随机半径 3~8 格球形破坏（基岩除外）；实体伤害 0 格 200、每格 -20、10 格起 0；触发玩家固定承受 200。
 */
public class SodiumWaterBomb {
    private static final TagKey<net.minecraft.item.Item> SODIUM_ITEMS_TAG =
            TagKey.of(RegistryKeys.ITEM, new Identifier(HandsomeMod.MOD_ID, "sodium_items"));

    // 每个玩家独立状态；WeakHashMap 在玩家对象被回收后自动清理，避免内存泄漏
    private static final Map<ServerPlayerEntity, PlayerState> STATES = new WeakHashMap<>();

    private static final int CHECK_INTERVAL = 5;   // 每 5 刻检测一次背包
    private static final int FUSE_TICKS = 40;      // 引信 40 刻 = 2 秒
    private static final float BASE_DAMAGE = 200F; // 0 格伤害
    private static final float DAMAGE_STEP = 20F;  // 每远 1 格 -20
    private static final int DAMAGE_MAX_DIST = 10; // 10 格起不再造成伤害

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register((ServerWorld world) -> {
            long time = world.getTime();
            boolean doCheck = (time % CHECK_INTERVAL == 0);

            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerState state = STATES.computeIfAbsent(player, p -> new PlayerState());

                // 每 5 刻刷新一次「是否持有钠」的布尔
                if (doCheck) {
                    state.hasSodium = hasSodium(player);
                }

                // 持有钠 且 触碰水/淋雨 -> 点燃引信（一旦点燃即提交，引信结束必定爆炸）
                if (state.fuse <= 0) {
                    if (state.hasSodium && isInWaterOrRain(world, player)) {
                        state.fuse = FUSE_TICKS;
                        playFuse(world, player);
                    }
                } else {
                    state.fuse--;
                    // 引信期间持续播放 TNT 点燃嘶嘶声（约每 0.5 秒一次，覆盖整段 2 秒）
                    if (state.fuse % 10 == 0) {
                        playFuse(world, player);
                    }
                    if (state.fuse <= 0) {
                        explode(world, player);
                        STATES.remove(player);
                    }
                }
            }
        });
    }

    /** 玩家背包（主背包含快捷栏、盔甲、副手）里是否含有任意钠系物品。 */
    private static boolean hasSodium(ServerPlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (isSodium(stack)) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (isSodium(stack)) return true;
        }
        for (ItemStack stack : player.getInventory().offHand) {
            if (isSodium(stack)) return true;
        }
        return false;
    }

    private static boolean isSodium(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(SODIUM_ITEMS_TAG);
    }

    /** 是否触碰水（脚下/浸没）或正在淋雨（天气下雨且头顶可见天空且为降雨）。 */
    private static boolean isInWaterOrRain(ServerWorld world, ServerPlayerEntity player) {
        if (player.isTouchingWater() || player.isSubmergedInWater()) {
            return true;
        }
        if (world.isRaining() && world.isSkyVisible(player.getBlockPos())) {
            Biome.Precipitation precipitation = world.getBiome(player.getBlockPos()).value().getPrecipitation(player.getBlockPos());
            return precipitation == Biome.Precipitation.RAIN;
        }
        return false;
    }

    /** 播放 TNT 点燃前的嘶嘶声。 */
    private static void playFuse(ServerWorld world, ServerPlayerEntity player) {
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static void explode(ServerWorld world, ServerPlayerEntity player) {
        BlockPos center = player.getBlockPos();
        double cx = center.getX() + 0.5;
        double cy = center.getY() + 0.5;
        double cz = center.getZ() + 0.5;

        // 随机半径 3~8 格
        int radius = 3 + world.random.nextInt(6);

        // 破坏球形范围内所有方块（基岩除外），不掉落物品
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r2) continue;
                    BlockPos p = center.add(dx, dy, dz);
                    Block block = world.getBlockState(p).getBlock();
                    if (block == Blocks.BEDROCK) continue;
                    world.breakBlock(p, false);
                }
            }
        }

        // 爆炸音效（额外的反馈，规范未要求但更自然）
        world.playSound(null, center, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, 1.0F);

        // 实体伤害：0~10 格按距离递减；触发玩家固定 200
        double reach = DAMAGE_MAX_DIST + 1;
        Box box = new Box(cx - reach, cy - reach, cz - reach, cx + reach, cy + reach, cz + reach);
        var damageSource = world.getDamageSources().explosion((Entity) null, (Entity) null);
        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, box, e -> true)) {
            int dist = (int) Math.round(Math.sqrt(entity.squaredDistanceTo(cx, cy, cz)));
            if (dist > DAMAGE_MAX_DIST) continue;
            float dmg = BASE_DAMAGE - DAMAGE_STEP * dist;
            if (dmg <= 0) continue;
            if (entity == player) {
                dmg = BASE_DAMAGE; // 玩家本体固定 200 点伤害
            }
            entity.damage(damageSource, dmg);
        }
    }

    private static class PlayerState {
        boolean hasSodium = false; // 背包中是否持有钠（每 5 刻刷新一次）
        int fuse = 0;              // 引信剩余刻数，<=0 表示未点燃
    }
}
