package net.ramixin.dunchanting.client.enchantmentui.etable;

import net.minecraft.item.ItemStack;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.client.util.ModClientUtils;

public class EnchantmentTableElement extends AbstractEnchantmentUIElement {


    public EnchantmentTableElement(ItemStack stack, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(stack, hoverManager, relX, relY);
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, String enchant) {
        return ModClientUtils.markAsUnavailable(this, hoverIndex, enchant);
    }

    @Override
    public AbstractEnchantmentUIElement createCopy(ItemStack stack) {
        return new EnchantmentTableElement(stack, getHoverManager(), getCachedRelatives()[0], getCachedRelatives()[1]);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    protected boolean updatesComponents() {
        return true;
    }
}
