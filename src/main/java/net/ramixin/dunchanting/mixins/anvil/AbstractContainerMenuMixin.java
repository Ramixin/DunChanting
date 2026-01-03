package net.ramixin.dunchanting.mixins.anvil;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.ramixin.dunchanting.util.ClickableHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @ModifyReturnValue(method = "clickMenuButton", at = @At("RETURN"))
    private boolean clickClickableHandlers(boolean original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) int button) {
        if(original) return true;
        if(this instanceof ClickableHandler clickableHandler) return clickableHandler.dungeonEnchants$onClick(player, button);
        return false;
    }

}
