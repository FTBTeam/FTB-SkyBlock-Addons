package dev.ftb.ftbsba.tools.integration.kubejs;

import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.FTBStoneBlock;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraft.resources.ResourceLocation;

public class KubeJSIntegration extends KubeJSPlugin {

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "hammer"), HammerRecipeJS::new);
        event.register(new ResourceLocation(FTBSBA.MOD_ID, "crook"), CrookRecipeJS::new);
    }
}
