package dev.ftb.ftbsba.tools.recipies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class SuperCoolerRecipeSerializer implements RecipeSerializer<SuperCoolerRecipe> {

    @Override
    public SuperCoolerRecipe fromJson(ResourceLocation arg, JsonObject jsonObject) {
        var recipe = new SuperCoolerRecipe(arg, jsonObject.has("group") ? jsonObject.get("group").getAsString() : "");

        var ingredients = jsonObject.get("ingredients").getAsJsonArray();
        for (JsonElement e : ingredients) {
            recipe.ingredients.add(Ingredient.fromJson(e));
        }

        var fluidIngredient = jsonObject.get("fluid").getAsJsonObject();
        recipe.fluidIngredient = FusingMachineRecipeSerializer.FluidStackSerializer.deserialize(fluidIngredient);
        recipe.energyComponent = SuperCoolerRecipe.EnergyComponent.fromJson(jsonObject.get("energy"));

        recipe.result = ShapedRecipe.itemStackFromJson(jsonObject.get("result").getAsJsonObject());

        return recipe;
    }

    @Override
    public @Nullable SuperCoolerRecipe fromNetwork(ResourceLocation arg, FriendlyByteBuf arg2) {
        var groups = arg2.readUtf();

        Ingredient[] ingredients = new Ingredient[3];
        for (int i = 0; i < 3; i++) {
            ingredients[i] = Ingredient.fromNetwork(arg2);
        }

        FluidStack fluidIngredient = FluidStack.readFromPacket(arg2);
        SuperCoolerRecipe.EnergyComponent energyComponent = SuperCoolerRecipe.EnergyComponent.fromNetwork(arg2);

        var recipe = new SuperCoolerRecipe(arg, groups);
        recipe.ingredients.addAll(Arrays.asList(ingredients));
        recipe.fluidIngredient = fluidIngredient;
        recipe.energyComponent = energyComponent;
        recipe.result = arg2.readItem();

        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf arg, SuperCoolerRecipe arg2) {
        arg.writeUtf(arg2.group);

        for (Ingredient i : arg2.ingredients) {
            i.toNetwork(arg);
        }

        arg2.fluidIngredient.writeToPacket(arg);
        arg2.energyComponent.toNetwork(arg);
        arg.writeItem(arg2.result);
    }
}
