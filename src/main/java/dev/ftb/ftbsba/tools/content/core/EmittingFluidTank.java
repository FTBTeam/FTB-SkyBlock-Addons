package dev.ftb.ftbsba.tools.content.core;

import dev.ftb.ftbsba.tools.net.FluidTankSync;
import dev.ftb.ftbsba.tools.net.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EmittingFluidTank extends FluidTank {
    private final Consumer<EmittingFluidTank> onChange;
    private boolean syncAllObservers;
    private final Set<ServerPlayer> toSync = Collections.newSetFromMap(new WeakHashMap<>());

    public EmittingFluidTank(int capacity, Consumer<EmittingFluidTank> onChange) {
        super(capacity);
        this.onChange = onChange;
    }

    public EmittingFluidTank(int capacity, Predicate<FluidStack> validator, Consumer<EmittingFluidTank> onChange) {
        super(capacity, validator);
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        onChange.accept(this);
        needSync();
    }

    public void needSync(ServerPlayer... players) {
        if (players.length == 0) {
            syncAllObservers = true;
        } else {
            toSync.addAll(Arrays.asList(players));
        }
    }

    public void sync(BlockEntity blockEntity) {
        FluidTankSync fluidTankSync = new FluidTankSync(blockEntity.getBlockPos(), fluid);
        if (syncAllObservers) {
            blockEntity.getLevel().getServer().getPlayerList().getPlayers().stream()
                    .filter(p -> p.containerMenu instanceof AbstractMachineMenu<?> prov && prov.getBlockEntity() == blockEntity)
                    .forEach(p -> NetworkHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> p), fluidTankSync));
        } else {
            toSync.forEach(p -> NetworkHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> p), fluidTankSync));
        }
        syncAllObservers = false;
        toSync.clear();
    }
}
