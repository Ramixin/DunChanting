package net.ramixin.dunchanting.client.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.ramixin.dunchanting.client.DunchantingClient;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.etable.EnchantmentTableElement;
import net.ramixin.dunchanting.client.enchantmentui.etable.EnchantmentTableHoverManager;
import net.ramixin.dunchanting.client.util.EnchantmentUIHolder;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.util.EnchantmentOptionsUtil;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Debug(export = true)
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> implements EnchantmentUIHolder {

    @Shadow private ItemStack last;

    @Unique
    private AbstractEnchantmentUIElement enchantingElement = null;


    public EnchantmentScreenMixin(EnchantmentMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void changeBackgroundHeight(EnchantmentMenu handler, Inventory inventory, Component title, CallbackInfo ci) {
        this.imageHeight = 174;
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void applyTickToElements(CallbackInfo ci) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        if(enchantingElement == null) enchantingElement = new EnchantmentTableElement(last, new EnchantmentTableHoverManager(), relX, relY);
        else if(enchantingElement.isInvalid(last)) enchantingElement = enchantingElement.createCopy(last);

        enchantingElement.tick(last);
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void preventDefaultRenderingAndRenderEnchantingUIElement(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        Optional<Integer> pointColor;
        if(enchantingElement == null || last.isEmpty()) pointColor = Optional.empty();
        else {
            enchantingElement.render(context, font, mouseX, mouseY, relX, relY);
            pointColor = enchantingElement.getPointColor();
        }

        int points = DunchantingClient.getPlayerEntityDuck().dungeonEnchants$getEnchantmentPoints();
        String text = String.valueOf(points);
        int width = font.width(text);
        int color = pointColor.orElse(0xFF9c50af);
        context.drawString(font, Component.nullToEmpty(text), relX + 88 - width / 2, relY + 8, color, false);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void applyEnchantmentOnClick(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(super.mouseClicked(click, doubled));
        if(this.minecraft.gameMode == null) return;
        if(enchantingElement == null) return;
        enchantingElement.updateMousePosition(click.x(), click.y());
        int optionId = enchantingElement.getActiveHoverOption();
        if(optionId == -1) return;
        if(optionId < 0) {
            EnchantmentOptions lockedOptions = EnchantmentOptionsUtil.getLocked(last);
            if(lockedOptions.hasEmptySlot(-optionId / 3)) return;
        }
        if(this.minecraft.player == null) return;
        this.menu.clickMenuButton(this.minecraft.player, optionId);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, optionId);
    }

    @Override
    public AbstractEnchantmentUIElement dungeonEnchants$getUIElement() {
        return enchantingElement;
    }
}
