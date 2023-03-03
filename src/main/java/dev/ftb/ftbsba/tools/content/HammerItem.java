package dev.ftb.ftbsba.tools.content;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class HammerItem extends DiggerItem {

    public HammerItem(Tiers tier, float attackBase, float attackSpeed, Properties arg2) {
        super(attackBase, attackSpeed, tier, BlockTags.MINEABLE_WITH_PICKAXE, arg2);
    }

    @Override
    public float getDestroySpeed(ItemStack arg, BlockState arg2) {
        return arg2.is(BlockTags.MINEABLE_WITH_PICKAXE) || arg2.is(BlockTags.MINEABLE_WITH_SHOVEL) ? this.speed : 1.0f;
    }

    @Override
    public void appendHoverText(ItemStack arg, @Nullable Level arg2, List<Component> list, TooltipFlag arg3) {
        list.add(new TranslatableComponent("ftbsbc.tooltip.hammers").gray());
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            int i = this.getTier().getLevel();
            if (i < 3 && state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
                return false;
            }

            if (i < 2 && state.is(BlockTags.NEEDS_IRON_TOOL)) {
                return false;
            }

            return i >= 1 || !state.is(BlockTags.NEEDS_STONE_TOOL);
        }

        return false;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction);
    }
}
