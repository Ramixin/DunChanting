package net.ramixin.dunchanting.util;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.ramixin.dunchanting.enchantments.LeveledEnchantmentEffect;

public interface EnchantmentDuck {

    boolean dungeonEnchants$getLeveledEffectResult(DataComponentType<LeveledEnchantmentEffect> type, Level world, int level);

    static EnchantmentDuck get(Enchantment enchantment) {
        return (EnchantmentDuck) (Object) enchantment;
    }
}
