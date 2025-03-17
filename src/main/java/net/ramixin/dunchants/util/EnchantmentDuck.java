package net.ramixin.dunchants.util;

import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;
import net.ramixin.dunchants.enchantments.LeveledEnchantmentEffect;

public interface EnchantmentDuck {

    boolean dungeonEnchants$getLeveledEffectResult(ComponentType<LeveledEnchantmentEffect> type, World world, int level);

    static EnchantmentDuck get(Enchantment enchantment) {
        return (EnchantmentDuck) (Object) enchantment;
    }
}
