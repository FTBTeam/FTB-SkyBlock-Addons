package dev.ftb.ftbsba.tools.recipies;

import com.google.common.base.MoreObjects;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FusingMachineRecipe implements Recipe<NoInventory> {
    private final ResourceLocation id;
    public String group;

    public List<Ingredient> ingredients;
    public FluidStack fluidResult;
    public SuperCoolerRecipe.EnergyComponent energyComponent;

    public FusingMachineRecipe(ResourceLocation i, String g) {
        this.id = i;
        this.group = g;

        this.ingredients = new ArrayList<>();
        this.fluidResult = FluidStack.EMPTY;
        this.energyComponent = new SuperCoolerRecipe.EnergyComponent(0, 0);
    }

    @Override
    public boolean matches(NoInventory inv, Level world) {
        return true;
    }

    @Override
    public ItemStack assemble(NoInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolsRegistry.FUSING_MACHINE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ToolsRegistry.FUSING_MACHINE_RECIPE_TYPE.get();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FusingMachineRecipe.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("group='" + group + "'")
                .add("ingredients=" + ingredients)
                .add("fluidResult=" + fluidResult)
                .add("energyComponent=" + energyComponent)
                .toString();
    }
}
