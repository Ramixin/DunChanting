package net.ramixin.dunchants.mixins.attribution;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.ramixin.dunchants.AttributionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStack();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V", ordinal = 1))
    private void redistributePointsOnItemDiscard(CallbackInfo ci) {
        if(!(getWorld() instanceof ServerWorld world)) return;
        ItemStack stack = getStack();
        AttributionManager.redistribute(stack, world);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V"))
    private void redistributePointsOnItemDestroyed(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = getStack();
        AttributionManager.redistribute(stack, world);
    }

}
