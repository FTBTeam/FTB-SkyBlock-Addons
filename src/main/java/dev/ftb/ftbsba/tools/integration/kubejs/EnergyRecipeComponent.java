package dev.ftb.ftbsba.tools.integration.kubejs;

import com.google.gson.JsonElement;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;

public class EnergyRecipeComponent implements RecipeComponent<SuperCoolerRecipe.EnergyComponent> {
    @Override
    public Class<?> componentClass() {
        return SuperCoolerRecipe.EnergyComponent.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, SuperCoolerRecipe.EnergyComponent value) {
        var obj = recipe.json;
        obj.addProperty("fePerTick", value.fePerTick());
        obj.addProperty("ticksToProcess", value.ticksToProcess());
        return obj;
    }

    @Override
    public SuperCoolerRecipe.EnergyComponent read(RecipeJS recipe, Object from) {
        var obj = recipe.json;
        return new SuperCoolerRecipe.EnergyComponent(
                obj.get("fePerTick").getAsInt(),
                obj.get("ticksToProcess").getAsInt()
        );
    }
}
