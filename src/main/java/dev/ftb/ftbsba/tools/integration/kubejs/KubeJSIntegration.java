package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.ftb.ftbsba.FTBSBA;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeTypesEvent;
import net.minecraft.resources.ResourceLocation;

public class KubeJSIntegration extends KubeJSPlugin {

    @Override
    public void registerRecipeTypes(RegisterRecipeTypesEvent event) {
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "hammer"), HammerRecipeJS::new);
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "crook"), CrookRecipeJS::new);
    }
}
