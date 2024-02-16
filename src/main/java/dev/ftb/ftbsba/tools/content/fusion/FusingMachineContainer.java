package dev.ftb.ftbsba.tools.content.fusion;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.AbstractMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.SlotItemHandler;

public class FusingMachineContainer extends AbstractMachineMenu<FusingMachineBlockEntity> {
    public FusingMachineContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public FusingMachineContainer(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ToolsRegistry.FUSING_MACHINE_CONTAINER.get(), windowId, playerInventory, pos);

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 43, 27));
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 43 + 18, 27));

        addPlayerSlots(playerInventory, 8, 84);

        addDataSlots(containerData);
    }
}
