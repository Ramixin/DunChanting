package net.ramixin.dunchants.client.enchantmentui.anvil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.dunchants.util.ModUtils;

import java.util.List;

public class EnchantedBookElement extends AbstractEnchantmentUIElement {

    private final ItemStack enchantableItem;

    public EnchantedBookElement(ItemStack book, ItemStack enchantableItem, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(book, hoverManager, relX, relY);
        this.enchantableItem = enchantableItem;
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, String enchant) {
        return !supportsEnchantment(hoverIndex);
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
    protected EnchantmentOptions generateOptions(ItemStack stack) {
        if(MinecraftClient.getInstance().world == null) throw new IllegalStateException("world is null");
        List<RegistryEntry<Enchantment>> orderedEnchants = ModUtils.getOrderedEnchantments(stack, MinecraftClient.getInstance().world.getRegistryManager());
        EnchantmentOption[] options = new EnchantmentOption[3];
        for(int i = 0; i < Math.min(orderedEnchants.size(), 3); i++) {
            String enchant = orderedEnchants.get(i).getIdAsString();
            options[i] = new EnchantmentOption(enchant, enchant, null);
        }
        return new EnchantmentOptions(options[0], options[1], options[2]);
    }

    @Override
    protected SelectedEnchantments generateSelection(ItemStack stack) {
        EnchantmentOptions options = getEnchantmentOptions();
        SelectedEnchantments selected = SelectedEnchantments.DEFAULT;
        for(int i = 0; i < 3; i++) {
            if(options.isLocked(i)) break;
            selected = selected.enchantOption(i, 0);
        }
        return selected;
    }

    @Override
    protected boolean updatesComponents() {
        return false;
    }

    @Override
    protected boolean canRender() {
        return getEnchantmentOptions() != null && getStack().isOf(Items.ENCHANTED_BOOK);
    }

    public ItemStack getEnchantableStack() {
        return enchantableItem;
    }

    public boolean supportsEnchantment(int hoverIndex) {
        if(!(getHoverManager() instanceof EnchantedBookHoverManager bookHoverManager)) return true;
        return bookHoverManager.supports(this, enchantableItem, hoverIndex);
    }
}
