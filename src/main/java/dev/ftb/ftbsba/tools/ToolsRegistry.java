package dev.ftb.ftbsba.tools;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.content.CrookItem;
import dev.ftb.ftbsba.tools.content.HammerItem;
import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerBlock;
import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerBlockEntity;
import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerProperties;
import dev.ftb.ftbsba.tools.content.fusion.FusingMachineBlock;
import dev.ftb.ftbsba.tools.content.fusion.FusingMachineBlockEntity;
import dev.ftb.ftbsba.tools.content.fusion.FusingMachineContainer;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerBlock;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerBlockEntity;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerContainer;
import dev.ftb.ftbsba.tools.loot.CrookModifier;
import dev.ftb.ftbsba.tools.loot.HammerModifier;
import dev.ftb.ftbsba.tools.recipies.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
    DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FTBSBA.MOD_ID);
    DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FTBSBA.MOD_ID);
    DeferredRegister<RecipeType<?>> RECIPE_TYPE_REGISTRY = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, FTBSBA.MOD_ID);
    DeferredRegister<MenuType<?>> CONTAINER_REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, FTBSBA.MOD_ID);

    // All the registries :D
    List<DeferredRegister<?>> REGISTERS = List.of(
            ITEM_REGISTRY,
            BLOCK_REGISTRY,
            BLOCK_ENTITY_REGISTRY,
            LOOT_MODIFIERS_REGISTRY,
            RECIPE_SERIALIZER_REGISTRY,
            RECIPE_TYPE_REGISTRY,
            CONTAINER_REGISTRY
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

    RegistryObject<Block> FUSING_MACHINE = BLOCK_REGISTRY.register("fusing_machine", FusingMachineBlock::new);
    RegistryObject<Block> SUPER_COOLER = BLOCK_REGISTRY.register("super_cooler", SuperCoolerBlock::new);

    RegistryObject<BlockItem> IRON_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("iron_auto_hammer", () -> new ToolTipBlockItem(IRON_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), Component.translatable("ftbsba.tooltip.auto-hammers").withStyle(ChatFormatting.GRAY)));
    RegistryObject<BlockItem> GOLD_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("gold_auto_hammer", () -> new ToolTipBlockItem(GOLD_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), Component.translatable("ftbsba.tooltip.auto-hammers").withStyle(ChatFormatting.GRAY)));
    RegistryObject<BlockItem> DIAMOND_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("diamond_auto_hammer", () -> new ToolTipBlockItem(DIAMOND_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), Component.translatable("ftbsba.tooltip.auto-hammers").withStyle(ChatFormatting.GRAY)));
    RegistryObject<BlockItem> NETHERITE_AUTO_HAMMER_BLOCK_ITEM = ITEM_REGISTRY.register("netherite_auto_hammer", () -> new ToolTipBlockItem(NETHERITE_AUTO_HAMMER.get(), new Item.Properties().tab(CREATIVE_GROUP), Component.translatable("ftbsba.tooltip.auto-hammers").withStyle(ChatFormatting.GRAY)));

    RegistryObject<BlockItem> FUSING_MACHINE_BLOCK_ITEM = ITEM_REGISTRY.register("fusing_machine", () -> new ToolTipBlockItem(FUSING_MACHINE.get(), new Item.Properties().tab(CREATIVE_GROUP), List.of(
            Component.translatable("ftbsba.tooltip.fusing_machine").withStyle(ChatFormatting.GRAY),
            Component.translatable("ftbsba.tooltip.slowmelter").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC)
    )));

    RegistryObject<BlockItem> SUPER_COOLER_BLOCK_ITEM = ITEM_REGISTRY.register("super_cooler", () -> new ToolTipBlockItem(SUPER_COOLER.get(), new Item.Properties().tab(CREATIVE_GROUP), Component.translatable("ftbsba.tooltip.super_cooler").withStyle(ChatFormatting.GRAY)));

    RegistryObject<MenuType<FusingMachineContainer>> FUSING_MACHINE_CONTAINER = CONTAINER_REGISTRY.register("fusing_machine", () -> IForgeMenuType.create(FusingMachineContainer::new));
    RegistryObject<MenuType<SuperCoolerContainer>> SUPER_COOLER_CONTAINER = CONTAINER_REGISTRY.register("super_cooler", () -> IForgeMenuType.create(SuperCoolerContainer::new));

    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Iron>> IRON_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("iron_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Iron::new, IRON_AUTO_HAMMER.get()).build(null));
    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Gold>> GOLD_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("gold_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Gold::new, GOLD_AUTO_HAMMER.get()).build(null));
    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Diamond>> DIAMOND_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("diamond_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Diamond::new, DIAMOND_AUTO_HAMMER.get()).build(null));
    RegistryObject<BlockEntityType<AutoHammerBlockEntity.Netherite>> NETHERITE_AUTO_HAMMER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("netherite_auto_hammer", () -> BlockEntityType.Builder.of(AutoHammerBlockEntity.Netherite::new, NETHERITE_AUTO_HAMMER.get()).build(null));

    RegistryObject<BlockEntityType<FusingMachineBlockEntity>> FUSING_MACHINE_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("fusing_machine", () -> BlockEntityType.Builder.of(FusingMachineBlockEntity::new, FUSING_MACHINE.get()).build(null));
    RegistryObject<BlockEntityType<SuperCoolerBlockEntity>> SUPER_COOLER_BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register("super_cooler", () -> BlockEntityType.Builder.of(SuperCoolerBlockEntity::new, SUPER_COOLER.get()).build(null));


    RegistryObject<Codec<? extends IGlobalLootModifier>> HAMMER_LOOT_MODIFIER = LOOT_MODIFIERS_REGISTRY.register("hammer_loot_modifier", () -> HammerModifier.CODEC);
    RegistryObject<Codec<? extends IGlobalLootModifier>> CROOK_LOOT_MODIFIER = LOOT_MODIFIERS_REGISTRY.register("crook_loot_modifier", () -> CrookModifier.CODEC);

    RegistryObject<RecipeType<CrookRecipe>> CROOK_RECIPE_TYPE = RECIPE_TYPE_REGISTRY.register("crook", () -> new RecipeType<>() {});
    RegistryObject<RecipeSerializer<?>> CROOK_RECIPE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register("crook", CrookRecipeSerializer::new);

    RegistryObject<RecipeType<HammerRecipe>> HAMMER_RECIPE_TYPE = RECIPE_TYPE_REGISTRY.register("hammer", () -> new RecipeType<>() {});
    RegistryObject<RecipeSerializer<?>> HAMMER_RECIPE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register("hammer", HammerRecipeSerializer::new);

    RegistryObject<RecipeType<SuperCoolerRecipe>> SUPER_COOLER_RECIPE_TYPE = RECIPE_TYPE_REGISTRY.register("super_cooler", () -> new RecipeType<>() {});
    RegistryObject<RecipeSerializer<?>> SUPER_COOLER_RECIPE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register("super_cooler", SuperCoolerRecipeSerializer::new);

    RegistryObject<RecipeType<FusingMachineRecipe>> FUSING_MACHINE_RECIPE_TYPE = RECIPE_TYPE_REGISTRY.register("fusing_machine", () -> new RecipeType<>() {});
    RegistryObject<RecipeSerializer<?>> FUSING_MACHINE_RECIPE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register("fusing_machine", FusingMachineRecipeSerializer::new);


    class ToolTipBlockItem extends BlockItem {
        private final List<Component> tooltip;

        public ToolTipBlockItem(Block arg, Properties arg2, List<Component> tooltip) {
            super(arg, arg2);
            this.tooltip = tooltip;
        }

        public ToolTipBlockItem(Block arg, Properties arg2, Component tooltip) {
            super(arg, arg2);
            this.tooltip = Collections.singletonList(tooltip);
        }

        @Override
        public void appendHoverText(ItemStack arg, @Nullable Level arg2, List<Component> list, TooltipFlag arg3) {
            list.addAll(tooltip);

            // call superclass so blocks can add extra info, e.g. stored power/fluid
            super.appendHoverText(arg, arg2, list, arg3);
        }
    }
}
