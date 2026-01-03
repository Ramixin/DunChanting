package net.ramixin.dunchanting.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public interface ItemEnchantmentsDuck {

    Object2IntOpenHashMap<Holder<Enchantment>> dungeonEnchants$getEnchantments();

    static ItemEnchantmentsDuck get(ItemEnchantments component) {
        return (ItemEnchantmentsDuck) component;
    }
}
