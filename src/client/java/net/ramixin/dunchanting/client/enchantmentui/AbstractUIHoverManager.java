package net.ramixin.dunchanting.client.enchantmentui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public abstract class AbstractUIHoverManager {

    public abstract void render(AbstractEnchantmentUIElement element, ItemStack stack, GuiGraphics context, Font textRenderer, int mouseX, int mouseY, int relX, int relY);

    public abstract void update(AbstractEnchantmentUIElement element, ItemStack stack, double mouseX, double mouseY, int relX, int relY);

    public abstract Optional<Integer> setPointsToCustomColor();

    public abstract int getActiveHoverOption();

    public abstract void cancelActiveHover();
}
