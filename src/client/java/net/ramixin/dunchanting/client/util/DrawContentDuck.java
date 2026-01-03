package net.ramixin.dunchanting.client.util;

import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public interface DrawContentDuck {

    void dunchanting$enableTooltipBatching();

    void dunchanting$addTooltipToBatch(Consumer<GuiGraphics> tooltip);

    static DrawContentDuck get(GuiGraphics context) {
        return (DrawContentDuck) context;
    }
}
