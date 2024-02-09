package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.ftb.ftbsba.FTBSBA;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.minecraft.resources.ResourceLocation;

public class KubeJSIntegration extends KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "hammer"), HammerRecipeSchema.SCHEMA);
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "crook"), CrookRecipeSchema.SCHEMA);
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "fusing_machine"), FusingMachineRecipeSchema.SCHEMA);
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "super_cooler"), SuperCoolerRecipeSchema.SCHEMA);
    }
}
