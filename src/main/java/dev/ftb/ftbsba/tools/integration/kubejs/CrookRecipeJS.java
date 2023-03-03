package dev.ftb.ftbsba.tools.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeArguments;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.world.item.ItemStack;

public class CrookRecipeJS extends SBARecipeJS {

	@Override
	public void create(RecipeArguments args) {
		this.inputItems.add(this.parseItemInput(args.get(0)));
		this.json.addProperty("max", 3);

		for (Object o : ListJS.orSelf(args.get(1))) {
			ItemStack i = this.parseItemOutput(o);
			this.outputItems.add(i);
		}
	}

	public CrookRecipeJS max(int max) {
		this.json.addProperty("max", max);
		this.save();
		return this;
	}

	@Override
	public void deserialize() {
		this.inputItems.add(this.parseItemInput(this.json.get("ingredient")));

		for (JsonElement e : this.json.get("results").getAsJsonArray()) {
			JsonObject o = e.getAsJsonObject();
			this.outputItems.add(this.parseItemOutput(o));
		}
	}

	@Override
	public void serialize() {
		if (this.serializeOutputs) {
			JsonArray array = new JsonArray();

			for (ItemStack o : this.outputItems) {
				array.add(itemToJson(o));
			}

			this.json.add("results", array);
		}

		if (this.serializeInputs) {
			this.json.add("ingredient", this.inputItems.get(0).toJson());
		}
	}
}
