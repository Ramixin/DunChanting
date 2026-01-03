package net.ramixin.dunchanting.client.screens;

import net.minecraft.client.gui.screens.MenuScreens;
import net.ramixin.dunchanting.menus.ModMenus;

public class ModScreens {

    public static void onInitialize() {
        MenuScreens.register(ModMenus.MOD_GRINDSTONE_HANDLER_TYPE, ModGrindstoneScreen::new);
    }

}
