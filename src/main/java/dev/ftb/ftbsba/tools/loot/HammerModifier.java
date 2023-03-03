package dev.ftb.ftbsba.tools.loot;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.ftbsba.tools.ToolsTags;
import dev.ftb.ftbsba.tools.recipies.ToolsRecipeCache;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HammerModifier extends LootModifier {

    public static final Codec<HammerModifier> CODEC = RecordCodecBuilder.create((builder) -> codecStart(builder).apply(builder, HammerModifier::new));

    public HammerModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> list, LootContext context) {
        ItemStack hammer = context.getParamOrNull(LootContextParams.TOOL);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        if (!(entity instanceof Player) || hammer == null || blockState == null || !hammer.is(ToolsTags.Items.HAMMERS) || !ToolsRecipeCache.hammerable(blockState)) {
            return list;
        }

        List<ItemStack> hammerDrops = ToolsRecipeCache.getHammerDrops(entity.level, new ItemStack(blockState.getBlock()));
        if (hammerDrops.size() > 0) {
            list.clear();
            hammerDrops.stream().map(ItemStack::copy).forEach(list::add);
        }

        return list;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
