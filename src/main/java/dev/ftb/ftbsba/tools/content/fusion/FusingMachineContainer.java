package dev.ftb.ftbsba.tools.content.fusion;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FusingMachineContainer extends AbstractContainerMenu {
    protected FusingMachineContainer(int i, Inventory arg, Player arg2) {
        super(ToolsRegistry.FUSING_MACHINE_CONTAINER.get(), i);

        addPlayerSlots(arg, 8, 56, this::addSlot);

    }

    public FusingMachineContainer(int i, Inventory arg, FriendlyByteBuf arg2) {
        this(i, arg, arg.player);
    }

    @Override
    public ItemStack quickMoveStack(Player arg, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player arg) {
        return true;
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
