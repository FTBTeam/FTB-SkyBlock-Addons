package dev.ftb.ftbsba.tools.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.world.item.ItemStack;


public class HammerRecipeJS extends SBARecipeJS {


    @Override
	public void create(RecipeArguments args) {
		this.inputItems.add(this.parseItemInput(args.get(0)));

		for (Object o : ListJS.orSelf(args.get(1))) {
			ItemStack i = this.parseItemOutput(o);
			this.outputItems.add(i);
		}
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
