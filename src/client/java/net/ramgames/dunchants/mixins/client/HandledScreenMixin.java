package net.ramgames.dunchants.mixins.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void preventEnchantmentTableTitleRendering(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if((HandledScreen<?>)(Object)this instanceof EnchantmentScreen) ci.cancel();
    }
}
