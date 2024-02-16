package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.TinyMap;

public interface FusingMachineRecipeSchema {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputFluid> RESULT = FluidComponents.OUTPUT.key("result");

    RecipeKey<TinyMap<String, Integer>> ENERGY = new MapRecipeComponent<>(
            StringComponent.ANY,
            NumberComponent.INT,
            false
    )
            .key("energy");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, INGREDIENTS, ENERGY);
}
