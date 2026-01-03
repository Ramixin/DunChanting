package net.ramixin.dunchanting.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.function.Consumer;

public class TooltipRenderer {

    private final Consumer<Consumer<GuiGraphics>> tooltipBatcher;
    private final Font textRenderer;
    private final int mouseX;
    private final int mouseY;
    private final MutableInt height = new MutableInt();

    public TooltipRenderer(GuiGraphics context, Font textRenderer, int mouseX, int mouseY) {
        DrawContentDuck duck = DrawContentDuck.get(context);
        duck.dunchanting$enableTooltipBatching();
        this.tooltipBatcher = duck::dunchanting$addTooltipToBatch;
        this.textRenderer = textRenderer;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void render(List<Component> text, int xOffset, int yOffset) {
        tooltipBatcher.accept(context -> {
            context.renderTooltip(
                    textRenderer,
                    text.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
                    mouseX + xOffset,
                    mouseY + yOffset + (int) height.get(),
                    DefaultTooltipPositioner.INSTANCE,
                    null);
            height.add(yOffset);
            if(text.isEmpty()) return;
            if(text.size() == 1) height.add(16);
            else height.add(8 + text.size() * 10);
        });

    }

    public void resetHeight() {
        tooltipBatcher.accept(context -> height.setValue(0));
    }

    public int getTextWidth(String text) {
        return textRenderer.width(text);
    }

    public int getTextWidth(Component text) {
        return textRenderer.width(text);
    }

}
