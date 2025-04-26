package net.ramixin.dunchanting.enchantments;

import com.mojang.serialization.Codec;

public enum LeveledEnchantmentEffect {

    INSTANCE
    ;

    public static final Codec<LeveledEnchantmentEffect> CODEC = Codec.unit(INSTANCE);

}
