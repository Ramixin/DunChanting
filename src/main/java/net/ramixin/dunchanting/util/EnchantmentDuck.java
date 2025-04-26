package net.ramixin.dunchanting.util;

import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;
import net.ramixin.dunchanting.enchantments.LeveledEnchantmentEffect;

public interface EnchantmentDuck {

    boolean dungeonEnchants$getLeveledEffectResult(ComponentType<LeveledEnchantmentEffect> type, World world, int level);

    static EnchantmentDuck get(Enchantment enchantment) {
        return (EnchantmentDuck) (Object) enchantment;
    }
}
