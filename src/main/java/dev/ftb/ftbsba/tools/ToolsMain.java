package dev.ftb.ftbsba.tools;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ToolsMain {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final CauldronInteraction EMPTY_FROM_LEAVES = (state, level, pos, player, hand, stack) -> {
        if (!level.isClientSide) {
            Item item = stack.getItem();

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState());
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    };

    public static final CauldronInteraction WATER_FROM_LEAVES = (state, level, pos, player, hand, stack) -> {
        if (!level.isClientSide && state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
            Item item = stack.getItem();

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    };

    public static void setup() {
        LOGGER.info("Running Tools init");

        // Cauldron Interactions
        for (Block block : ForgeRegistries.BLOCKS) {
            if (block instanceof LeavesBlock || block instanceof SaplingBlock) {
                LOGGER.info("Registering new cauldron interaction for {} [{}]", block.getName().getString(), ForgeRegistries.BLOCKS.getKey(block));
                Item item = block.asItem();

                if (item != Items.AIR) {
                    CauldronInteraction.EMPTY.put(item, EMPTY_FROM_LEAVES);
                    CauldronInteraction.WATER.put(item, WATER_FROM_LEAVES);
                }
            }
        }
    }
}
