package net.ramixin.dunchants;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.loot.ModSubPredicateTypes;
import net.ramixin.mixson.debug.DebugMode;
import net.ramixin.mixson.inline.Mixson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DungeonEnchants implements ModInitializer {

	public static final String MOD_ID = "dungeon_enchants";
    public static final Logger LOGGER = LoggerFactory.getLogger("Dungeon Enchants");

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("initializing (1/1)");
		if(FabricLoader.getInstance().isDevelopmentEnvironment()) Mixson.setDebugMode(DebugMode.EXPORT);
		ModItemComponents.onInitialize();
		ModEnchantmentEffects.onInitialize();
		ModSubPredicateTypes.onInitialize();
		ModMixson.onInitialize();

	}
}