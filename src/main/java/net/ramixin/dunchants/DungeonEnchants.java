package net.ramixin.dunchants;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchants.handlers.ModHandlers;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.loot.ModSubPredicateTypes;
import net.ramixin.dunchants.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.mixson.debug.DebugMode;
import net.ramixin.mixson.inline.Mixson;
import net.ramixin.util.PlayerEntityDuck;
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