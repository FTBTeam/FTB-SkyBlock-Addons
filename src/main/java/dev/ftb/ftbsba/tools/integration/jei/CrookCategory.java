package dev.ftb.ftbsba.tools.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.recipies.CrookRecipe;
import dev.ftb.ftbsba.tools.recipies.ItemWithChance;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class CrookCategory implements IRecipeCategory<CrookRecipe> {
    public static final RecipeType<CrookRecipe> TYPE = RecipeType.create(FTBSBA.MOD_ID, "crook_jei", CrookRecipe.class);
    public static final ResourceLocation BACKGROUND = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/crook_jei_background.png");

    private final static Comparator<ItemWithChance> COMPARATOR = (a, b) -> (int) ((b.chance() * 100) - (a.chance() * 100));

    private final IDrawableStatic background;

    public CrookCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 156, 78).setTextureSize(180, 78).build();
    }

    @Override
    public RecipeType<CrookRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Crooking");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrookRecipe crookRecipe, IFocusGroup iFocusGroup) {

        ArrayList<ItemWithChance> itemWithChance = new ArrayList<>(crookRecipe.results);
        itemWithChance.sort(COMPARATOR);

        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addIngredients(crookRecipe.ingredient);

        for (int i = 0; i < itemWithChance.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 28 + (i % 7 * 18), 5 + i / 7 * 24).addItemStack(itemWithChance.get(i).item());
        }
    }

    @Override
    public void draw(CrookRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);

        ArrayList<ItemWithChance> itemWithWeights = new ArrayList<>(recipe.results);
        itemWithWeights.sort(COMPARATOR);

        int row = 0;
        for (int i = 0; i < itemWithWeights.size(); i++) {
            if (i > 0 && i % 7 == 0) {
                row++;
            }
            stack.pushPose();
            stack.translate(36 + (i % 7 * 18), 23.5f + (row * 24), 100);
            stack.scale(.5F, .5F, 8000F);
            Gui.drawCenteredString(stack, Minecraft.getInstance().font, Math.round(itemWithWeights.get(i).chance() * 100) + "%", 0, 0, 0xFFFFFF);
            stack.popPose();
        }
    }

}
