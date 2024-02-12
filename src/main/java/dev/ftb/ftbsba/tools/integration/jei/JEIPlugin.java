package dev.ftb.ftbsba.tools.integration.jei;

import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.fusion.FusingMachineContainer;
import dev.ftb.ftbsba.tools.content.fusion.FusingMachineScreen;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerContainer;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerScreen;
import dev.ftb.ftbsba.tools.recipies.NoInventory;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation FTBSBTOOLS_JEI = new ResourceLocation(FTBSBA.MOD_ID, "jei");
    public static HashSet<RegistryObject<? extends Item>> HAMMERS = new LinkedHashSet<>() {{
        this.add(ToolsRegistry.STONE_HAMMER);
        this.add(ToolsRegistry.IRON_HAMMER);
        this.add(ToolsRegistry.GOLD_HAMMER);
        this.add(ToolsRegistry.DIAMOND_HAMMER);
        this.add(ToolsRegistry.NETHERITE_HAMMER);
        this.add(ToolsRegistry.IRON_AUTO_HAMMER_BLOCK_ITEM);
        this.add(ToolsRegistry.GOLD_AUTO_HAMMER_BLOCK_ITEM);
        this.add(ToolsRegistry.DIAMOND_AUTO_HAMMER_BLOCK_ITEM);
        this.add(ToolsRegistry.NETHERITE_AUTO_HAMMER_BLOCK_ITEM);
    }};

    public static HashSet<RegistryObject<Item>> CROOKS = new HashSet<>() {{
        this.add(ToolsRegistry.CROOK);
    }};

    @Override
    public ResourceLocation getPluginUid() {
        return FTBSBTOOLS_JEI;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        r.addRecipeCategories(new HammerCategory(r.getJeiHelpers().getGuiHelper()));
        r.addRecipeCategories(new CrookCategory(r.getJeiHelpers().getGuiHelper()));
        r.addRecipeCategories(new FusingMachineCategory(r.getJeiHelpers().getGuiHelper()));
        r.addRecipeCategories(new SuperCoolerCategory(r.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        Level level = Minecraft.getInstance().level;
        r.addRecipes(HammerCategory.TYPE, level.getRecipeManager().getRecipesFor(ToolsRegistry.HAMMER_RECIPE_TYPE.get(), NoInventory.INSTANCE, level));
        r.addRecipes(CrookCategory.TYPE, level.getRecipeManager().getRecipesFor(ToolsRegistry.CROOK_RECIPE_TYPE.get(), NoInventory.INSTANCE, level));
        r.addRecipes(FusingMachineCategory.TYPE, level.getRecipeManager().getRecipesFor(ToolsRegistry.FUSING_MACHINE_RECIPE_TYPE.get(), NoInventory.INSTANCE, level));
        r.addRecipes(SuperCoolerCategory.TYPE, level.getRecipeManager().getRecipesFor(ToolsRegistry.SUPER_COOLER_RECIPE_TYPE.get(), NoInventory.INSTANCE, level));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        HAMMERS.forEach(hammer -> r.addRecipeCatalyst(new ItemStack(hammer.get()), HammerCategory.TYPE));
        CROOKS.forEach(crook -> r.addRecipeCatalyst(new ItemStack(crook.get()), CrookCategory.TYPE));

        r.addRecipeCatalyst(new ItemStack(ToolsRegistry.FUSING_MACHINE_BLOCK_ITEM.get()), FusingMachineCategory.TYPE);
        r.addRecipeCatalyst(new ItemStack(ToolsRegistry.SUPER_COOLER_BLOCK_ITEM.get()), SuperCoolerCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                SuperCoolerContainer.class,
                ToolsRegistry.SUPER_COOLER_CONTAINER.get(),
                SuperCoolerCategory.TYPE,
                36,
                3,
                0,
                36
        );

        registration.addRecipeTransferHandler(
                FusingMachineContainer.class,
                ToolsRegistry.FUSING_MACHINE_CONTAINER.get(),
                FusingMachineCategory.TYPE,
                36,
                2,
                0,
                36
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SuperCoolerScreen.class, 80, 28, 22, 16, SuperCoolerCategory.TYPE);
        registration.addRecipeClickArea(FusingMachineScreen.class, 91, 28, 22, 16, FusingMachineCategory.TYPE);
    }
}
