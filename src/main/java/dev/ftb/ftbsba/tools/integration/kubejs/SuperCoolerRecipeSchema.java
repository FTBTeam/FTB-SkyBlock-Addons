package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface SuperCoolerRecipeSchema {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<InputFluid> FLUID = FluidComponents.INPUT.key("fluid");
    RecipeKey<SuperCoolerRecipe.EnergyComponent> ENERGY = new EnergyRecipeComponent().key("energy");
    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, INGREDIENTS, FLUID, ENERGY);
}
