package net.ramixin.dunchanting.util;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public interface ModMath {

    static int textWrapScore(int fixed, int length, int tolerance) {
        return Math.abs(fixed + length - tolerance);
    }

    static float OddsOfThirdEnchant(int level, int optionIndex) {
        int index = optionIndex * 5;
        if(level > 10 + index) return 1;
        return (float) Math.sin(((level - index) * Math.PI) / 20d);
    }

    static float OddsOfAnotherSlot(int level) {
        return (float) ((Math.cos(Math.PI * (level % 5) / 5) - 1) / -2);
    }

    static int totalEnchantmentWeights(List<Holder<Enchantment>> enchants) {
        int tot = 0;
        for(Holder<Enchantment> holder : enchants) {
            tot += holder.value().getWeight();
        }
        return tot;
    }

}
