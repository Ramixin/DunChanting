package net.ramixin.dunchanting.client.enchantmentui.grindstone;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;

public class GrindstoneElement extends AbstractEnchantmentUIElement {

    public GrindstoneElement(ItemStack stack, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(stack, hoverManager, relX, relY);
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, Holder<Enchantment> enchant) {
        return !getSelectedEnchantments().hasSelection(hoverIndex / 3);
    }

    @Override
    public AbstractEnchantmentUIElement createCopy(ItemStack stack) {
        return new GrindstoneElement(stack, getHoverManager(), getCachedRelatives()[0], getCachedRelatives()[1]);
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    protected boolean updatesComponents() {
        return true;
    }
}
