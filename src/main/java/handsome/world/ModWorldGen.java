package handsome.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class ModWorldGen {
    private static final Identifier FEATURE_ID = new Identifier("handsome-mod", "sodium_ore_replace");

    // 自定义 Feature 仍需在 Java 中注册（FEATURE 注册表引用走 Registries.FEATURE，1.20.1 可用）。
    // 注意：1.20.1 的 yarn(1.20.1+build.10) 存在映射缺口——
    //   CONFIGURED_FEATURE / PLACED_FEATURE 这类动态注册表字段无法从 Registries/BuiltinRegistries 解析，
    //   因此 configured / placed 特征改由数据包 JSON 注册（见 resources/data/handsome-mod/worldgen/）。
    public static final Feature<DefaultFeatureConfig> SODIUM_ORE_REPLACE_FEATURE =
            Registry.register(Registries.FEATURE, FEATURE_ID,
                    new SodiumOreReplaceFeature(DefaultFeatureConfig.CODEC));

    public static void generateModWorldGen() {
        // 在主世界、矿石装饰之后（UNDERGROUND_DECORATION 阶段）加入我们的放置特征。
        // placed_feature 的注册键与上面的 FEATURE_ID 同名，由 JSON 数据包注册。
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, FEATURE_ID));
    }
}
