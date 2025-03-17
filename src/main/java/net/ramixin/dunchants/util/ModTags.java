package net.ramixin.dunchants.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.ramixin.dunchants.DungeonEnchants;

public interface ModTags {

    TagKey<Enchantment> POWERFUL_ENCHANTMENT = of("powerful_enchantment");

    private static TagKey<Enchantment> of(String id) {
        return TagKey.of(RegistryKeys.ENCHANTMENT, DungeonEnchants.id(id));
    }

}
