package dev.ftb.ftbsba.tools.recipies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SuperCoolerRecipeSerializer implements RecipeSerializer<SuperCoolerRecipe> {

    @Override
    public SuperCoolerRecipe fromJson(ResourceLocation arg, JsonObject jsonObject) {
        var recipe = new SuperCoolerRecipe(arg, jsonObject.has("group") ? jsonObject.get("group").getAsString() : "");

        var ingredients = jsonObject.get("ingredients").getAsJsonArray();
        for (JsonElement e : ingredients) {
            recipe.ingredients.add(Ingredient.fromJson(e));
        }

        recipe.energyComponent = SuperCoolerRecipe.EnergyComponent.fromJson(jsonObject.get("energy"));
        recipe.result = ShapedRecipe.itemStackFromJson(jsonObject.get("result").getAsJsonObject());
        recipe.fluidIngredient = FusingMachineRecipeSerializer.FluidStackSerializer.deserialize(jsonObject.get("fluid").getAsJsonObject());

        return recipe;
    }

    @Override
    public @Nullable SuperCoolerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
        var groups = buf.readUtf();
        var recipe = new SuperCoolerRecipe(recipeId, groups);

        List<Ingredient> ingredients = buf.readList(Ingredient::fromNetwork);
        SuperCoolerRecipe.EnergyComponent energyComponent = SuperCoolerRecipe.EnergyComponent.fromNetwork(buf);

        recipe.ingredients.addAll(ingredients);
        recipe.energyComponent = energyComponent;
        recipe.result = buf.readItem();
        recipe.fluidIngredient = buf.readFluidStack();

        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, SuperCoolerRecipe recipe) {
        buf.writeUtf(recipe.group);
        buf.writeCollection(recipe.ingredients, (buf1, ingredient) -> ingredient.toNetwork(buf1));
        recipe.energyComponent.toNetwork(buf);
        buf.writeItem(recipe.result);
        buf.writeFluidStack(recipe.fluidIngredient);
    }
}
