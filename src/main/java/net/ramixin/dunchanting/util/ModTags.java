package net.ramixin.dunchanting.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.ramixin.dunchanting.Dunchanting;

public interface ModTags {

    TagKey<Enchantment> POWERFUL_ENCHANTMENT = of("powerful_enchantment");

    private static TagKey<Enchantment> of(String id) {
        return TagKey.create(Registries.ENCHANTMENT, Dunchanting.id(id));
    }

}
