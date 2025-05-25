package net.ramixin.dunchanting.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;

public interface ItemEnchantmentsComponentDuck {

    Object2IntOpenHashMap<RegistryEntry<Enchantment>> dungeonEnchants$getEnchantments();

    static ItemEnchantmentsComponentDuck get(ItemEnchantmentsComponent component) {
        return (ItemEnchantmentsComponentDuck) component;
    }
}
