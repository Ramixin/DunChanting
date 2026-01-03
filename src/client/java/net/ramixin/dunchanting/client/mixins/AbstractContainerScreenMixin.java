package net.ramixin.dunchanting.client.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.ramixin.dunchanting.client.util.ScreenDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    private void preventEnchantmentTableTitleRendering(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if((AbstractContainerScreen<?>)(Object)this instanceof EnchantmentScreen) ci.cancel();
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void tickDuckedScreens(CallbackInfo ci) {
        if(this instanceof ScreenDuck screenDuck) screenDuck.dungeonEnchants$tick();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderDuckedScreens(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(this instanceof ScreenDuck screenDuck) screenDuck.dungeonEnchants$render(context, mouseX, mouseY, delta);
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void mouseClickedDuckedScreen(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if(this instanceof ScreenDuck screenDuck) screenDuck.dungeonEnchants$mouseClicked(click, doubled);
    }
}
