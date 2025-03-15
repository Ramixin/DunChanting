package net.ramixin.dunchants.client.screens;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.ramixin.dunchants.handlers.ModHandlers;

public class ModScreens {

    public static void onInitialize() {
        HandledScreens.register(ModHandlers.MOD_GRINDSTONE_HANDLER_TYPE, ModGrindstoneScreen::new);
    }

}
