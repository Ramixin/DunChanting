package net.ramixin.dunchanting.mixins.attribution;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ramixin.dunchanting.AttributionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getItem();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", ordinal = 1))
    private void redistributePointsOnItemDiscard(CallbackInfo ci) {
        if(!(level() instanceof ServerLevel world)) return;
        ItemStack stack = getItem();
        AttributionManager.redistribute(stack, world);
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"))
    private void redistributePointsOnItemDestroyed(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = getItem();
        AttributionManager.redistribute(stack, world);
    }

}
