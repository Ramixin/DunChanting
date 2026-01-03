package net.ramixin.dunchanting.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.client.DunchantingClient;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.grindstone.GrindstoneElement;
import net.ramixin.dunchanting.client.enchantmentui.grindstone.GrindstoneHoverManager;
import net.ramixin.dunchanting.client.util.EnchantmentUIHolder;
import net.ramixin.dunchanting.menus.ModGrindstoneMenu;
import net.ramixin.dunchanting.util.ModUtil;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public class ModGrindstoneScreen extends AbstractContainerScreen<ModGrindstoneMenu> implements EnchantmentUIHolder {

    public static final Identifier BACKGROUND = Dunchanting.id("textures/gui/container/grindstone.png");

    private AbstractEnchantmentUIElement element;

    private final UUID playerUUID;

    public ModGrindstoneScreen(ModGrindstoneMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        imageHeight = 184;
        this.playerUUID = inventory.player.getUUID();
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int relX = (width - imageWidth) / 2;
        int relY = (height - imageHeight) / 2;

        ItemStack stack = menu.getSlot(0).getItem();
        Optional<Integer> pointColor;
        if(element == null || stack.isEmpty()) pointColor = Optional.empty();
        else {
            element.render(context, font, mouseX, mouseY, relX, relY + 8);
            pointColor = element.getPointColor();
        }

        int points = DunchantingClient.getPlayerEntityDuck().dungeonEnchants$getEnchantmentPoints();
        String text = String.valueOf(points);
        int width = font.width(text);
        context.drawString(font, Component.nullToEmpty(text), relX + 60 - width / 2, relY + 10, 0xFF9c50af, false);

        int color = pointColor.orElse(0xFF9c50af);
        String secondText;
        if(pointColor.isEmpty()) {
            secondText = text;
        } else {
            secondText = String.valueOf(points + ModUtil.getAttributionOnItem(playerUUID, menu.getSlot(0).getItem(), element.getActiveHoverOption() / 3));
        }
        int secondWith = font.width(secondText);
        context.drawString(font, Component.nullToEmpty(secondText), relX + 117 - secondWith / 2, relY + 10, color, false);

        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NonNull GuiGraphics context, int mouseX, int mouseY) {
        // Prevent title rendering
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack stack = menu.getSlot(0).getItem();
        int relX = (width - imageWidth) / 2;
        int relY = (height - imageHeight) / 2;
        if(element == null) element = new GrindstoneElement(stack, new GrindstoneHoverManager(playerUUID), relX, relY);
        else if(element.isInvalid(stack)) element = element.createCopy(stack);

        element.tick(stack);
    }

    @Override
    public AbstractEnchantmentUIElement dungeonEnchants$getUIElement() {
        return element;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        element.updateMousePosition(click.x(), click.y());
        int activeHoverOption = element.getActiveHoverOption();
        if(this.minecraft.gameMode == null) return false;
        if(activeHoverOption != -1)  this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, activeHoverOption / 3);
        if(element != null) element.getHoverManager().cancelActiveHover();
        return super.mouseClicked(click, doubled);
    }
}
