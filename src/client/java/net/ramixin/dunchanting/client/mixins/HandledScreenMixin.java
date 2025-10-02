package net.ramixin.dunchanting.client.mixins;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.ramixin.dunchanting.client.util.ScreenDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void preventEnchantmentTableTitleRendering(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if((HandledScreen<?>)(Object)this instanceof EnchantmentScreen) ci.cancel();
    }

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void tickDuckedScreens(CallbackInfo ci) {
        if(this instanceof ScreenDuck screenDuck) screenDuck.dungeonEnchants$tick();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderDuckedScreens(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(this instanceof ScreenDuck screenDuck) screenDuck.dungeonEnchants$render(context, mouseX, mouseY, delta);
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void mouseClickedDuckedScreen(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if(this instanceof ScreenDuck screenDuck) screenDuck.dungeonEnchants$mouseClicked(click, doubled);
    }
}
