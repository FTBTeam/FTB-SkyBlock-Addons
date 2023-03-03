package dev.ftb.ftbsba.tools.content.autohammer;

import dev.ftb.ftbsba.config.FTBSAConfig;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Supplier;

public enum AutoHammerProperties {
    IRON(ToolsRegistry.IRON_HAMMER, FTBSAConfig.HAMMERS.speedIron),
    GOLD(ToolsRegistry.GOLD_HAMMER , FTBSAConfig.HAMMERS.speedGold),
    DIAMOND(ToolsRegistry.DIAMOND_HAMMER, FTBSAConfig.HAMMERS.speedDiamond),
    NETHERITE(ToolsRegistry.NETHERITE_HAMMER, FTBSAConfig.HAMMERS.speedNetherite);

    final Supplier<Item> hammerItem;
    final ForgeConfigSpec.IntValue hammerSpeed;

    AutoHammerProperties(Supplier<Item> hammerItem, ForgeConfigSpec.IntValue hammerSpeed) {
        this.hammerItem = hammerItem;
        this.hammerSpeed = hammerSpeed;
    }

    public Supplier<Item> getHammerItem() {
        return hammerItem;
    }

    public ForgeConfigSpec.IntValue getHammerSpeed() {
        return hammerSpeed;
    }
}
