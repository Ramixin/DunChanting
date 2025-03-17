package net.ramixin.dunchants.mixins.anvil;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.ramixin.dunchants.util.ClickableHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @ModifyReturnValue(method = "onButtonClick", at = @At("RETURN"))
    private boolean clickClickableHandlers(boolean original, @Local(argsOnly = true) PlayerEntity player, @Local(argsOnly = true) int button) {
        if(original) return true;
        if(this instanceof ClickableHandler clickableHandler) return clickableHandler.dungeonEnchants$onClick(player, button);
        return false;
    }

}
