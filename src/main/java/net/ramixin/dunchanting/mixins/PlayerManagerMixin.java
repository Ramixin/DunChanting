package net.ramixin.dunchanting.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.PlayerEntityDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 4))
    private void preventXpFromBeingLostOnDeath(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir, @Local(ordinal = 1) ServerPlayerEntity serverPlayer) {
        serverPlayer.setExperienceLevel(player.experienceLevel);
        serverPlayer.setExperiencePoints(player.totalExperience);
        serverPlayer.experienceProgress = player.experienceProgress;
        PlayerEntityDuck duck = PlayerEntityDuck.get(player);
        PlayerEntityDuck serverDuck = PlayerEntityDuck.get(serverPlayer);
        serverDuck.dungeonEnchants$setEnchantmentPoints(duck.dungeonEnchants$getEnchantmentPoints());
        ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(duck.dungeonEnchants$getEnchantmentPoints()));
    }
}
