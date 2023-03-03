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

public class HammerRecipeSerializer implements RecipeSerializer<HammerRecipe> {
    @Override
    public HammerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        HammerRecipe r = new HammerRecipe(recipeId, json.has("group") ? json.get("group").getAsString() : "");

        if (GsonHelper.isArrayNode(json, "ingredient")) {
            r.ingredient = Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "ingredient"));
        } else {
            r.ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
        }

        for (JsonElement e : json.get("results").getAsJsonArray()) {
            JsonObject o = e.getAsJsonObject();
            // allow for stack sizes greater than 64 by splitting into multiple stacks
            int i = GsonHelper.getAsInt(o, "count", 1);
            while (i > 0) {
                int j = Math.min(i, 64);
                o.addProperty("count", j);
                r.results.add(ShapedRecipe.itemStackFromJson(o));
                i -= j;
            }
        }

        return r;
    }

    @Override
    public HammerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        HammerRecipe r = new HammerRecipe(recipeId, buffer.readUtf(Short.MAX_VALUE));
        r.ingredient = Ingredient.fromNetwork(buffer);
        int w = buffer.readVarInt();

        for (int i = 0; i < w; i++) {
            r.results.add(buffer.readItem());
        }
        return r;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, HammerRecipe r) {
        buffer.writeUtf(r.getGroup(), Short.MAX_VALUE);
        r.ingredient.toNetwork(buffer);

        buffer.writeVarInt(r.results.size());

        for (ItemStack i : r.results) {
            buffer.writeItem(i);
        }
    }
}
