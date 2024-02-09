package dev.ftb.ftbsba.tools.content.fusion;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FusingMachineBlockEntity extends BlockEntity implements MenuProvider {
    public FusingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ToolsRegistry.FUSING_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof FusingMachineBlockEntity entity) {
//            System.out.println("FusingMachineBlockEntity.ticker");
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ftbsba.fusing_machine"); // TODO: Add translation
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory arg, Player arg2) {
        return new FusingMachineContainer(i, arg, arg2);
    }
}
