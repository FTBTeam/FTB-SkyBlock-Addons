package dev.ftb.ftbsba.tools;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.content.core.AbstractMachineBlock;
import dev.ftb.ftbsba.tools.loot.CrookModifier;
import dev.ftb.ftbsba.tools.loot.HammerModifier;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

@Mod.EventBusSubscriber(modid = FTBSBA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ToolsData {
    public static final String MODID = FTBSBA.MOD_ID;

    @SubscribeEvent
    public static void dataGenEvent(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();

        if (event.includeClient()) {
            SMBlockModels blockModels = new SMBlockModels(gen, MODID, event.getExistingFileHelper());

            gen.addProvider(true, blockModels);
            gen.addProvider(true, new SMLang(gen, MODID, "en_us"));
            gen.addProvider(true, new SMItemModels(gen, MODID, event.getExistingFileHelper()));
            gen.addProvider(true, new SMBlockStateModels(gen, MODID, event.getExistingFileHelper(), blockModels));
        }

        if (event.includeServer()) {
            SMBlockTags blockTags = new SMBlockTags(gen, event.getExistingFileHelper());

            gen.addProvider(true, blockTags);
            gen.addProvider(true, new SMItemTags(gen, blockTags, event.getExistingFileHelper()));
            gen.addProvider(true, new SMRecipes(gen));
            gen.addProvider(true, new SMLootTableProvider(gen));
            gen.addProvider(true, new SMLootModifiers(gen));
        }
    }

    private static class SMLootModifiers extends GlobalLootModifierProvider {
        public SMLootModifiers(DataGenerator gen) {
            super(gen, FTBSBA.MOD_ID);
        }

        @Override
        protected void start() {
            this.add("crook_loot_modifier", new CrookModifier(new LootItemCondition[] {
                    MatchTool.toolMatches(ItemPredicate.Builder.item().of(ToolsTags.Items.CROOKS)).build()
            }));

            this.add("hammer_loot_modifier", new HammerModifier(new LootItemCondition[] {
                    MatchTool.toolMatches(ItemPredicate.Builder.item().of(ToolsTags.Items.HAMMERS)).build()
            }));
        }
    }

    private static class SMLang extends LanguageProvider {
        public SMLang(DataGenerator gen, String modid, String locale) {
            super(gen, modid, locale);
        }

        @Override
        protected void addTranslations() {
            this.add("itemGroup." + MODID, "FTB SkyBlock Addons");
            this.addItem(ToolsRegistry.STONE_HAMMER, "Stone Hammer");
            this.addItem(ToolsRegistry.IRON_HAMMER, "Iron Hammer");
            this.addItem(ToolsRegistry.GOLD_HAMMER, "Gold Hammer");
            this.addItem(ToolsRegistry.DIAMOND_HAMMER, "Diamond Hammer");
            this.addItem(ToolsRegistry.NETHERITE_HAMMER, "Netherite Hammer");
            this.addItem(ToolsRegistry.CROOK, "Stone Crook");
            this.addItem(ToolsRegistry.STONE_ROD, "Stone Rod");

            this.addBlock(ToolsRegistry.IRON_AUTO_HAMMER, "Iron Auto-hammer");
            this.addBlock(ToolsRegistry.GOLD_AUTO_HAMMER, "Gold Auto-hammer");
            this.addBlock(ToolsRegistry.DIAMOND_AUTO_HAMMER, "Diamond Auto-hammer");
            this.addBlock(ToolsRegistry.NETHERITE_AUTO_HAMMER, "Netherite Auto-hammer");

            this.addBlock(ToolsRegistry.FUSING_MACHINE, "SlowMelter 9000");
            this.addBlock(ToolsRegistry.SUPER_COOLER, "\"Super\" Cooler");

            this.add("screens.ftbsba.select_start_group", "Select a group");
            this.add("screens.ftbsba.select_start", "Select a start");
            this.add("screens.ftbsba.selected_start", "Selected start");
            this.add("screens.ftbsba.by", "By: %s");
            this.add("screens.ftbsba.back", "Back");
            this.add("screens.ftbsba.create", "Create");
            this.add("screens.ftbsba.select", "Select");
            this.add("screens.ftbsba.close", "Close");

            this.add("ftbsba.tooltip.fireplow", "Hold right click whilst looking at Stone to create lava");
            this.add("ftbsba.tooltip.hammers", "Crushes materials down to their core components");
            this.add("ftbsba.tooltip.auto-hammers", "Automatically crushes materials down using the hammer based on the tier of hammer");
            this.add("ftbsba.tooltip.energy", "Energy: %s FE");
            this.add("ftbsba.tooltip.fluid", "Fluid: %smB %s");
            this.add("ftbsba.tooltip.slowmelter", "This is what happens when 'The Boss' decides to get involved at the last minute.");

            this.add("ftbsba.tooltip.fusing_machine", "Used to fuse items together to produce new results");
            this.add("ftbsba.tooltip.super_cooler", "Used to \"super\"-cool items to produce new results");

            this.add("ftbsba.jade.waiting", "Waiting for input: %s ticks");
            this.add("ftbsba.jade.processing", "Processing: %s/%s");
            this.add("ftbsba.jade.input", "Input");
            this.add("ftbsba.jade.buffer", "Buffer");

            this.add("config.jade.plugin_ftbsba.blocks", "FTB Skyblock Addons Blocks");
            this.add("container.ftbsba.super_cooler", "\"Super\" Cooler");
            this.add("container.ftbsba.fusing_machine", "SlowMelter 9000");

            this.add("ftbsba.jei.recipe.fusing", "SlowMelter 9000");
            this.add("ftbsba.jei.recipe.super_cooler", "\"Super\" Cooling");
            this.add("ftbsba.jei.recipe.hammer", "Hammering");
            this.add("ftbsba.jei.recipe.crook", "Crooks");
        }
    }

    private static class SMBlockStateModels extends BlockStateProvider {
        private static final List<DirRotation> HORIZONTALS = Util.make(new ArrayList<>(), l -> {
            l.add(new DirRotation(Direction.NORTH, 0));
            l.add(new DirRotation(Direction.EAST, 90));
            l.add(new DirRotation(Direction.SOUTH, 180));
            l.add(new DirRotation(Direction.WEST, 270));
        });

        private final SMBlockModels blockModels;

        public SMBlockStateModels(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper, SMBlockModels bm) {
            super(generator, modid, existingFileHelper);
            this.blockModels = bm;
        }

        @Override
        public BlockModelProvider models() {
            return this.blockModels;
        }

        @Override
        protected void registerStatesAndModels() {
            for (var block : List.of(ToolsRegistry.IRON_AUTO_HAMMER, ToolsRegistry.GOLD_AUTO_HAMMER, ToolsRegistry.DIAMOND_AUTO_HAMMER, ToolsRegistry.NETHERITE_AUTO_HAMMER)) {
                MultiPartBlockStateBuilder b = getMultipartBuilder(block.get());
                String path = block.getId().getPath();
                for (DirRotation d : HORIZONTALS) {
                    b.part().modelFile(models().getExistingFile(modLoc("block/" + path)))
                            .rotationY(d.rotation).addModel().condition(AbstractMachineBlock.ACTIVE, false)
                            .condition(HORIZONTAL_FACING, d.direction);
                    b.part().modelFile(models().getExistingFile(modLoc("block/" + path + "_active")))
                            .rotationY(d.rotation).addModel().condition(AbstractMachineBlock.ACTIVE, true)
                            .condition(HORIZONTAL_FACING, d.direction);
                }
            }

            for (var block: List.of(ToolsRegistry.FUSING_MACHINE, ToolsRegistry.SUPER_COOLER)) {
                var model = machineModel(block, false);
                var activeModel = machineModel(block, true);
                VariantBlockStateBuilder.PartialBlockstate builder = getVariantBuilder(block.get()).partialState();
                for (DirRotation d : HORIZONTALS) {
                    builder.with(HORIZONTAL_FACING, d.direction).with(AbstractMachineBlock.ACTIVE, false)
                            .setModels(new ConfiguredModel(model, 0, d.rotation, false));
                    builder.with(HORIZONTAL_FACING, d.direction).with(AbstractMachineBlock.ACTIVE, true)
                            .setModels(new ConfiguredModel(activeModel, 0, d.rotation, false));
                }
                simpleBlockItem(block.get(), model);
            }
        }

        private ModelFile machineModel(RegistryObject<Block> block, boolean active) {
            String name = block.getId().getPath();
            String suffix = active ? "_active" : "";
            return models().withExistingParent(name + suffix, "block/orientable")
                    .texture("top", modLoc("block/" + name + "_top" + suffix))
                    .texture("side", modLoc("block/generic_machine_side"))
                    .texture("front", modLoc("block/" + name + "_front" + suffix));
        }
    }

    private record DirRotation(Direction direction, int rotation) {
    }

    private static class SMBlockModels extends BlockModelProvider {
        public SMBlockModels(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
            super(generator, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
        }
    }

    private static class SMItemModels extends ItemModelProvider {
        public SMItemModels(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
            super(generator, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            this.registerBlockModel(ToolsRegistry.IRON_AUTO_HAMMER);
            this.registerBlockModel(ToolsRegistry.GOLD_AUTO_HAMMER);
            this.registerBlockModel(ToolsRegistry.DIAMOND_AUTO_HAMMER);
            this.registerBlockModel(ToolsRegistry.NETHERITE_AUTO_HAMMER);

            this.simpleItem(ToolsRegistry.STONE_HAMMER);
            this.simpleItem(ToolsRegistry.IRON_HAMMER);
            this.simpleItem(ToolsRegistry.GOLD_HAMMER);
            this.simpleItem(ToolsRegistry.DIAMOND_HAMMER);
            this.simpleItem(ToolsRegistry.NETHERITE_HAMMER);
            this.simpleItem(ToolsRegistry.CROOK);
            this.simpleItem(ToolsRegistry.STONE_ROD);
        }

        private void simpleItem(RegistryObject<Item> item) {
            String path = item.getId().getPath();
            this.singleTexture(path, this.mcLoc("item/handheld"), "layer0", this.modLoc("item/" + path));
        }

        private void registerBlockModel(RegistryObject<Block> block) {
            String path = block.getId().getPath();
            this.getBuilder(path).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + path)));
        }
    }

    private static class SMBlockTags extends BlockTagsProvider {
        public SMBlockTags(DataGenerator generatorIn, ExistingFileHelper helper) {
            super(generatorIn, FTBSBA.MOD_ID, helper);
        }

        @Override
        protected void addTags() {
            Block[] blocks = Set.of(
                    ToolsRegistry.IRON_AUTO_HAMMER.get(),
                    ToolsRegistry.GOLD_AUTO_HAMMER.get(),
                    ToolsRegistry.DIAMOND_AUTO_HAMMER.get(),
                    ToolsRegistry.NETHERITE_AUTO_HAMMER.get()
            ).toArray(Block[]::new);

            this.tag(Tags.Blocks.NEEDS_WOOD_TOOL).add(blocks);
            this.tag(ToolsTags.Blocks.AUTO_HAMMERS).add(blocks);
        }
    }

    private static class SMItemTags extends ItemTagsProvider {
        public SMItemTags(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, ExistingFileHelper helper) {
            super(dataGenerator, blockTagProvider, FTBSBA.MOD_ID, helper);
        }

        @Override
        protected void addTags() {
            this.tag(ToolsTags.Items.HAMMERS).add(
                    ToolsRegistry.STONE_HAMMER.get(),
                    ToolsRegistry.IRON_HAMMER.get(),
                    ToolsRegistry.GOLD_HAMMER.get(),
                    ToolsRegistry.DIAMOND_HAMMER.get(),
                    ToolsRegistry.NETHERITE_HAMMER.get()
            );

            this.tag(ToolsTags.Items.CROOKS).add(
                    ToolsRegistry.CROOK.get()
            );
        }
    }

    private static class SMRecipes extends RecipeProvider {
        public SMRecipes(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
            //Hammer
            this.hammer(ToolsRegistry.STONE_HAMMER.get(), Items.COBBLESTONE, consumer);
            this.hammer(ToolsRegistry.IRON_HAMMER.get(), Items.IRON_INGOT, consumer);
            this.hammer(ToolsRegistry.GOLD_HAMMER.get(), Items.GOLD_INGOT, consumer);
            this.hammer(ToolsRegistry.DIAMOND_HAMMER.get(), Items.DIAMOND, consumer);
            this.hammer(ToolsRegistry.NETHERITE_HAMMER.get(), Items.NETHERITE_INGOT, consumer);

            ShapedRecipeBuilder.shaped(ToolsRegistry.STONE_ROD.get(), 2)
                    .unlockedBy("has_item", has(Items.COBBLESTONE))
                    .pattern("S")
                    .pattern("S")
                    .define('S', Items.COBBLESTONE)
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ToolsRegistry.CROOK.get(), 1)
                    .unlockedBy("has_item", has(ToolsRegistry.STONE_ROD.get()))
                    .pattern("RR ")
                    .pattern(" R ")
                    .pattern(" R ")
                    .define('R', ToolsRegistry.STONE_ROD.get())
                    .save(consumer);

            ShapedRecipeBuilder.shaped(ToolsRegistry.IRON_AUTO_HAMMER.get())
                    .unlockedBy("has_item", has(ToolsRegistry.IRON_HAMMER.get()))
                    .pattern("IGI")
                    .pattern("XHX")
                    .pattern("RGR")
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('X', Tags.Items.GLASS)
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('G', Tags.Items.INGOTS_GOLD)
                    .define('H', ToolsRegistry.IRON_HAMMER.get())
                    .save(consumer);

            autoHammer(ToolsRegistry.GOLD_AUTO_HAMMER.get(), ToolsRegistry.IRON_AUTO_HAMMER_BLOCK_ITEM.get(), ToolsRegistry.GOLD_HAMMER.get(), consumer);
            autoHammer(ToolsRegistry.DIAMOND_AUTO_HAMMER.get(), ToolsRegistry.GOLD_AUTO_HAMMER_BLOCK_ITEM.get(), ToolsRegistry.DIAMOND_HAMMER.get(), consumer);
            autoHammer(ToolsRegistry.NETHERITE_AUTO_HAMMER.get(), ToolsRegistry.DIAMOND_AUTO_HAMMER_BLOCK_ITEM.get(), ToolsRegistry.NETHERITE_HAMMER.get(), consumer);
        }

        private void autoHammer(ItemLike output, Item center, Item top, Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(output)
                    .unlockedBy("has_item", has(center))
                    .pattern("ITI")
                    .pattern("XCX")
                    .pattern("RGR")
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('G', Tags.Items.INGOTS_GOLD)
                    .define('X', Tags.Items.GLASS)
                    .define('T', top)
                    .define('C', center)
                    .save(consumer);

        }

        private void hammer(ItemLike output, TagKey<Item> head, Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(output)
                    .unlockedBy("has_item", has(head))
                    .pattern("hrh")
                    .pattern(" r ")
                    .pattern(" r ")
                    .define('h', head)
                    .define('r', ToolsRegistry.STONE_ROD.get())
                    .save(consumer);
        }

        private void hammer(ItemLike output, ItemLike head, Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(output)
                    .unlockedBy("has_item", has(head))
                    .pattern("hrh")
                    .pattern(" r ")
                    .pattern(" r ")
                    .define('h', head)
                    .define('r', ToolsRegistry.STONE_ROD.get())
                    .save(consumer);
        }
    }

    private static class SMLootTableProvider extends LootTableProvider {
        public SMLootTableProvider(DataGenerator dataGeneratorIn) {
            super(dataGeneratorIn);
        }

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
            return Lists.newArrayList(Pair.of(SMBlockLootProvider::new, LootContextParamSets.BLOCK));
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext tracker) {
            map.forEach((k, v) -> LootTables.validate(tracker, k, v));
        }
    }

    public static class SMBlockLootProvider extends BlockLoot {
        Set<Block> blocks = new HashSet<>();

        @Override
        protected void addTables() {
            dropSelf(ToolsRegistry.IRON_AUTO_HAMMER.get());
            dropSelf(ToolsRegistry.GOLD_AUTO_HAMMER.get());
            dropSelf(ToolsRegistry.DIAMOND_AUTO_HAMMER.get());
            dropSelf(ToolsRegistry.NETHERITE_AUTO_HAMMER.get());

            preserveContents(ToolsRegistry.FUSING_MACHINE.get(), ToolsRegistry.FUSING_MACHINE_BLOCK_ENTITY.get());
            preserveContents(ToolsRegistry.SUPER_COOLER.get(), ToolsRegistry.SUPER_COOLER_BLOCK_ENTITY.get());
        }

        private void preserveContents(Block block, BlockEntityType<?> blockEntity) {
            LootPool.Builder builder = LootPool.lootPool()
                    .when(ExplosionCondition.survivesExplosion())
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(block)
                            .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                    .copy("energy", "BlockEntityTag.energy", CopyNbtFunction.MergeStrategy.REPLACE)
                                    .copy("fluid", "BlockEntityTag.fluid", CopyNbtFunction.MergeStrategy.REPLACE))
//                            .apply(SetContainerContents.setContents(blockEntity)
//                                    .withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS))));
                    );
            add(block, LootTable.lootTable().withPool(builder));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return blocks;
        }

        @Override
        protected void add(Block blockIn, LootTable.Builder table) {
            blocks.add(blockIn);
            super.add(blockIn, table);
        }
    }
}
