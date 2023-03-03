package dev.ftb.ftbsba.tools.recipies;

import com.google.common.base.MoreObjects;
import net.minecraft.world.item.ItemStack;

public record ItemWithChance(ItemStack item, double chance) {

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("item", item)
			.add("weight", chance)
			.toString();
	}

	public ItemWithChance copy(){
		return new ItemWithChance(item.copy(), chance);
	}
}
