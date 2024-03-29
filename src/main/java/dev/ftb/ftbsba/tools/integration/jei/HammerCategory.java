package dev.ftb.ftbsba.tools.integration.jei;

import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.recipies.HammerRecipe;
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

public class HammerCategory implements IRecipeCategory<HammerRecipe> {
    public static final RecipeType<HammerRecipe> TYPE = RecipeType.create(FTBSBA.MOD_ID, "hammers_jei", HammerRecipe.class);

    public static final ResourceLocation BACKGROUND = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/hammer_jei_background.png");

    private final IDrawableStatic background;

    public HammerCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 156, 62).setTextureSize(180, 62).build();
    }


    @Override
    public RecipeType<HammerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ftbsba.jei.recipe.hammer");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HammerRecipe hammerRecipe, IFocusGroup iFocusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addIngredients(hammerRecipe.ingredient);

        for (int i = 0; i < hammerRecipe.results.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 28 + (i % 7 * 18), 5 + i / 7 * 18).addItemStack(hammerRecipe.results.get(i));
        }
    }

}
