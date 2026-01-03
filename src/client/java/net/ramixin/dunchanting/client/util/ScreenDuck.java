package net.ramixin.dunchanting.client.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

public interface ScreenDuck {

    void dungeonEnchants$tick();

    void dungeonEnchants$render(GuiGraphics context, int mouseX, int mouseY, float delta);

    void dungeonEnchants$mouseClicked(MouseButtonEvent click, boolean doubled);
}
