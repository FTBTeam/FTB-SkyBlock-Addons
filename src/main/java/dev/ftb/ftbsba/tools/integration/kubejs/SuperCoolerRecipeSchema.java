package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.TinyMap;

public interface SuperCoolerRecipeSchema {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<InputFluid> FLUID = FluidComponents.INPUT.key("fluid");

    RecipeKey<TinyMap<String, Integer>> ENERGY = new MapRecipeComponent<>(
            StringComponent.ANY,
            NumberComponent.INT,
            false
    )
            .key("energy");

    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, INGREDIENTS, FLUID, ENERGY);
}
