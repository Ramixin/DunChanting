package net.ramixin.dunchants.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Shadow public int experienceLevel;

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    private void changeXpProgression(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) (50 * Math.pow(1.25, experienceLevel)));
    }

    @ModifyReturnValue(method = "shouldAlwaysDropXp", at = @At("RETURN"))
    private boolean changeShouldAlwaysDropXp(boolean original) {
        return true;
    }

    @ModifyReturnValue(method = "getXpToDrop", at = @At("RETURN"))
    private int preventXpOrbsFromDropping(int original) {
        return 0;
    }
}
