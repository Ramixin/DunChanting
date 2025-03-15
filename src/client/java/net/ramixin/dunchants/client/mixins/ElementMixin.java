package net.ramixin.dunchants.client.mixins;

import net.minecraft.client.gui.Element;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.client.util.EnchantmentUIHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Element.class)
public interface ElementMixin {

    @Inject(method = "mouseMoved", at = @At("TAIL"))
    private void updateEnchantmentElementOnMouseMove(double mouseX, double mouseY, CallbackInfo ci) {
        if(!(((Object)this) instanceof EnchantmentUIHolder holder)) return;
        AbstractEnchantmentUIElement element = holder.dungeonEnchants$getUIElement();
        if(element == null) return;
        element.updateMousePosition(mouseX, mouseY);
    }

}
