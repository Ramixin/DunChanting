package net.ramixin.dunchants.client.enchantmentui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public abstract class AbstractUIHoverManager {

    public abstract void render(AbstractEnchantmentUIElement element, ItemStack stack, DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY);

    public abstract void update(AbstractEnchantmentUIElement element, ItemStack stack, double mouseX, double mouseY, int relX, int relY);

    public abstract Optional<Integer> setPointsToCustomColor();

    public abstract int getActiveHoverOption();

    public abstract void cancelActiveHover();
}
