package net.ramixin.dunchanting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.ramixin.dunchanting.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchanting.handlers.ModHandlers;
import net.ramixin.dunchanting.items.ModItemComponents;
import net.ramixin.dunchanting.loot.ModSubPredicateTypes;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.PlayerEntityDuck;
import net.ramixin.mixson.debug.DebugMode;
import net.ramixin.mixson.inline.Mixson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dunchanting implements ModInitializer {

	public static final String MOD_ID = "dunchanting";
    public static final Logger LOGGER = LoggerFactory.getLogger("Dungeon Enchants");

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static String idString(String path) {
		return MOD_ID + ":" + path;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("initializing (1/1)");
		if(FabricLoader.getInstance().isDevelopmentEnvironment()) Mixson.setDebugMode(DebugMode.EXPORT);
		ModItemComponents.onInitialize();
		ModEnchantmentEffects.onInitialize();
		ModSubPredicateTypes.onInitialize();
		ModMixson.onInitialize();
		PayloadTypeRegistry.playS2C().register(EnchantmentPointsUpdateS2CPayload.PACKET_ID, EnchantmentPointsUpdateS2CPayload.PACKET_CODEC);
		ModHandlers.onInitialize();

		ServerPlayConnectionEvents.JOIN.register((handler, server, client) -> {
			ServerPlayerEntity player = handler.getPlayer();
			int attributions = AttributionManager.popAttributions(player.getUuid());
			PlayerEntityDuck duck = (PlayerEntityDuck) player;
			duck.dungeonEnchants$changeEnchantmentPoints(attributions);
			ServerPlayNetworking.send(player, new EnchantmentPointsUpdateS2CPayload(duck.dungeonEnchants$getEnchantmentPoints()));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, unused2) -> ModCommands.register(dispatcher.getRoot(), registryAccess));
	}
}