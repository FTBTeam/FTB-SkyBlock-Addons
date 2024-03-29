package dev.ftb.ftbsba.tools.recipies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import net.minecraft.network.FriendlyByteBuf;
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

public class SuperCoolerRecipe implements Recipe<NoInventory> {
    private final ResourceLocation id;
    public final String group;
    public final List<Ingredient> ingredients;
    public EnergyComponent energyComponent;
    public FluidStack fluidIngredient;

    public ItemStack result;

    public SuperCoolerRecipe(ResourceLocation i, String g) {
        this.id = i;
        this.group = g;

        this.ingredients = new ArrayList<>();
        this.energyComponent = new EnergyComponent(0, 0);
        this.fluidIngredient = FluidStack.EMPTY;

        this.result = ItemStack.EMPTY;
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
        return ToolsRegistry.SUPER_COOLER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ToolsRegistry.SUPER_COOLER_RECIPE_TYPE.get();
    }

    public record EnergyComponent(int fePerTick, int ticksToProcess) {
        public static EnergyComponent fromJson(JsonElement element) {
            return new EnergyComponent(element.getAsJsonObject().get("fePerTick").getAsInt(), element.getAsJsonObject().get("ticksToProcess").getAsInt());
        }

        public JsonElement toJson() {
            var element = new JsonObject();
            element.addProperty("fePerTick", fePerTick);
            element.addProperty("ticksToProcess", ticksToProcess);
            return element;
        }

        public static EnergyComponent fromNetwork(FriendlyByteBuf buffer) {
            return new EnergyComponent(buffer.readInt(), buffer.readInt());
        }

        public void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeInt(fePerTick);
            buffer.writeInt(ticksToProcess);
        }
    }
}
