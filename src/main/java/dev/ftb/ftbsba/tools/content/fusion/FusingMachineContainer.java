package dev.ftb.ftbsba.tools.content.fusion;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerBlockEntity;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FusingMachineContainer extends AbstractContainerMenu {
    FusingMachineBlockEntity entity;
    ContainerData containerData;

    protected FusingMachineContainer(int i, ContainerData data, Inventory arg, Player arg2, FusingMachineBlockEntity entity) {
        super(ToolsRegistry.FUSING_MACHINE_CONTAINER.get(), i);

        addPlayerSlots(arg, 8, 56, this::addSlot);

        this.entity = entity;
        this.entity.input.ifPresent(itemHandler -> {
            addSlot(new SlotItemHandler(itemHandler, 0, 43, 27));
            addSlot(new SlotItemHandler(itemHandler, 1, 43 + 18, 27));
        });

        this.containerData = data;
        addDataSlots(data);
    }

    public FusingMachineContainer(int i, Inventory arg, FriendlyByteBuf arg2) {
        this(i, arg, arg.player, (FusingMachineBlockEntity) arg.player.level.getBlockEntity(arg2.readBlockPos()));
    }

    public FusingMachineContainer(int i, Inventory arg, Player player, FusingMachineBlockEntity entity) {
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
            } else if (!this.moveItemStackTo(currentStack, 36, 38, false)) {
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

    public static void addPlayerSlots(Inventory playerInventory, int inX, int inY, Consumer<Slot> addSlot) {
        // Slots for the hotbar
        for (int row = 0; row < 9; ++ row) {
            int x = inX + row * 18;
            int y = inY + 86;
            addSlot.accept(new Slot(playerInventory, row, x, y));
        }
        // Slots for the main inventory
        for (int row = 1; row < 4; ++ row) {
            for (int col = 0; col < 9; ++ col) {
                int x = inX + col * 18;
                int y = row * 18 + (inY + 10);
                addSlot.accept(new Slot(playerInventory, col + row * 9, x, y));
            }
        }
    }
}
