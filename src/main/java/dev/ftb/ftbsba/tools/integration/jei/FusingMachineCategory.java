package dev.ftb.ftbsba.tools.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.recipies.FusingMachineRecipe;
import dev.ftb.ftbsba.tools.recipies.HammerRecipe;
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

public class FusingMachineCategory implements IRecipeCategory<FusingMachineRecipe> {
    public static final RecipeType<FusingMachineRecipe> TYPE = RecipeType.create(FTBSBA.MOD_ID, "fusing_machine_jei", FusingMachineRecipe.class);

    public static final ResourceLocation BACKGROUND = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/jei_fusing_machine.png");

    private final IDrawableStatic background;
    private final IDrawableAnimated powerBar;
    private final IDrawableAnimated progress;

    public FusingMachineCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 106, 28)
                .setTextureSize(134, 28)
                .build();

        this.powerBar = guiHelper.drawableBuilder(BACKGROUND, 106, 0, 6, 16)
                .setTextureSize(134, 28)
                .buildAnimated(guiHelper.createTickTimer(120, 16, false), IDrawableAnimated.StartDirection.BOTTOM);

        this.progress = guiHelper.drawableBuilder(BACKGROUND, 112, 0, 22, 16)
                .setTextureSize(134, 28)
                .buildAnimated(guiHelper.createTickTimer(120, 22, true), IDrawableAnimated.StartDirection.LEFT);
    }

    @Override
    public RecipeType<FusingMachineRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ftbsba.jei.recipe.fusing");
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
    public void draw(FusingMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        this.powerBar.draw(stack, 6, 6);
        this.progress.draw(stack, 57, 6);

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
        stack.translate(83, 25, 0);
        stack.scale(0.5F, 0.5F, 0.5F);
        Gui.drawString(stack, Minecraft.getInstance().font, "%s ticks".formatted(ticks), 0, 0, 0xBEFFFFFF);

        stack.popPose();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FusingMachineRecipe fusingRecipe, IFocusGroup iFocusGroup) {
        for (int i = 0; i < fusingRecipe.ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 18 + i * 18, 6).addIngredients(fusingRecipe.ingredients.get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 6).addFluidStack(fusingRecipe.fluidResult.getFluid(), fusingRecipe.fluidResult.getAmount())
                .addTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.literal(fusingRecipe.fluidResult.getAmount() + " mB"));
                });
    }

}
