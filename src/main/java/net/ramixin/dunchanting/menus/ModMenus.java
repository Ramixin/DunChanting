package net.ramixin.dunchanting.menus;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.ramixin.dunchanting.Dunchanting;

public class ModMenus {


    public static final MenuType<ModGrindstoneMenu> MOD_GRINDSTONE_HANDLER_TYPE = new MenuType<>(ModGrindstoneMenu::new, FeatureFlags.VANILLA_SET);

    public static void onInitialize() {
        Registry.register(BuiltInRegistries.MENU, Dunchanting.id("grindstone"), MOD_GRINDSTONE_HANDLER_TYPE);
    }
}
