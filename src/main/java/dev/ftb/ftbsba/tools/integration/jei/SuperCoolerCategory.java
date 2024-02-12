package dev.ftb.ftbsba.tools.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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

public class SuperCoolerCategory implements IRecipeCategory<SuperCoolerRecipe> {
    public static final RecipeType<SuperCoolerRecipe> TYPE = RecipeType.create(FTBSBA.MOD_ID, "super_cooler_jei", SuperCoolerRecipe.class);

    public static final ResourceLocation BACKGROUND = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/jei_super_cooler.png");

    private final IDrawableStatic background;
    private final IDrawableAnimated powerBar;
    private final IDrawableAnimated progress;

    public SuperCoolerCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 146, 28).setTextureSize(174, 28).build();
        this.powerBar = guiHelper.drawableBuilder(BACKGROUND, 146, 0, 6, 16)
                .setTextureSize(174, 28)
                .buildAnimated(guiHelper.createTickTimer(120, 16, false), IDrawableAnimated.StartDirection.BOTTOM);

        this.progress = guiHelper.drawableBuilder(BACKGROUND, 152, 0, 22, 16)
                .setTextureSize(174, 28)
                .buildAnimated(guiHelper.createTickTimer(120, 22, true), IDrawableAnimated.StartDirection.LEFT);
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
    public void draw(SuperCoolerRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);
        this.powerBar.draw(stack, 6, 6);
        this.progress.draw(stack, 97, 6);

        stack.pushPose();
        stack.translate(5, 25, 0);
        stack.scale(0.5F, 0.5F, 0.5F);

        SuperCoolerRecipe.EnergyComponent energyComponent = recipe.energyComponent;
        int ticks = energyComponent.ticksToProcess();
        int energyPerTick = energyComponent.fePerTick();
        int totalEnergy = ticks * energyPerTick;

        Gui.drawString(stack, Minecraft.getInstance().font, "%sFE/t (%sFE)".formatted(
                energyPerTick, totalEnergy
        ), 0, 0, 0xBEFFFFFF);

        stack.popPose();

        stack.pushPose();
        stack.translate(96, 25, 0);
        stack.scale(0.5F, 0.5F, 0.5F);
        Gui.drawString(stack, Minecraft.getInstance().font, "%s ticks".formatted(ticks), 0, 0, 0xBEFFFFFF);

        stack.popPose();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SuperCoolerRecipe superCoolerRecipe, IFocusGroup iFocusGroup) {
        for (int i = 0; i < superCoolerRecipe.ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 40 + i * 18, 6).addIngredients(superCoolerRecipe.ingredients.get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 6).addItemStack(superCoolerRecipe.result);

        builder.addSlot(RecipeIngredientRole.CATALYST, 18, 6)
                .addFluidStack(superCoolerRecipe.fluidIngredient.getFluid(), superCoolerRecipe.fluidIngredient.getAmount())
                .addTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.literal(superCoolerRecipe.fluidIngredient.getAmount() + " mB"));
                });
    }
}
