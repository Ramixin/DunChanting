package net.ramixin.dunchants.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {


    @ModifyReturnValue(method = "isExperienceDroppingDisabled", at = @At("RETURN"))
    private boolean preventXpDrop(boolean original) {
        //noinspection ConstantValue
        return original || ((LivingEntity)(Object)this) instanceof PlayerEntity;
    }
}
