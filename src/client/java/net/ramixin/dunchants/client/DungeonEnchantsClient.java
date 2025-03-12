package net.ramixin.dunchants.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.DungeonEnchants;
import net.ramixin.dunchants.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.util.PlayerEntityDuck;

public class DungeonEnchantsClient implements ClientModInitializer {

	public static final Identifier ENCHANTMENT_ICONS_ATLAS_TEXTURE = DungeonEnchants.id("textures/atlas/enchantment_icons.png");
	public static final Identifier ENCHANTMENT_ICONS_ATLAS_ID = DungeonEnchants.id("enchantment_icons");

	@Override
	public void onInitializeClient() {
		ModMixsonClient.onInitialize();
		ClientPlayNetworking.registerGlobalReceiver(EnchantmentPointsUpdateS2CPayload.PACKET_ID, DungeonEnchantsClient::updateClientPlayerEnchantmentPoints);
	}

	private static void updateClientPlayerEnchantmentPoints(EnchantmentPointsUpdateS2CPayload payload, ClientPlayNetworking.Context context) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if(player == null) throw new IllegalStateException("client player is null, but received '"+payload+"' payload");
		PlayerEntityDuck.get(player).dungeonEnchants$setEnchantmentPoints(payload.value());
	}

	public static Registry<Enchantment> getEnchantmentRegistry() {
		if(MinecraftClient.getInstance().world == null) throw new IllegalStateException();
		return MinecraftClient.getInstance().world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
	}

	public static PlayerEntityDuck getPlayerEntityDuck() {
		if(MinecraftClient.getInstance().player == null) throw new NullPointerException("player is null");
		return PlayerEntityDuck.get(MinecraftClient.getInstance().player);
	}
}