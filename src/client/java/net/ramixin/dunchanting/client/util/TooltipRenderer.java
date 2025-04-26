package net.ramixin.dunchanting.client.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class TooltipRenderer {

    private final DrawContext context;
    private final TextRenderer textRenderer;
    private final int mouseX;
    private final int mouseY;
    private final MutableInt height = new MutableInt();

    public TooltipRenderer(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        this.context = context;
        this.textRenderer = textRenderer;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void render(List<Text> text, int xOffset, int yOffset) {
        context.drawTooltip(textRenderer, text, mouseX + xOffset, mouseY + yOffset + height.getValue());
        height.add(yOffset);
        if(text.isEmpty()) return;
        if(text.size() == 1) height.add(16);
        else height.add(8 + text.size() * 10);
    }

    public void resetHeight() {
        height.setValue(0);
    }

    public int getTextWidth(String text) {
        return textRenderer.getWidth(text);
    }

    public int getTextWidth(Text text) {
        return textRenderer.getWidth(text);
    }

}
