package dev.ftb.ftbsba.tools.recipies;

import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;


public class NoInventory extends RecipeWrapper {
	public static final NoInventory INSTANCE = new NoInventory();

	private NoInventory() {
		super(new ItemStackHandler(0));
	}
}
