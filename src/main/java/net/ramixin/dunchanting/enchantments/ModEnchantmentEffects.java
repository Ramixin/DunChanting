package net.ramixin.dunchanting.enchantments;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.ramixin.dunchanting.Dunchanting;

import java.util.function.UnaryOperator;

public class ModEnchantmentEffects {

    public static final DataComponentType<LeveledEnchantmentEffect> LEVELED_PREVENT_EQUIPMENT_DROP = register("leveled_prevent_equipment_drop", (builder) -> builder.persistent(LeveledEnchantmentEffect.CODEC));
    public static final DataComponentType<LeveledEnchantmentEffect> LEVELED_PREVENT_ARMOR_CHANGE = register("leveled_prevent_armor_change", (builder) -> builder.persistent(LeveledEnchantmentEffect.CODEC));

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Dunchanting.id(id), builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void onInitialize() {

    }

}
