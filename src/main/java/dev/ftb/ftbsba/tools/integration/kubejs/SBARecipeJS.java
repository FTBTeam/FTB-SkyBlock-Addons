package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.IngredientMatch;
import dev.latvian.mods.kubejs.recipe.ItemInputTransformer;
import dev.latvian.mods.kubejs.recipe.ItemOutputTransformer;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public abstract class SBARecipeJS extends RecipeJS
{

    public final List<Ingredient> inputItems = new ArrayList<>(1);
    public final List<ItemStack> outputItems = new ArrayList<>();
    @Override
    public boolean hasInput(IngredientMatch match) {
        for (Ingredient in : inputItems) {
            if (match.contains(in)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean replaceInput(IngredientMatch match, Ingredient with, ItemInputTransformer transformer) {
        boolean changed = false;

        for (int i = 0; i < inputItems.size(); ++i) {
            Ingredient in = inputItems.get(i);

            if (match.contains(in)) {
                inputItems.set(i, transformer.transform(this, match, in, with));
                changed = true;
            }
        }

        return changed;
    }
    @Override
    public boolean hasOutput(IngredientMatch match) {
        for (ItemStack out : outputItems) {
            if (match.contains(out)) {
                return true;
            }
        }

        return false;
    }
    @Override
    public boolean replaceOutput(IngredientMatch match, ItemStack with, ItemOutputTransformer transformer) {
        boolean changed = false;

        for (int i = 0; i < outputItems.size(); ++i) {
            var out = outputItems.get(i);

            if (match.contains(out)) {
                outputItems.set(i, transformer.transform(this, match, out, with));
                changed = true;
            }
        }

        return changed;
    }
}
