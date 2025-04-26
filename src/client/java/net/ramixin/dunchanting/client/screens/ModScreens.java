package net.ramixin.dunchanting.client.screens;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.ramixin.dunchanting.handlers.ModHandlers;

public class ModScreens {

    public static void onInitialize() {
        HandledScreens.register(ModHandlers.MOD_GRINDSTONE_HANDLER_TYPE, ModGrindstoneScreen::new);
    }

}
