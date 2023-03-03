package dev.ftb.ftbsba.tools.recipies;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

// TODO: Clean this up and make it less crap
@Mod.EventBusSubscriber
public class ToolsRecipeCache {
    public static final Set<Ingredient> crookableCache = new HashSet<>();
    public static final Set<Ingredient> hammerableCache = new HashSet<>();

    private static final Map<Item, CrookDropsResult> crookCache = new HashMap<>();
    private static final Map<Item, List<ItemStack>> hammerCache = new HashMap<>();

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) arg -> ToolsRecipeCache.refreshCaches(event.getServerResources().getRecipeManager()));
    }

    @SubscribeEvent
    public static void recipesSetup(RecipesUpdatedEvent event) {
        RecipeManager recipeManager = event.getRecipeManager();
        ToolsRecipeCache.refreshCaches(recipeManager);
    }

    public static void refreshCaches(RecipeManager manager) {
        clearCache();
        crookableCache.addAll(manager.getAllRecipesFor(ToolsRegistry.CROOK_RECIPE_TYPE.get()).stream().map(e -> e.ingredient).toList());
        hammerableCache.addAll(manager.getAllRecipesFor(ToolsRegistry.HAMMER_RECIPE_TYPE.get()).stream().map(e -> e.ingredient).toList());
    }

    public static void clearCache() {
        crookCache.clear();
        crookableCache.clear();
        hammerCache.clear();
        hammerableCache.clear();
    }

    public static List<ItemStack> getHammerDrops(Level level, ItemStack input) {
        return hammerCache.computeIfAbsent(input.getItem(), key -> {
            List<ItemStack> drops = new ArrayList<>();
            for (HammerRecipe recipe : level.getRecipeManager().getRecipesFor(ToolsRegistry.HAMMER_RECIPE_TYPE.get(), NoInventory.INSTANCE, level)) {
                if (recipe.ingredient.test(input)) {
                    recipe.results.forEach(e -> drops.add(e.copy()));
                }
            }

            return drops;
        });
    }

    public static boolean hammerable(BlockState state) {
        return hammerable(new ItemStack(state.getBlock()));
    }

    public static boolean hammerable(ItemStack stack) {
        for (Ingredient e : hammerableCache) {
            if (e.test(stack)) {
                return true;
            }
        }

        return false;
    }

    public static CrookDropsResult getCrookDrops(Level level, ItemStack input) {
        return crookCache.computeIfAbsent(input.getItem(), key -> {
            List<ItemWithChance> drops = new ArrayList<>();
            int max = -1;
            for (CrookRecipe recipe : level.getRecipeManager().getRecipesFor(ToolsRegistry.CROOK_RECIPE_TYPE.get(), NoInventory.INSTANCE, level)) {
                if (recipe.ingredient.test(input)) {
                    if (recipe.max > 0) {
                        max = recipe.max;
                    }
                    recipe.results.forEach(e -> drops.add(e.copy()));
                }
            }

            return new CrookDropsResult(drops, max);
        });
    }

    public static boolean crookable(BlockState state) {
        return crookable(new ItemStack(state.getBlock()));
    }

    public static boolean crookable(ItemStack stack) {
        for (Ingredient e : crookableCache) {
            if (e.test(stack)) {
                return true;
            }
        }

        return false;
    }
}
