package net.ramixin.dunchanting.client.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.function.Consumer;

public class TooltipRenderer {

    private final Consumer<Consumer<DrawContext>> tooltipBatcher;
    private final TextRenderer textRenderer;
    private final int mouseX;
    private final int mouseY;
    private final MutableInt height = new MutableInt();

    public TooltipRenderer(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        DrawContentDuck duck = DrawContentDuck.get(context);
        duck.dunchanting$enableTooltipBatching();
        this.tooltipBatcher = duck::dunchanting$addTooltipToBatch;
        this.textRenderer = textRenderer;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void render(List<Text> text, int xOffset, int yOffset) {
        tooltipBatcher.accept(context -> {
            context.drawTooltipImmediately(
                    textRenderer,
                    text.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList(),
                    mouseX + xOffset,
                    mouseY + yOffset + height.getValue(),
                    HoveredTooltipPositioner.INSTANCE,
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
        return textRenderer.getWidth(text);
    }

    public int getTextWidth(Text text) {
        return textRenderer.getWidth(text);
    }

}
