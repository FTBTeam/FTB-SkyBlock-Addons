package dev.ftb.ftbsba.tools.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.recipies.CrookRecipe;
import dev.ftb.ftbsba.tools.recipies.ItemWithChance;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
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
    public static final ResourceLocation ID = new ResourceLocation(FTBSBA.MOD_ID, "crook_jei");
    public static final ResourceLocation BACKGROUND = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/crook_jei_background.png");

    private final static Comparator<ItemWithChance> COMPARATOR = (a, b) -> (int) ((b.chance() * 100) - (a.chance() * 100));

    private final IDrawableStatic background;

    public CrookCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 156, 78).setTextureSize(180, 78).build();
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public Class<? extends CrookRecipe> getRecipeClass() {
        return CrookRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Crooking");
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
    public void setIngredients(CrookRecipe crookRecipe, IIngredients iIngredients) {
        iIngredients.setInputs(VanillaTypes.ITEM, Arrays.asList(crookRecipe.ingredient.getItems()));

        List<ItemWithChance> results = new ArrayList<>(crookRecipe.results);
        results.sort(COMPARATOR);

        iIngredients.setOutputs(VanillaTypes.ITEM, crookRecipe.results.stream().map(e -> e.item()).collect(Collectors.toList()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CrookRecipe crookRecipe, IIngredients iIngredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 4, 4);
        itemStacks.init(1, true, 4, 23);

        ArrayList<ItemWithChance> itemWithChance = new ArrayList<>(crookRecipe.results);
        itemWithChance.sort(COMPARATOR);

        for (int i = 0; i < itemWithChance.size(); i++) {
            itemStacks.init(2 + i, false, 27 + (i % 7 * 18), 4 + i / 7 * 24);
        }

        itemStacks.set(iIngredients);
    }

    @Override
    public void draw(CrookRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, matrixStack, mouseX, mouseY);

        ArrayList<ItemWithChance> itemWithWeights = new ArrayList<>(recipe.results);
        itemWithWeights.sort(COMPARATOR);

        int row = 0;
        for (int i = 0; i < itemWithWeights.size(); i++) {
            if (i > 0 && i % 7 == 0) {
                row++;
            }
            matrixStack.pushPose();
            matrixStack.translate(36 + (i % 7 * 18), 23.5f + (row * 24), 100);
            matrixStack.scale(.5F, .5F, 8000F);
            Gui.drawCenteredString(matrixStack, Minecraft.getInstance().font, Math.round(itemWithWeights.get(i).chance() * 100) + "%", 0, 0, 0xFFFFFF);
            matrixStack.popPose();
        }
    }
}
