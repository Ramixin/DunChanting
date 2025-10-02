package net.ramixin.dunchanting.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.client.screens.ModScreens;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.PlayerEntityDuck;

public class DunchantingClient implements ClientModInitializer {

	public static final Identifier ENCHANTMENT_ICONS_ATLAS_TEXTURE = Dunchanting.id("textures/atlas/enchantment_icons.png");
	public static final Identifier ENCHANTMENT_ICONS_ATLAS_ID = Dunchanting.id("enchantment_icons");

	@Override
	public void onInitializeClient() {
		Dunchanting.LOGGER.info("initializing client (1/1)");
		ModMixsonClient.onInitialize();
		ClientPlayNetworking.registerGlobalReceiver(EnchantmentPointsUpdateS2CPayload.PACKET_ID, DunchantingClient::updateClientPlayerEnchantmentPoints);
		ModScreens.onInitialize();
	}

	private static void updateClientPlayerEnchantmentPoints(EnchantmentPointsUpdateS2CPayload payload, ClientPlayNetworking.Context context) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if(player == null) throw new IllegalStateException("client player is null, but received '"+payload+"' payload");
		PlayerEntityDuck.get(player).dungeonEnchants$setEnchantmentPoints(payload.value());
	}

	public static PlayerEntityDuck getPlayerEntityDuck() {
		if(MinecraftClient.getInstance().player == null) throw new NullPointerException("player is null");
		return PlayerEntityDuck.get(MinecraftClient.getInstance().player);
	}
}