package net.ramixin.dunchanting.client.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
//? >=1.21.2
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.client.DunchantingClient;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.grindstone.GrindstoneElement;
import net.ramixin.dunchanting.client.enchantmentui.grindstone.GrindstoneHoverManager;
import net.ramixin.dunchanting.client.util.EnchantmentUIHolder;
import net.ramixin.dunchanting.handlers.ModGrindstoneScreenHandler;
import net.ramixin.dunchanting.util.ModUtils;

import java.util.Optional;
import java.util.UUID;

public class ModGrindstoneScreen extends HandledScreen<ModGrindstoneScreenHandler> implements EnchantmentUIHolder {

    public static final Identifier BACKGROUND = Dunchanting.id("textures/gui/container/grindstone.png");

    private AbstractEnchantmentUIElement element;

    private final UUID playerUUID;

    public ModGrindstoneScreen(ModGrindstoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight = 184;
        this.playerUUID = inventory.player.getUuid();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int relX = (width - backgroundWidth) / 2;
        int relY = (height - backgroundHeight) / 2;

        Optional<Integer> pointColor;
        if(element == null) pointColor = Optional.empty();
        else {
            element.render(context, textRenderer, mouseX, mouseY, relX, relY + 8);
            pointColor = element.getPointColor();
        }

        int points = DunchantingClient.getPlayerEntityDuck().dungeonEnchants$getEnchantmentPoints();
        String text = String.valueOf(points);
        int width = textRenderer.getWidth(text);
        context.drawText(textRenderer, Text.of(text), relX + 60 - width / 2, relY + 10, 0xFF9c50af, false);

        int color = pointColor.orElse(0xFF9c50af);
        String secondText;
        if(pointColor.isEmpty()) {
            secondText = text;
        } else {
            secondText = String.valueOf(points + ModUtils.getAttributionOnItem(playerUUID, handler.getSlot(0).getStack(), element.getActiveHoverOption() / 3));
        }
        int secondWith = textRenderer.getWidth(secondText);
        context.drawText(textRenderer, Text.of(secondText), relX + 117 - secondWith / 2, relY + 10, color, false);

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Prevent title rendering
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(
                /*? if >=1.21.2 >>*/ RenderLayer::getGuiTextured,
                BACKGROUND, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        ItemStack stack = handler.getSlot(0).getStack();
        int relX = (width - backgroundWidth) / 2;
        int relY = (height - backgroundHeight) / 2;
        if(element == null) element = new GrindstoneElement(stack, new GrindstoneHoverManager(playerUUID), relX, relY);
        else if(element.isInvalid(stack)) element = element.createCopy(stack);

        element.tick(stack);
    }

    @Override
    public AbstractEnchantmentUIElement dungeonEnchants$getUIElement() {
        return element;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int activeHoverOption = element.getActiveHoverOption();
        if(this.client == null || this.client.interactionManager == null) return false;
        if(activeHoverOption != -1)  this.client.interactionManager.clickButton(this.handler.syncId, activeHoverOption / 3);
        if(element != null) element.getHoverManager().cancelActiveHover();
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
