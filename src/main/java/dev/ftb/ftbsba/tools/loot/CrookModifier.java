package dev.ftb.ftbsba.tools.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.ftbsba.tools.ToolsTags;
import dev.ftb.ftbsba.tools.recipies.CrookDropsResult;
import dev.ftb.ftbsba.tools.recipies.ToolsRecipeCache;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrookModifier extends LootModifier {

    public static final Codec<CrookModifier> CODEC = RecordCodecBuilder.create((builder) -> codecStart(builder).apply(builder, CrookModifier::new));

    public CrookModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> list, LootContext context) {
        ItemStack crook = context.getParamOrNull(LootContextParams.TOOL);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        if (!(entity instanceof Player) || crook == null || blockState == null || !crook.is(ToolsTags.Items.CROOKS) || !ToolsRecipeCache.crookable(blockState)) {
            return list;
        }

        CrookDropsResult crookDrops = ToolsRecipeCache.getCrookDrops(entity.level, new ItemStack(blockState.getBlock()));
        if (crookDrops.items().size() > 0) {
            RandomSource random = context.getRandom();

            List<ItemStack> collect = crookDrops
                    .items()
                    .stream()
                    .filter(itemWithChance -> random.nextFloat() < itemWithChance.chance())
                    .map(itemWithChance -> itemWithChance.item().copy())
                    .collect(Collectors.toList());

            Collections.shuffle(collect);
            list.clear();
            collect.stream().limit(crookDrops.max()).forEach(list::add);
        }

        return list;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
