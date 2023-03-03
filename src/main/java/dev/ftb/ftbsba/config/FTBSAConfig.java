package dev.ftb.ftbsba.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class FTBSAConfig {
    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static final CategoryHammers HAMMERS = new CategoryHammers();
    public static final CategoryDimensions DIMENSIONS = new CategoryDimensions();

    static {
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static class CategoryDimensions {
        public final ForgeConfigSpec.BooleanValue clearPlayerInventory;

        public CategoryDimensions() {
            COMMON_BUILDER.push("hammers");

            this.clearPlayerInventory = COMMON_BUILDER
                    .comment("When set to true, the players inventory will be cleared when leaving a team")
                    .define("clearPlayerInventory", true);

            COMMON_BUILDER.pop();
        }
    }

    public static class CategoryHammers {
        public final ForgeConfigSpec.IntValue speedIron;
        public final ForgeConfigSpec.IntValue speedGold;
        public final ForgeConfigSpec.IntValue speedDiamond;
        public final ForgeConfigSpec.IntValue speedNetherite;

        private CategoryHammers() {
            COMMON_BUILDER.push("hammers");

            this.speedIron = COMMON_BUILDER
                    .comment("Speed of the iron auto-hammer as ticks taken to process the block")
                    .defineInRange("ironSpeed", 50, 1, 100000);

            this.speedGold = COMMON_BUILDER
                    .comment("Speed of the gold auto-hammer as ticks taken to process the block")
                    .defineInRange("goldSpeed", 40, 1, 100000);

            this.speedDiamond = COMMON_BUILDER
                    .comment("Speed of the diamond auto-hammer as ticks taken to process the block")
                    .defineInRange("diamondSpeed", 30, 1, 100000);

            this.speedNetherite = COMMON_BUILDER.comment(
                            "Speed of the netherite auto-hammer as ticks taken to process the block")
                    .defineInRange("netheriteSpeed", 15, 1, 100000);

            COMMON_BUILDER.pop();
        }
    }
}
