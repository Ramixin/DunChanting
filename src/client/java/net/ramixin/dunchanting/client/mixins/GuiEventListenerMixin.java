package net.ramixin.dunchanting.client.mixins;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.util.EnchantmentUIHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiEventListener.class)
public interface GuiEventListenerMixin {

    @Inject(method = "mouseMoved", at = @At("TAIL"))
    private void updateEnchantmentElementOnMouseMove(double mouseX, double mouseY, CallbackInfo ci) {
        if(!(((Object)this) instanceof EnchantmentUIHolder holder)) return;
        AbstractEnchantmentUIElement element = holder.dungeonEnchants$getUIElement();
        if(element == null) return;
        element.updateMousePosition(mouseX, mouseY);
    }

}
