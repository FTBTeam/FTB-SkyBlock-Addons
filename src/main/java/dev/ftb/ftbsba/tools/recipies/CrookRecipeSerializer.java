package dev.ftb.ftbsba.tools.recipies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class CrookRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CrookRecipe> {
    @Override
    public CrookRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        CrookRecipe r = new CrookRecipe(recipeId, json.has("group") ? json.get("group").getAsString() : "");
        r.max = GsonHelper.getAsInt(json, "max", 0);

        if (GsonHelper.isArrayNode(json, "ingredient")) {
            r.ingredient = Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "ingredient"));
        } else {
            r.ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
        }

        for (JsonElement e : json.get("results").getAsJsonArray()) {
            JsonObject o = e.getAsJsonObject();
            r.results.add(new ItemWithChance(ShapedRecipe.itemStackFromJson(o), o.has("chance") ? o.get("chance").getAsDouble() : 1.0));
        }

        return r;
    }

    @Override
    public CrookRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        CrookRecipe r = new CrookRecipe(recipeId, buffer.readUtf(Short.MAX_VALUE));
        r.ingredient = Ingredient.fromNetwork(buffer);
        r.max = buffer.readVarInt();

        int w = buffer.readVarInt();

        for (int i = 0; i < w; i++) {
            r.results.add(new ItemWithChance(buffer.readItem(), buffer.readDouble()));
        }
        return r;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CrookRecipe r) {
        buffer.writeUtf(r.getGroup(), Short.MAX_VALUE);
        r.ingredient.toNetwork(buffer);
        buffer.writeVarInt(r.max);
        buffer.writeVarInt(r.results.size());

        for (ItemWithChance i : r.results) {
            buffer.writeItem(i.item());
            buffer.writeDouble(i.chance());
        }
    }
}
