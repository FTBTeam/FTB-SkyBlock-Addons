package dev.ftb.ftbsba.tools.loot;

import com.google.gson.JsonObject;
import dev.ftb.ftbsba.tools.ToolsTags;
import dev.ftb.ftbsba.tools.recipies.CrookDropsResult;
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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CrookModifier extends LootModifier {
    public CrookModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> list, LootContext context) {
        ItemStack crook = context.getParamOrNull(LootContextParams.TOOL);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        if (!(entity instanceof Player) || crook == null || blockState == null || !crook.is(ToolsTags.Items.CROOKS) || !ToolsRecipeCache.crookable(blockState)) {
            return list;
        }

        CrookDropsResult crookDrops = ToolsRecipeCache.getCrookDrops(entity.level, new ItemStack(blockState.getBlock()));
        if (crookDrops.items().size() > 0) {
            Random random = context.getRandom();

            List<ItemStack> collect = crookDrops
                    .items()
                    .stream()
                    .filter(itemWithChance -> random.nextFloat() < itemWithChance.chance())
                    .map(itemWithChance -> itemWithChance.item().copy())
                    .collect(Collectors.toList());

            Collections.shuffle(collect);
            return collect.stream().limit(crookDrops.max()).toList();
        }

        return list;
    }

    public static class Serializer extends GlobalLootModifierSerializer<CrookModifier> {
        @Override
        public CrookModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            return new CrookModifier(conditions);
        }

        @Override
        public JsonObject write(CrookModifier instance) {
            return this.makeConditions(instance.conditions);
        }
    }
}
