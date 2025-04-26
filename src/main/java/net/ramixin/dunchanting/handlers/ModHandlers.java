package net.ramixin.dunchanting.handlers;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.ramixin.dunchanting.Dunchanting;

public class ModHandlers {


    public static final ScreenHandlerType<ModGrindstoneScreenHandler> MOD_GRINDSTONE_HANDLER_TYPE = new ScreenHandlerType<>(ModGrindstoneScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static void onInitialize() {
        Registry.register(Registries.SCREEN_HANDLER, Dunchanting.id("grindstone"), MOD_GRINDSTONE_HANDLER_TYPE);
    }
}
