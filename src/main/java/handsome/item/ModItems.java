package handsome.item;

import handsome.HandsomeMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModItems {
    // 钠（基础材料，沿用铁锭材质）
    public static final Item SODIUM = register("sodium", new Item(new Item.Settings()));

    // 钠盔甲材质：数值完全对齐铁，但修复材料为钠，且渲染套用铁的原版盔甲贴图（getName = "iron"）
    private static final ArmorMaterial SODIUM_ARMOR_MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurability(ArmorItem.Type type) {
            return ArmorMaterials.IRON.getDurability(type);
        }

        @Override
        public int getProtection(ArmorItem.Type type) {
            return ArmorMaterials.IRON.getProtection(type);
        }

        @Override
        public int getEnchantability() {
            return ArmorMaterials.IRON.getEnchantability();
        }

        @Override
        public SoundEvent getEquipSound() {
            return ArmorMaterials.IRON.getEquipSound();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(SODIUM);
        }

        @Override
        public String getName() {
            // 复用铁盔甲的原版 3D 贴图，无需另做盔甲材质
            return "iron";
        }

        @Override
        public float getToughness() {
            return ArmorMaterials.IRON.getToughness();
        }

        @Override
        public float getKnockbackResistance() {
            return ArmorMaterials.IRON.getKnockbackResistance();
        }
    };

    // 钠工具材质：数值完全对齐铁（耐久 250 / 挖掘速度 6.0 / 挖掘等级 2 / 附魔 14），
    // 基础攻击力固定为 3（配合剑 +3 = 6，等同铁），修复材料为钠
    private static final ToolMaterial SODIUM_TOOL_MATERIAL = new ToolMaterial() {
        @Override
        public int getDurability() {
            return ToolMaterials.IRON.getDurability();
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return ToolMaterials.IRON.getMiningSpeedMultiplier();
        }

        @Override
        public float getAttackDamage() {
            return 3.0F;
        }

        @Override
        public int getMiningLevel() {
            return ToolMaterials.IRON.getMiningLevel();
        }

        @Override
        public int getEnchantability() {
            return ToolMaterials.IRON.getEnchantability();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(SODIUM);
        }
    };

    // ===== 钠盔甲四件套（属性等同铁盔甲）=====
    public static final Item SODIUM_HELMET = register("sodium_helmet",
            new ArmorItem(SODIUM_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings()));
    public static final Item SODIUM_CHESTPLATE = register("sodium_chestplate",
            new ArmorItem(SODIUM_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings()));
    public static final Item SODIUM_LEGGINGS = register("sodium_leggings",
            new ArmorItem(SODIUM_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Settings()));
    public static final Item SODIUM_BOOTS = register("sodium_boots",
            new ArmorItem(SODIUM_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings()));

    // ===== 钠工具五件（属性等同铁工具）=====
    public static final Item SODIUM_SWORD = register("sodium_sword",
            new SwordItem(SODIUM_TOOL_MATERIAL, 3, -2.4F, new Item.Settings()));
    public static final Item SODIUM_PICKAXE = register("sodium_pickaxe",
            new PickaxeItem(SODIUM_TOOL_MATERIAL, 1, -2.8F, new Item.Settings()));
    public static final Item SODIUM_AXE = register("sodium_axe",
            new AxeItem(SODIUM_TOOL_MATERIAL, 6.0F, -3.2F, new Item.Settings()));
    public static final Item SODIUM_SHOVEL = register("sodium_shovel",
            new ShovelItem(SODIUM_TOOL_MATERIAL, 1.5F, -3.0F, new Item.Settings()));
    public static final Item SODIUM_HOE = register("sodium_hoe",
            new HoeItem(SODIUM_TOOL_MATERIAL, -3, -2.0F, new Item.Settings()));

    public static Item register(String id, Item item) {
        return register(new Identifier(HandsomeMod.MOD_ID, id), item);
    }

    public static Item register(Identifier id, Item item) {
        return register(RegistryKey.of(Registries.ITEM.getKey(), id), item);
    }

    public static Item register(RegistryKey<Item> key, Item item) {
        if (item instanceof BlockItem) {
            ((BlockItem) item).appendBlocks(Item.BLOCK_ITEMS, item);
        }

        return Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModItems() {
        HandsomeMod.LOGGER.info("Registering Mod Items for " + HandsomeMod.MOD_ID);

        // 材料
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(entries -> entries.add(SODIUM));

        // 战斗（武器 + 盔甲）
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(entries -> {
                    entries.add(SODIUM_SWORD);
                    entries.add(SODIUM_HELMET);
                    entries.add(SODIUM_CHESTPLATE);
                    entries.add(SODIUM_LEGGINGS);
                    entries.add(SODIUM_BOOTS);
                });

        // 工具
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> {
                    entries.add(SODIUM_PICKAXE);
                    entries.add(SODIUM_AXE);
                    entries.add(SODIUM_SHOVEL);
                    entries.add(SODIUM_HOE);
                });
    }
}
