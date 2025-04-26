package net.ramixin.dunchanting.client.enchantmentui.anvil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.items.components.EnchantmentOption;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModUtils;

import java.util.List;

public class EnchantedBookElement extends AbstractEnchantmentUIElement {

    private final ItemStack enchantableItem;

    public EnchantedBookElement(ItemStack book, ItemStack enchantableItem, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(book, hoverManager, relX, relY);
        this.enchantableItem = enchantableItem;
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, String enchant) {
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

    public boolean isEnchantmentDisallowed(int hoverIndex) {
        if(!(getHoverManager() instanceof EnchantedBookHoverManager bookHoverManager)) return false;
        return !bookHoverManager.supports(this, enchantableItem, hoverIndex);
    }
}
