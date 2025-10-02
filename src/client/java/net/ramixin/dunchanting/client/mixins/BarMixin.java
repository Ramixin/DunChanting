package net.ramixin.dunchanting.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bar.class)
public interface BarMixin {


    @WrapOperation(method = "drawExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V", ordinal = 4))
    private static void changeExperienceLevelNumberDisplayColor(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int c, boolean b, Operation<Void> original) {
        original.call(instance, textRenderer, text, x, y, 0xB978FB, b);
    }
}
