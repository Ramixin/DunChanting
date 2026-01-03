package net.ramixin.dunchanting.client.enchantmentui.anvil;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModUtil;

import java.util.List;

public class EnchantedBookElement extends AbstractEnchantmentUIElement {

    private final ItemStack enchantableItem;

    public EnchantedBookElement(ItemStack book, ItemStack enchantableItem, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(book, hoverManager, relX, relY);
        this.enchantableItem = enchantableItem;
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, Holder<Enchantment> enchant) {
        return isEnchantmentDisallowed(hoverIndex);
    }

    @Override
    public AbstractEnchantmentUIElement createCopy(ItemStack stack) {
        return new EnchantedBookElement(stack, enchantableItem, getHoverManager(), getCachedRelatives()[0], getCachedRelatives()[1]);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    protected EnchantmentOptions retrieveOptions(ItemStack stack) {
        List<Holder<Enchantment>> orderedEnchants = ModUtil.getOrderedEnchantments(stack);
        EnchantmentSlot[] options = new EnchantmentSlot[3];
        for(int i = 0; i < Math.min(orderedEnchants.size(), 3); i++) {
            Holder<Enchantment> enchant = orderedEnchants.get(i);
            options[i] = new EnchantmentSlot(enchant, enchant, null);
        }
        return new EnchantmentOptions(options[0], options[1], options[2]);
    }

    @Override
    protected SelectedEnchantments retrieveSelection(ItemStack stack) {
        EnchantmentOptions options = getEnchantmentOptions();
        SelectedEnchantments selected = SelectedEnchantments.DEFAULT;
        for(int i = 0; i < 3; i++) {
            if(options.hasEmptySlot(i)) break;
            selected = selected.with(i, 0);
        }
        return selected;
    }

    @Override
    protected boolean updatesComponents() {
        return false;
    }

    @Override
    protected boolean canRender() {
        return getEnchantmentOptions() != null && getStack().is(Items.ENCHANTED_BOOK);
    }

    public ItemStack getEnchantableStack() {
        return enchantableItem;
    }

    public boolean isEnchantmentDisallowed(int hoverIndex) {
        if(!(getHoverManager() instanceof EnchantedBookHoverManager)) return false;
        return !supports(this, enchantableItem, hoverIndex);
    }

    public boolean supports(AbstractEnchantmentUIElement element, ItemStack primary, int hoverIndex) {
        if(hoverIndex == -1) return false;
        int optionIndex = hoverIndex % 3;
        int index = hoverIndex / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.hasEmptySlot(index)) return false;
        EnchantmentSlot option = options.getOrThrow(index);
        if(option.isLocked(optionIndex)) return false;
        Holder<Enchantment> enchant = option.getOrThrow(optionIndex);
        return primary.canBeEnchantedWith(enchant, EnchantingContext.ACCEPTABLE);
    }
}
