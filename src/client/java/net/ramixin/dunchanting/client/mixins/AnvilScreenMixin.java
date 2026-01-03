package net.ramixin.dunchanting.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.anvil.EnchantedBookElement;
import net.ramixin.dunchanting.client.enchantmentui.anvil.EnchantedBookHoverManager;
import net.ramixin.dunchanting.client.enchantmentui.anvil.TransferElement;
import net.ramixin.dunchanting.client.enchantmentui.anvil.TransferHoverManager;
import net.ramixin.dunchanting.client.util.EnchantmentUIHolder;
import net.ramixin.dunchanting.client.util.ModTextures;
import net.ramixin.dunchanting.client.util.ScreenDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> implements ScreenDuck, EnchantmentUIHolder {

    @Shadow private EditBox name;

    @Shadow @Final private Player player;
    @Unique
    private AbstractEnchantmentUIElement bookElement = null;

    @Unique
    private boolean showBookEnchants = false;

    @Unique
    private int selectionIndex = -1;

    @Unique
    private boolean hideTransfer = false;

    @Unique
    private AbstractEnchantmentUIElement transferElement = null;

    public AnvilScreenMixin(AnvilMenu handler, Inventory playerInventory, Component title, Identifier texture) {
        super(handler, playerInventory, title, texture);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void changeBackgroundHeight(AnvilMenu handler, Inventory inventory, Component title, CallbackInfo ci) {
        this.imageHeight = 184;
    }

    @Inject(method = "subInit", at = @At("TAIL"))
    private void setNameFieldAsDisabledOnScreenOpen(CallbackInfo ci) {
        this.name.visible = false;
        this.name.active = false;
    }

    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    protected void cancelSuperForegroundRender(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "slotChanged", at = @At("HEAD"))
    private void hideTextFieldUnlessItemInFirstAndNotSecondSlot(AbstractContainerMenu handler, int slotId, ItemStack stack, CallbackInfo ci) {
        boolean val = !handler.getSlot(1).getItem().is(Items.ENCHANTED_BOOK);
        this.name.visible = val;
        this.name.active = val;
    }

    @WrapOperation(method = "subInit", at = @At(value = "NEW", target = "(Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/components/EditBox;"))
    private EditBox moveTextField(Font textRenderer, int x, int y, int width, int height, Component text, Operation<EditBox> original) {
        return original.call(textRenderer, x - 26, y + 30, width, height, text);
    }

    @WrapOperation(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void moveTextFieldTextures(GuiGraphics context, RenderPipeline renderPipeline, Identifier identifier, int x, int y, int w, int h, Operation<Void> original) {
        if(this.name.visible) {
            context.blitSprite(renderPipeline, ModTextures.anvilTextFieldBackdrop, x - 32, y + 24, 122, 28);
            original.call(context, renderPipeline, identifier, x - 26, y + 30, w, h);
        }
    }

    @WrapOperation(method = "renderErrorIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void moveInvalidRecipeArrow(GuiGraphics context, RenderPipeline renderPipeline, Identifier identifier, int x, int y, int w, int h, Operation<Void> original) {
        original.call(context, renderPipeline, identifier, x - 9, y - 41, w - 3, h - 1);
    }

    @Override
    public void dungeonEnchants$tick() {
        showBookEnchants = false;
        ItemStack primary = menu.getSlot(0).getItem();
        ItemStack secondary = menu.getSlot(1).getItem();
        if(!secondary.is(Items.ENCHANTED_BOOK) || primary.is(Items.ENCHANTED_BOOK)) {
            selectionIndex = -1;
            bookElement = null;
            transferElement = null;
            hideTransfer = false;
            return;
        }
        int relX = (width - imageWidth) / 2;
        int relY = (height - imageHeight) / 2;

        if(bookElement instanceof EnchantedBookElement enchantedBookElement)
            if(enchantedBookElement.getEnchantableStack() != primary)
                bookElement = null;

        if(bookElement == null) bookElement = new EnchantedBookElement(secondary, primary, new EnchantedBookHoverManager(), relX, relY);
        else if(bookElement.isInvalid(secondary)) bookElement = bookElement.createCopy(secondary);

        bookElement.tick(secondary);

        if(EnchantmentHelper.canStoreEnchantments(primary)) {
            showBookEnchants = selectionIndex == -1;
        }

        if(showBookEnchants || selectionIndex == -1) return;

        if(transferElement == null) transferElement = new TransferElement(primary, secondary, selectionIndex, new TransferHoverManager(), relX, relY);
        else if(bookElement.isInvalid(primary)) bookElement = bookElement.createCopy(primary);

        transferElement.tick(primary);
    }

    @Override
    public void dungeonEnchants$render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int relX = (width - imageWidth) / 2;
        int relY = (height - imageHeight) / 2;
        if(showBookEnchants && bookElement != null)
            bookElement.render(context, font, mouseX, mouseY, relX, relY + 10);

        if(!showBookEnchants && transferElement != null && !hideTransfer)
            transferElement.render(context, font, mouseX, mouseY, relX, relY + 10);

    }

    @Override
    public AbstractEnchantmentUIElement dungeonEnchants$getUIElement() {
        return showBookEnchants ? bookElement : transferElement;
    }

    @Override
    public void dungeonEnchants$mouseClicked(MouseButtonEvent click, boolean doubled) {
        if(showBookEnchants && bookElement instanceof EnchantedBookElement enchantedBookElement) {
            bookElement.updateMousePosition(click.x(), click.y());
            int hover = bookElement.getActiveHoverOption();
            if(hover == -1 || enchantedBookElement.isEnchantmentDisallowed(hover)) return;
            if(Minecraft.getInstance().level != null)
                Minecraft.getInstance().level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundSource.NEUTRAL, 0.8f, 0.9f);
            selectionIndex = hover;
        } else if(!showBookEnchants && !hideTransfer && transferElement instanceof TransferElement localTransferElement) {
            localTransferElement.updateMousePosition(click.x(), click.y());
            int hover = localTransferElement.getActiveHoverOption();
            if(hover == -1 || localTransferElement.getSelectedEnchantments().hasSelection(hover / 3)) return;
            if(Minecraft.getInstance().level != null)
                Minecraft.getInstance().level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundSource.NEUTRAL, 0.8f, 0.9f);
            if(this.minecraft.gameMode == null) return;
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, selectionIndex << 16 | hover);
            hideTransfer = true;
        }
    }
}
