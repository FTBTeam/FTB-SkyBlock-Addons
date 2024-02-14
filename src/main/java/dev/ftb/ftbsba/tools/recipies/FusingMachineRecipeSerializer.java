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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class FusingMachineRecipeSerializer implements RecipeSerializer<FusingMachineRecipe> {
    @Override
    public FusingMachineRecipe fromJson(ResourceLocation id, JsonObject json) {
        var recipe = new FusingMachineRecipe(id, json.has("group") ? json.get("group").getAsString() : "");

        var ingredients = json.get("ingredients").getAsJsonArray();
        for (JsonElement e : ingredients) {
            recipe.ingredients.add(Ingredient.fromJson(e));
        }

        var fluidResult = json.get("result").getAsJsonObject();
        recipe.fluidResult = FluidStackSerializer.deserialize(fluidResult);
        recipe.energyComponent = SuperCoolerRecipe.EnergyComponent.fromJson(json.get("energy"));

        return recipe;
    }

    @Override
    public @Nullable FusingMachineRecipe fromNetwork(ResourceLocation arg, FriendlyByteBuf arg2) {
        var groups = arg2.readUtf();

        Ingredient[] ingredients = new Ingredient[3];
        for (int i = 0; i < 3; i++) {
            ingredients[i] = Ingredient.fromNetwork(arg2);
        }

        FluidStack fluidResult = FluidStack.readFromPacket(arg2);
        SuperCoolerRecipe.EnergyComponent energyComponent = SuperCoolerRecipe.EnergyComponent.fromNetwork(arg2);

        var recipe = new FusingMachineRecipe(arg, groups);
        Collections.addAll(recipe.ingredients, ingredients);
        recipe.fluidResult = fluidResult;
        recipe.energyComponent = energyComponent;

        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, FusingMachineRecipe recipe) {
        buffer.writeUtf(recipe.group);

        for (Ingredient i : recipe.ingredients) {
            i.toNetwork(buffer);
        }

        recipe.fluidResult.writeToPacket(buffer);
        recipe.energyComponent.toNetwork(buffer);
    }

    public static class FluidStackSerializer {
        public static JsonElement serialize(FluidStack stack) {
            JsonObject o = new JsonObject();
            var fluidLookup = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
            if (fluidLookup == null) {
                throw new RuntimeException("Fluid " + stack.getFluid() + " is not registered");
            }

            o.addProperty("fluid", fluidLookup.toString());
            o.addProperty("amount", stack.getAmount());
            return o;
        }

        public static FluidStack deserialize(JsonElement element) {
            JsonObject o = element.getAsJsonObject();
            var fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(o.get("fluid").getAsString()));
            if (fluid == null) {
                throw new RuntimeException("Fluid " + o.get("fluid").getAsString() + " is not registered");
            }

            return new FluidStack(fluid, o.get("amount").getAsInt());
        }
    }
}
