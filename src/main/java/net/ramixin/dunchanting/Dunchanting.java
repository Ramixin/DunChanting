package net.ramixin.dunchanting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.dunchanting.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.loot.ModFunctionTypes;
import net.ramixin.dunchanting.loot.ModSubPredicateTypes;
import net.ramixin.dunchanting.menus.ModMenus;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.PlayerDuck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dunchanting implements ModInitializer {

	public static final String MOD_ID = "dunchanting";
    public static final Logger LOGGER = LoggerFactory.getLogger("Dungeon Enchants");

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static String idString(String path) {
		return MOD_ID + ":" + path;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("initializing (2/2)");
		//if(FabricLoader.getInstance().isDevelopmentEnvironment()) Mixson.setDebugMode(DebugMode.EXPORT);
		ModDataComponents.onInitialize();
		ModEnchantmentEffects.onInitialize();
		ModSubPredicateTypes.onInitialize();
		ModMixson.onInitialize();
		PayloadTypeRegistry.playS2C().register(EnchantmentPointsUpdateS2CPayload.PACKET_ID, EnchantmentPointsUpdateS2CPayload.PACKET_CODEC);
		ModMenus.onInitialize();
		ModFunctionTypes.onInitialize();

		ServerPlayConnectionEvents.JOIN.register((handler, server, client) -> {
			ServerPlayer player = handler.getPlayer();
			int attributions = AttributionManager.popAttributions(player.getUUID());
			PlayerDuck duck = (PlayerDuck) player;
			duck.dungeonEnchants$changeEnchantmentPoints(attributions);
			ServerPlayNetworking.send(player, new EnchantmentPointsUpdateS2CPayload(duck.dungeonEnchants$getEnchantmentPoints()));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, unused2) -> ModCommands.register(dispatcher.getRoot(), registryAccess));
	}
}