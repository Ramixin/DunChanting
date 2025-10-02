package net.ramixin.dunchanting.client.enchantmentui.anvil;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.util.ModUtils;

import java.util.List;

public class TransferElement extends AbstractEnchantmentUIElement {

    private final RegistryEntry<Enchantment> transferSelection;
    private final int transferIndex;
    private final ItemStack bookStack;

    public TransferElement(ItemStack stack, ItemStack book, int transferSelection, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(stack, hoverManager, relX, relY);
        this.transferIndex = transferSelection;
        List<RegistryEntry<Enchantment>> orderedEnchantments = ModUtils.getOrderedEnchantments(book);
        this.transferSelection = orderedEnchantments.get(transferSelection / 3);
        this.bookStack = book;
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, RegistryEntry<Enchantment> enchant) {
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

    public RegistryEntry<Enchantment> getTransferSelection() {
        return transferSelection;
    }
}
