package dev.ftb.ftbsba.tools.integration.jei;

import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SuperCoolerCategory implements IRecipeCategory<SuperCoolerRecipe> {
    public static final RecipeType<SuperCoolerRecipe> TYPE = RecipeType.create(FTBSBA.MOD_ID, "super_cooler_jei", SuperCoolerRecipe.class);

    public static final ResourceLocation BACKGROUND = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/hammer_jei_background.png");

    private final IDrawableStatic background;

    public SuperCoolerCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 156, 62).setTextureSize(180, 62).build();
    }

    @Override
    public RecipeType<SuperCoolerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ftbsba.jei.recipe.super_cooler");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SuperCoolerRecipe superCoolerRecipe, IFocusGroup iFocusGroup) {
        for (int i = 0; i < superCoolerRecipe.ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5 + i * 18, 5).addIngredients(superCoolerRecipe.ingredients.get(i));
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 5, 23).addFluidStack(superCoolerRecipe.fluidIngredient.getFluid(), superCoolerRecipe.fluidIngredient.getAmount());
    }
}
