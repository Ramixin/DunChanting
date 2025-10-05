package net.ramixin.dunchanting.client.util;

import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

public interface DrawContentDuck {

    void dunchanting$enableTooltipBatching();

    void dunchanting$addTooltipToBatch(Consumer<DrawContext> tooltip);

    static DrawContentDuck get(DrawContext context) {
        return (DrawContentDuck) context;
    }
}
