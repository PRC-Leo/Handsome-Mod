package handsome.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.EmptyFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.List;

public class ModWorldGen {
    private static final Identifier FEATURE_ID = new Identifier("handsome-mod", "sodium_ore_replace");

    public static final Feature<EmptyFeatureConfig> SODIUM_ORE_REPLACE_FEATURE =
            Registry.register(Registry.FEATURE, FEATURE_ID,
                    new SodiumOreReplaceFeature(EmptyFeatureConfig.CODEC));

    public static void generateModWorldGen() {
        // 配置的矿床（无额外配置，替换逻辑在 Feature 内部）
        ConfiguredFeature<?, ?> configured = new ConfiguredFeature<>(SODIUM_ORE_REPLACE_FEATURE, EmptyFeatureConfig.INSTANCE);
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, FEATURE_ID, configured);

        // 放置的矿床：不加任何 placement 修饰（每区块触发一次扫描替换）
        PlacedFeature placed = new PlacedFeature(RegistryEntry.of(configured), List.of());
        Registry.register(BuiltinRegistries.PLACED_FEATURE, FEATURE_ID, placed);

        // 在主世界、矿石装饰之后执行，确保铁矿（UNDERGROUND_ORES）已生成
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, FEATURE_ID));
    }
}
