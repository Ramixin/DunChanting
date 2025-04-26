package net.ramixin.dunchanting.enchantments;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.ramixin.dunchanting.Dunchanting;

import java.util.function.UnaryOperator;

public class ModEnchantmentEffects {

    public static final ComponentType<LeveledEnchantmentEffect> LEVELED_PREVENT_EQUIPMENT_DROP = register("leveled_prevent_equipment_drop", (builder) -> builder.codec(LeveledEnchantmentEffect.CODEC));
    public static final ComponentType<LeveledEnchantmentEffect> LEVELED_PREVENT_ARMOR_CHANGE = register("leveled_prevent_armor_change", (builder) -> builder.codec(LeveledEnchantmentEffect.CODEC));

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Dunchanting.id(id), builderOperator.apply(ComponentType.builder()).build());
    }

    public static void onInitialize() {

    }

}
