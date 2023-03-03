package dev.ftb.ftbsba.tools.integration.jade;

import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerBlock;
import dev.ftb.ftbsba.tools.content.autohammer.AutoHammerBlockEntity;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin
public class SBCJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AutoHammerComponentProvider.INSTANCE, AutoHammerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(AutoHammerComponentProvider.INSTANCE, TooltipPosition.BODY, AutoHammerBlock.class);
    }

    enum AutoHammerComponentProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            CompoundTag serverData = blockAccessor.getServerData();
            if (!serverData.contains("progress")) {
                return;
            }

            var helper = iTooltip.getElementHelper();

            int timeout = serverData.getInt("timeout");
            int maxTimeout = serverData.getInt("maxTimeout");

            if (timeout == 0) {
                iTooltip.add(helper.progress((float) serverData.getInt("progress") / (float) serverData.getInt("maxProgress"), null, helper.progressStyle().color(0xAD00FF00), helper.borderStyle().color(0x0FFFFFFF)));
            } else {
                iTooltip.add(helper.progress((float) timeout / (float) maxTimeout, null, helper.progressStyle().color(0xADFF0000), helper.borderStyle().color(0x0FFFFFFF)));
            }

            var inputStack = LightItem.deserialize(serverData.getCompound("input")).toStack();

            List<ItemStack> outputItems = new ArrayList<>();
            if (serverData.contains("output")) {
                var items = serverData.getList("output", Tag.TAG_COMPOUND);
                for (Tag item : items) {
                    outputItems.add(LightItem.deserialize((CompoundTag) item).toStack());
                }
            }

            if (!inputStack.isEmpty()) {
                iTooltip.add(helper.item(inputStack));
                ITooltip tooltip = helper.tooltip();
                tooltip.append(helper.text(new TranslatableComponent("ftbsbc.jade.input")));
                iTooltip.append(helper.box(tooltip, helper.borderStyle().width(0)).align(IElement.Align.RIGHT));
            }

            if (!outputItems.isEmpty()) {
                iTooltip.add(helper.spacer(-5, 0));
                ITooltip tooltip = helper.tooltip();

                // Creates rows of 5 to prevent the box getting too big.
                int count = 0;
                float scale = outputItems.size() > 5 ? .8f : 1f;
                for (ItemStack outputItem : outputItems) {
                    if (count != 0 && count % 5 == 0) {
                        tooltip.add(helper.item(outputItem, scale));
                        count = 0;
                        continue;
                    }
                    tooltip.append(helper.item(outputItem, scale));
                    count ++;
                }

                iTooltip.append(helper.box(tooltip,  helper.borderStyle().width(0)));

                // Hacks to make the boxes not look stupid
                ITooltip text = helper.tooltip();
                text.append(helper.text(new TranslatableComponent("ftbsbc.jade.buffer")));
                iTooltip.append(helper.box(text, helper.borderStyle().width(0)).align(IElement.Align.RIGHT));
            }
        }

        @Override
        public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
            if (!(blockEntity instanceof AutoHammerBlockEntity autoHammerEntity)) {
                return;
            }

            compoundTag.putInt("progress", autoHammerEntity.getProgress());
            compoundTag.putInt("maxProgress", autoHammerEntity.getMaxProgress());
            compoundTag.putInt("timeout", autoHammerEntity.getTimeOut());
            compoundTag.putInt("maxTimeout", autoHammerEntity.getTimeoutDuration());

            Direction direction = autoHammerEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            ItemStack inputStack = autoHammerEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, AutoHammerBlockEntity.getInputDirection(direction))
                    .map(h -> h.getStackInSlot(0))
                    .orElse(ItemStack.EMPTY);

            compoundTag.put("input", LightItem.create(inputStack).serialize());

            LazyOptional<IItemHandler> capability = autoHammerEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, AutoHammerBlockEntity.getOutputDirection(direction));
            IItemHandler inventory = capability
                    .orElse(EmptyHandler.INSTANCE);

            ListTag tagItems = new ListTag();
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stackInSlot = inventory.getStackInSlot(i);
                if (stackInSlot.isEmpty()) {
                    continue;
                }

                tagItems.add(new LightItem(stackInSlot.getItem().getRegistryName(), stackInSlot.getCount()).serialize());
            }

            compoundTag.put("output", tagItems);
        }
    }

    record LightItem(
            ResourceLocation location,
            int count
    ) {
        public static LightItem create(ItemStack stack) {
            return new LightItem(stack.getItem().getRegistryName(), stack.getCount());
        }

        public static LightItem deserialize(CompoundTag compound) {
            if (!compound.contains("name")) {
                return new LightItem(new ResourceLocation("minecraft:air"), 0);
            }

            return new LightItem(new ResourceLocation(compound.getString("name")), compound.contains("count") ? compound.getInt("count") : 1);
        }

        public CompoundTag serialize() {
            var tag = new CompoundTag();
            tag.putInt("count", this.count);
            tag.putString("name", this.location.toString());
            return tag;
        }

        public ItemStack toStack() {
            Item value = ForgeRegistries.ITEMS.getValue(this.location);
            if (value == null) {
                return ItemStack.EMPTY;
            }

            return new ItemStack(value, this.count);
        }
    }
}
