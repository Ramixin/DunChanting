package net.ramixin.dunchanting.client.util;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;

public interface ScreenDuck {

    void dungeonEnchants$tick();

    void dungeonEnchants$render(DrawContext context, int mouseX, int mouseY, float delta);

    void dungeonEnchants$mouseClicked(Click click, boolean doubled);
}
