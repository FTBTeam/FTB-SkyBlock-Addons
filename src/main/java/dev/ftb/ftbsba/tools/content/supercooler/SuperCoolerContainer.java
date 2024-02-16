package dev.ftb.ftbsba.tools.content.supercooler;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.AbstractMachineMenu;
import dev.ftb.ftbsba.tools.content.core.IOStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SuperCoolerContainer extends AbstractMachineMenu<SuperCoolerBlockEntity> {
    public SuperCoolerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getTilePos(buffer));
    }

    public SuperCoolerContainer(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ToolsRegistry.SUPER_COOLER_CONTAINER.get(), windowId, playerInventory, pos);

        IOStackHandler handler = blockEntity.getItemHandler();
        int startY = 10;
        addSlot(new SlotItemHandler(handler.getInput(), 0, 42, startY));
        addSlot(new SlotItemHandler(handler.getInput(), 1, 42, startY + 18));
        addSlot(new SlotItemHandler(handler.getInput(), 2, 42, startY + (18 * 2)));
        addSlot(new ExtractOnlySlot(handler.getOutput(), 0, 122, startY + 19));

        addPlayerSlots(playerInventory, 8, 84);

        addDataSlots(containerData);
    }

    public static class ExtractOnlySlot extends SlotItemHandler {
        public ExtractOnlySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}
