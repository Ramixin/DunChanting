package net.ramixin.dunchanting.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;

public class SelectedEnchantmentsUtil {

    static SelectedEnchantments get(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
    }

    static SelectedEnchantments generate(ItemStack stack) {

        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if(enchantments.isEmpty()) return SelectedEnchantments.DEFAULT;

        Integer[] indices = {null, null, null};
        EnchantmentOptions options = stack.getOrDefault(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS, EnchantmentOptions.DEFAULT);
        for(int i = 0; i < 3; i++) {
            if(options.hasEmptySlot(i)) continue;
            EnchantmentSlot slot = options.getOrThrow(i);
            if(slot.isLocked(0)) continue;
            Holder<Enchantment> enchant = slot.getOrThrow(0);
            if(enchantments.getLevel(enchant) > 0) indices[i] = 0;
        }
        return new SelectedEnchantments(indices[0], indices[1], indices[2]);
    }


}
