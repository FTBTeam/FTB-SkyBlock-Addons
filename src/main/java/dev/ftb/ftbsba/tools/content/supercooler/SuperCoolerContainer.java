package dev.ftb.ftbsba.tools.content.supercooler;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.fusion.FusingMachineContainer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SuperCoolerContainer extends AbstractContainerMenu {
    SuperCoolerBlockEntity entity;
    ContainerData containerData;

    protected SuperCoolerContainer(int i, ContainerData containerData, Inventory arg, Player arg2, SuperCoolerBlockEntity entity) {
        super(ToolsRegistry.SUPER_COOLER_CONTAINER.get(), i);
        FusingMachineContainer.addPlayerSlots(arg, 8, 56, this::addSlot);

        this.entity = entity;
        this.entity.ioWrapper.ifPresent(inventory -> {
            int startY = 10;
            addSlot(new SlotItemHandler(inventory.getInput(), 0, 42, startY));
            addSlot(new SlotItemHandler(inventory.getInput(), 1, 42, startY + 18));
            addSlot(new SlotItemHandler(inventory.getInput(), 2, 42, startY + (18 * 2)));
            addSlot(new ExtractOnlySlot(inventory.getOutput(), 0, 122, startY + 19));
        });

        this.containerData = containerData;
        addDataSlots(containerData);
    }

    public SuperCoolerContainer(int i, Inventory arg, FriendlyByteBuf arg2) {
        this(i, arg, arg.player, (SuperCoolerBlockEntity) arg.player.level.getBlockEntity(arg2.readBlockPos()));
    }

    public SuperCoolerContainer(int i, Inventory arg, Player player, SuperCoolerBlockEntity entity) {
        this(i, entity.containerData, arg, player, entity);
    }

    @Override
    public ItemStack quickMoveStack(Player arg, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem();
            itemstack = currentStack.copy();

            if (index > 35) {
                if (!this.moveItemStackTo(currentStack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(currentStack, 36, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player arg) {
        Vec3 position = arg.position();
        return this.entity.getBlockPos().distManhattan(new Vec3i(position.x, position.y, position.z)) <= 8;
    }

    @Override
    public void slotsChanged(Container arg) {
        super.slotsChanged(arg);
        this.entity.setChanged();
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
