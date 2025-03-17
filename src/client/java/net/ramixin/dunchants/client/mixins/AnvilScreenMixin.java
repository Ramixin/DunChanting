package net.ramixin.dunchants.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.client.enchantmentui.anvil.EnchantedBookElement;
import net.ramixin.dunchants.client.enchantmentui.anvil.EnchantedBookHoverManager;
import net.ramixin.dunchants.client.enchantmentui.anvil.TransferElement;
import net.ramixin.dunchants.client.enchantmentui.anvil.TransferHoverManager;
import net.ramixin.dunchants.client.util.EnchantmentUIHolder;
import net.ramixin.dunchants.client.util.ModTextures;
import net.ramixin.dunchants.client.util.ScreenDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ForgingScreen<AnvilScreenHandler> implements ScreenDuck, EnchantmentUIHolder {

    @Shadow private TextFieldWidget nameField;

    @Shadow @Final private PlayerEntity player;
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

    @Unique boolean showTransfer = false;

    public AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture) {
        super(handler, playerInventory, title, texture);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void changeBackgroundHeight(AnvilScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.backgroundHeight = 184;
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void setNameFieldAsDisabledOnScreenOpen(CallbackInfo ci) {
        this.nameField.visible = false;
        this.nameField.active = false;
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    protected void cancelSuperForegroundRender(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onSlotUpdate", at = @At("HEAD"))
    private void hideTextFieldUnlessItemInFirstAndNotSecondSlot(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        boolean val = !handler.getSlot(1).getStack().isOf(Items.ENCHANTED_BOOK);
        this.nameField.visible = val;
        this.nameField.active = val;
    }

    @WrapOperation(method = "setup", at = @At(value = "NEW", target = "(Lnet/minecraft/client/font/TextRenderer;IIIILnet/minecraft/text/Text;)Lnet/minecraft/client/gui/widget/TextFieldWidget;"))
    private TextFieldWidget moveTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text, Operation<TextFieldWidget> original) {
        return original.call(textRenderer, x - 26, y + 30, width, height, text);
    }

    @WrapOperation(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"))
    private void moveTextFieldTextures(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        if(this.nameField.visible) {
            instance.drawGuiTexture(renderLayers, ModTextures.anvilTextFieldBackdrop, x - 32, y + 24, 122, 28);
            original.call(instance, renderLayers, sprite, x - 26, y + 30, width, height);
        }
    }

    @WrapOperation(method = "drawInvalidRecipeArrow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"))
    private void moveInvalidRecipeArrow(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        original.call(instance, renderLayers, sprite, x - 9, y - 41, width - 3, height - 1);
    }

    @Override
    public void dungeonEnchants$tick() {
        showBookEnchants = false;
        ItemStack primary = handler.getSlot(0).getStack();
        ItemStack secondary = handler.getSlot(1).getStack();
        if(!secondary.isOf(Items.ENCHANTED_BOOK) || primary.isOf(Items.ENCHANTED_BOOK)) {
            selectionIndex = -1;
            bookElement = null;
            transferElement = null;
            hideTransfer = false;
            return;
        }
        int relX = (width - backgroundWidth) / 2;
        int relY = (height - backgroundHeight) / 2;

        if(bookElement instanceof EnchantedBookElement enchantedBookElement)
            if(enchantedBookElement.getEnchantableStack() != primary)
                bookElement = null;

        if(bookElement == null) bookElement = new EnchantedBookElement(secondary, primary, new EnchantedBookHoverManager(), relX, relY);
        else if(bookElement.isInvalid(secondary)) bookElement = bookElement.createCopy(secondary);

        bookElement.tick(secondary);

        if(EnchantmentHelper.canHaveEnchantments(primary)) {
            showBookEnchants = selectionIndex == -1;
        }

        if(showBookEnchants || selectionIndex == -1) return;

        if(transferElement == null) transferElement = new TransferElement(primary, secondary, selectionIndex, new TransferHoverManager(), relX, relY);
        else if(bookElement.isInvalid(primary)) bookElement = bookElement.createCopy(primary);

        transferElement.tick(primary);
    }

    @Override
    public void dungeonEnchants$render(DrawContext context, int mouseX, int mouseY, float delta) {
        int relX = (width - backgroundWidth) / 2;
        int relY = (height - backgroundHeight) / 2;
        if(showBookEnchants && bookElement != null)
            bookElement.render(context, textRenderer, mouseX, mouseY, relX, relY + 10);

        if(!showBookEnchants && transferElement != null && !hideTransfer)
            transferElement.render(context, textRenderer, mouseX, mouseY, relX, relY + 10);

    }

    @Override
    public AbstractEnchantmentUIElement dungeonEnchants$getUIElement() {
        return showBookEnchants ? bookElement : transferElement;
    }

    @Override
    public void dungeonEnchants$mouseClicked(double mouseX, double mouseY, int button) {
        if(showBookEnchants && bookElement instanceof EnchantedBookElement enchantedBookElement) {
            int hover = bookElement.getActiveHoverOption();
            if(hover == -1 || !enchantedBookElement.supportsEnchantment(hover)) return;
            if(MinecraftClient.getInstance().world != null)
                MinecraftClient.getInstance().world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.NEUTRAL, 0.8f, 0.9f);
            selectionIndex = hover;
        } else if(!showBookEnchants && !hideTransfer && transferElement instanceof TransferElement localTransferElement) {
            int hover = localTransferElement.getActiveHoverOption();
            if(hover == -1 || localTransferElement.getSelectedEnchantments().hasSelection(hover / 3)) return;
            if(MinecraftClient.getInstance().world != null)
                MinecraftClient.getInstance().world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.NEUTRAL, 0.8f, 0.9f);
            if(this.client == null || this.client.interactionManager == null) return;
            this.client.interactionManager.clickButton(this.handler.syncId, selectionIndex << 16 | hover);
            hideTransfer = true;
        }
    }
}
