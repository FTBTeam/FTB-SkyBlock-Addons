package dev.ftb.ftbsba.tools.content;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class CrookItem extends DiggerItem {
    public CrookItem(float attackBase, float attackSpeed, Tier tier, Properties properties) {
        super(attackBase, attackSpeed, tier, BlockTags.MINEABLE_WITH_SHOVEL, properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction)
                || ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction);
    }
}
