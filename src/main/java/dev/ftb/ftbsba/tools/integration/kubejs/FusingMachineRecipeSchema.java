package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface FusingMachineRecipeSchema {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<SuperCoolerRecipe.EnergyComponent> ENERGY = new EnergyRecipeComponent().key("energy");
    RecipeKey<OutputFluid> RESULT = FluidComponents.OUTPUT.key("result");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, INGREDIENTS, ENERGY);
}
