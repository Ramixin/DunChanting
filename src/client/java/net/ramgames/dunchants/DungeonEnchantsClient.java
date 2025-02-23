package net.ramgames.dunchants;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

public class DungeonEnchantsClient implements ClientModInitializer {

	public static final Identifier ENCHANTMENT_ICONS_ATLAS_TEXTURE = DungeonEnchants.id("textures/atlas/enchantment_icons.png");
	public static final Identifier ENCHANTMENT_ICONS_ATLAS_ID = DungeonEnchants.id("enchantment_icons");

	public static final Identifier LARGE_ENCHANTMENT_ICONS_ATLAS_TEXTURE = DungeonEnchants.id("textures/atlas/large_enchantment_icons.png");
	public static final Identifier LARGE_ENCHANTMENT_ICONS_ATLAS_ID = DungeonEnchants.id("large_enchantment_icons");

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}