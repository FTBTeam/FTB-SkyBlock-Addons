package dev.ftb.ftbsba.tools.content.core;

import dev.ftb.ftbsba.tools.recipies.FusingMachineRecipe;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class RecipeCaches {
    public static final RecipeCache<FusingMachineRecipe> FUSING_MACHINE =
            new RecipeCache<>((itemHandler, fluidHandler) -> hashItemHandler(itemHandler));
    public static final RecipeCache<SuperCoolerRecipe> SUPER_COOLER =
            new RecipeCache<>(RecipeCaches::makeSuperCoolerHash);

    private static int makeSuperCoolerHash(IItemHandler itemHandler, IFluidHandler fluidHandler) {
        return Objects.hash(hashItemHandler(itemHandler), hashFluidHandler(fluidHandler));
    }

    private static int hashItemHandler(IItemHandler handler) {
        IntList ids = new IntArrayList();
        for (int i = 0; i < handler.getSlots(); i++) {
            ids.add(Registry.ITEM.getId(handler.getStackInSlot(i).getItem()));
        }
        return Arrays.hashCode(ids.intStream().sorted().toArray());
    }

    private static int hashFluidHandler(IFluidHandler handler) {
        IntList ids = new IntArrayList();
        for (int i = 0; i < handler.getTanks(); i++) {
            ids.add(Registry.FLUID.getId(handler.getFluidInTank(i).getFluid()));
        }
        return Arrays.hashCode(ids.intStream().sorted().toArray());
    }

    public static void clearAll() {
        FUSING_MACHINE.clear();
        SUPER_COOLER.clear();
    }

    public static class ReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.runAsync(RecipeCaches::clearAll, gameExecutor)
                    .thenCompose(stage::wait);
        }
    }

    public static class RecipeCache<R extends Recipe<?>> {
        private static final int MAX_CACHE_SIZE = 1024;

        private final KeyGen keyGen;
        private final Int2ObjectLinkedOpenHashMap<Optional<R>> recipeCache = new Int2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE, 0.25f);

        public RecipeCache(KeyGen keyGen) {
            this.keyGen = keyGen;
        }

        public Optional<R> getCachedRecipe(Supplier<Optional<R>> recipeFinder, IItemHandler itemHandler, IFluidHandler fluidHandler) {
            int key = keyGen.genHashKey(itemHandler, fluidHandler);

            if (recipeCache.containsKey(key)) {
                return recipeCache.getAndMoveToFirst(key);
            } else {
                Optional<R> newRecipe = recipeFinder.get();
                while (recipeCache.size() >= MAX_CACHE_SIZE) {
                    recipeCache.removeLast();
                }
                recipeCache.put(key, newRecipe);
                return newRecipe;
            }
        }

        private void clear() {
            recipeCache.clear();
        }

        @FunctionalInterface
        public interface KeyGen {
            int genHashKey(IItemHandler itemHandler, IFluidHandler fluidHandler);
        }
    }
}
