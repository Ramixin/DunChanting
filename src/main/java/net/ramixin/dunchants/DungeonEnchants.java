package net.ramixin.dunchants;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
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
		LOGGER.info("init");
		ModItemComponents.onInitialize();
	}
}