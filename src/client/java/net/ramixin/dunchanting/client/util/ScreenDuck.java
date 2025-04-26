package net.ramixin.dunchanting.client.util;

import net.minecraft.client.gui.DrawContext;

public interface ScreenDuck {

    void dungeonEnchants$tick();

    void dungeonEnchants$render(DrawContext context, int mouseX, int mouseY, float delta);

    void dungeonEnchants$mouseClicked(double mouseX, double mouseY, int button);
}
