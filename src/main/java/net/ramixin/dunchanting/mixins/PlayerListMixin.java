package net.ramixin.dunchanting.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 4))
    private void preventXpFromBeingLostOnDeath(ServerPlayer player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir, @Local(ordinal = 1) ServerPlayer serverPlayer) {
        serverPlayer.setExperienceLevels(player.experienceLevel);
        serverPlayer.setExperiencePoints(player.totalExperience);
        serverPlayer.experienceProgress = player.experienceProgress;
        PlayerDuck duck = PlayerDuck.get(player);
        PlayerDuck serverDuck = PlayerDuck.get(serverPlayer);
        serverDuck.dungeonEnchants$setEnchantmentPoints(duck.dungeonEnchants$getEnchantmentPoints());
        ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(duck.dungeonEnchants$getEnchantmentPoints()));
    }
}
