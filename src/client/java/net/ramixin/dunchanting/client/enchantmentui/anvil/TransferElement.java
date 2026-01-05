package net.ramixin.dunchanting.client.enchantmentui.anvil;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.util.ModUtil;

import java.util.List;

public class TransferElement extends AbstractEnchantmentUIElement {

    private final Holder<Enchantment> transferSelection;
    private final int transferIndex;
    private final ItemStack bookStack;

    public TransferElement(ItemStack stack, ItemStack book, int transferSelection, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(stack, hoverManager, relX, relY);
        this.transferIndex = transferSelection;
        List<Holder<Enchantment>> orderedEnchantments = ModUtil.getStoredEnchants(book);
        this.transferSelection = orderedEnchantments.get(transferSelection / 3);
        this.bookStack = book;
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, Holder<Enchantment> enchant) {
        return getSelectedEnchantments().hasSelection(hoverIndex / 3);
    }

    @Override
    public AbstractEnchantmentUIElement createCopy(ItemStack stack) {
        return new TransferElement(stack, bookStack, transferIndex, getHoverManager(), getCachedRelatives()[0], getCachedRelatives()[1]);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    protected boolean updatesComponents() {
        return true;
    }

    public Holder<Enchantment> getTransferSelection() {
        return transferSelection;
    }
}
