package dev.ftb.ftbsba.tools.net;

import dev.ftb.ftbsba.tools.ToolsClient;
import dev.ftb.ftbsba.tools.content.core.AbstractMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FluidTankSync {
    private final BlockPos pos;
    private final FluidStack fluidStack;

    public FluidTankSync(BlockPos pos, FluidStack fluidStack) {
        this.pos = pos;
        this.fluidStack = fluidStack;
    }

    public FluidTankSync(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        fluidStack = buf.readFluidStack();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeFluidStack(fluidStack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ToolsClient.getBlockEntityAt(pos, AbstractMachineBlockEntity.class)
                    .ifPresent(holder -> holder.setFluid(fluidStack));
        });
        ctx.get().setPacketHandled(true);
    }
}
