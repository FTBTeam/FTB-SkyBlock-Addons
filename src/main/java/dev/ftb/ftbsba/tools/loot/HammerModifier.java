package dev.ftb.ftbsba.tools.loot;

import com.google.gson.JsonObject;
import dev.ftb.ftbsba.tools.ToolsTags;
import dev.ftb.ftbsba.tools.recipies.ToolsRecipeCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HammerModifier extends LootModifier {
    public HammerModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> list, LootContext context) {
        ItemStack hammer = context.getParamOrNull(LootContextParams.TOOL);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        if (!(entity instanceof Player) || hammer == null || blockState == null || !hammer.is(ToolsTags.Items.HAMMERS) || !ToolsRecipeCache.hammerable(blockState)) {
            return list;
        }

        List<ItemStack> hammerDrops = ToolsRecipeCache.getHammerDrops(entity.level, new ItemStack(blockState.getBlock()));
        if (hammerDrops.size() > 0) {
            return hammerDrops.stream().map(ItemStack::copy).collect(Collectors.toList());
        }

        return list;
    }

    public static class Serializer extends GlobalLootModifierSerializer<HammerModifier> {
        @Override
        public HammerModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            return new HammerModifier(conditions);
        }

        @Override
        public JsonObject write(HammerModifier instance) {
            return this.makeConditions(instance.conditions);
        }
    }
}
