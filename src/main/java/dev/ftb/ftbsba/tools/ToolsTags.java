package dev.ftb.ftbsba.tools;

import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.FTBStoneBlock;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ToolsTags {
    public static class Items {
        public static final TagKey<Item> HAMMERS = bind("hammers");
        public static final TagKey<Item> CROOKS = bind("crooks");

        private static TagKey<Item> bind(String name) {
            return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(String.format("%s:%s", FTBSBA.MOD_ID, name)));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> AUTO_HAMMERS = tag("auto_hammers");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(String.format("%s:%s", FTBSBA.MOD_ID, name)));
        }
    }
}
