package net.ramixin.dunchants.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.DungeonEnchants;

public class DungeonEnchantsClient implements ClientModInitializer {

	public static final Identifier ENCHANTMENT_ICONS_ATLAS_TEXTURE = DungeonEnchants.id("textures/atlas/enchantment_icons.png");
	public static final Identifier ENCHANTMENT_ICONS_ATLAS_ID = DungeonEnchants.id("enchantment_icons");

	@Override
	public void onInitializeClient() {
	}
}