package net.ramixin.dunchanting.enchantments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public enum LeveledEnchantmentEffect {

    INSTANCE
    ;

    public static final Codec<LeveledEnchantmentEffect> CODEC = MapCodec.unitCodec(INSTANCE);

}
