package dev.ftb.ftbsba.tools;

import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.content.CrookItem;
import dev.ftb.ftbsba.tools.content.HammerItem;
import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerBlock;
import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerBlockEntity;
import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerProperties;
import dev.ftb.ftbsba.tools.loot.CrookModifier;
import dev.ftb.ftbsba.tools.loot.HammerModifier;
import dev.ftb.ftbsba.tools.recipies.CrookRecipe;
import dev.ftb.ftbsba.tools.recipies.CrookRecipeSerializer;
import dev.ftb.ftbsba.tools.recipies.HammerRecipe;
import dev.ftb.ftbsba.tools.recipies.HammerRecipeSerializer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ToolsRegistry {
    CreativeModeTab CREATIVE_GROUP = new CreativeModeTab(FTBSBA.MOD_ID) {
        @Override
        @OnlyIn(Dist.CLIENT)
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(IRON_HAMMER.get());
        }
    };

    DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, FTBSBA.MOD_ID);
    DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, FTBSBA.MOD_ID);
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FTBSBA.MOD_ID);
    DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIERS_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FTBSBA.MOD_ID);
    DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FTBSBA.MOD_ID);
    DeferredRegister<RecipeType<?>> RECIPE_TYPE_REGISTRY = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, FTBSBA.MOD_ID);

    // All the registries :D
    List<DeferredRegister<?>> REGISTERS = List.of(
            ITEM_REGISTRY,
            BLOCK_REGISTRY,
            BLOCK_ENTITY_REGISTRY,
            LOOT_MODIFIERS_REGISTRY,
            RECIPE_SERIALIZER_REGISTRY,
            RECIPE_TYPE_REGISTRY
    );

    // Hammers
    RegistryObject<Item> STONE_HAMMER = ITEM_REGISTRY.register("stone_hammer", () -> new HammerItem(Tiers.STONE, 1, -2.8F, new Item.Properties().tab(CREATIVE_GROUP)));
    RegistryObject<Item> IRON_HAMMER = ITEM_REGISTRY.register("iron_hammer", () -> new HammerItem(Tiers.IRON, 1, -2.8F, new Item.Properties().tab(CREATIVE_GROUP)));
    RegistryObject<Item> GOLD_HAMMER = ITEM_REGISTRY.register("gold_hammer", () -> new HammerItem(Tiers.GOLD, 1, -2.8F, new Item.Properties().tab(CREATIVE_GROUP)));
    RegistryObject<Item> DIAMOND_HAMMER = ITEM_REGISTRY.register("diamond_hammer", () -> new HammerItem(Tiers.DIAMOND, 1, -2.8F, new Item.Properties().tab(CREATIVE_GROUP)));
    RegistryObject<Item> NETHERITE_HAMMER = ITEM_REGISTRY.register("netherite_hammer", () -> new HammerItem(Tiers.NETHERITE, 1, -2.8F, new Item.Properties().tab(CREATIVE_GROUP).fireResistant()));

    //MISC
    RegistryObject<Item> STONE_ROD = ITEM_REGISTRY.register("stone_rod", () -> new Item(new Item.Properties().tab(CREATIVE_GROUP)));
    RegistryObject<Item> CROOK = ITEM_REGISTRY.register("stone_crook", () -> new CrookItem(2, -2.8F, Tiers.STONE, new Item.Properties().tab(CREATIVE_GROUP)));

    RegistryObject<Block> IRON_AUTO_HAMMER = BLOCK_REGISTRY.register("iron_auto_hammer", () -> new AutoHammerBlock(IRON_HAMMER, AutoHammerProperties.IRON));
    RegistryObject<Block> GOLD_AUTO_HAMMER = BLOCK_REGISTRY.register("gold_auto_hammer", () -> new AutoHammerBlock(GOLD_HAMMER, AutoHammerProperties.GOLD));
    RegistryObject<Block> DIAMOND_AUTO_HAMMER = BLOCK_REGISTRY.register("diamond_auto_hammer", () -> new AutoHammerBlock(DIAMOND_HAMMER, AutoHammerProperties.DIAMOND));
    RegistryObject<Block> NETHERITE_AUTO_HAMMER = BLOCK_REGISTRY.register("netherite_auto_hammer", () -> new AutoHammerBlock(NETHERITE_HAMMER, AutoHammerProperties.NETHERITE));

    RegistryObject<BlockItem> IRON_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("iron_auto_hammer", () -> new ToolTipBlockItem(IRON_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), new TranslatableComponent("ftbsbc.tooltip.auto-hammers").gray()));
    RegistryObject<BlockItem> GOLD_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("gold_auto_hammer", () -> new ToolTipBlockItem(GOLD_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), new TranslatableComponent("ftbsbc.tooltip.auto-hammers").gray()));
    RegistryObject<BlockItem> DIAMOND_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("diamond_auto_hammer", () -> new ToolTipBlockItem(DIAMOND_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), new TranslatableComponent("ftbsbc.tooltip.auto-hammers").gray()));
    RegistryObject<BlockItem> NETHERITE_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("netherite_auto_hammer", () -> new ToolTipBlockItem(NETHERITE_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), new TranslatableComponent("ftbsbc.tooltip.auto-hammers").gray()));

    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Iron>> IRON_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("iron_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Iron::new, IRON_AUTO_HAMMER.get()).build(null));
    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Gold>> GOLD_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("gold_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Gold::new, GOLD_AUTO_HAMMER.get()).build(null));
    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Diamond>> DIAMOND_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("diamond_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Diamond::new, DIAMOND_AUTO_HAMMER.get()).build(null));
    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Netherite>> NETHERITE_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("netherite_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Netherite::new, NETHERITE_AUTO_HAMMER.get()).build(null));

    RegistryObject<GlobalLootModifierSerializer<HammerModifier>> HAMMER_LOOT_MODIFIER = LOOT_MODIFIERS_REGISTRY.register("hammer_loot_modifier", HammerModifier.Serializer::new);
    RegistryObject<GlobalLootModifierSerializer<CrookModifier>> CROOK_LOOT_MODIFIER = LOOT_MODIFIERS_REGISTRY.register("crook_loot_modifier", CrookModifier.Serializer::new);

    RegistryObject<RecipeSerializer<?>> CROOK_RECIPE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register("crook", CrookRecipeSerializer::new);
    RegistryObject<RecipeType<CrookRecipe>> CROOK_RECIPE_TYPE = RECIPE_TYPE_REGISTRY.register("crook", () -> new RecipeType<>() {});
    RegistryObject<RecipeSerializer<?>> HAMMER_RECIPE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register("hammer", HammerRecipeSerializer::new);
    RegistryObject<RecipeType<HammerRecipe>> HAMMER_RECIPE_TYPE = RECIPE_TYPE_REGISTRY.register("hammer", () -> new RecipeType<>() {});

    class ToolTipBlockItem extends BlockItem {
        private final Component tooltip;

        public ToolTipBlockItem(Block arg, Properties arg2, Component tooltip) {
            super(arg, arg2);
            this.tooltip = tooltip;
        }

        @Override
        public void appendHoverText(ItemStack arg, @Nullable Level arg2, List<Component> list, TooltipFlag arg3) {
            list.add(tooltip);
        }
    }
}
