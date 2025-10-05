package net.ramixin.dunchanting.loot;

import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.ramixin.dunchanting.Dunchanting;

public class ModSubPredicateTypes {

    @SuppressWarnings("unused")
    public static final ComponentSubPredicate.Type<EnchantmentLevelChancePredicate> LEVELED_CHANCE = register("leveled_chance", new ComponentSubPredicate.Type<>(EnchantmentLevelChancePredicate.CODEC));

    private static <T extends ComponentPredicate> ComponentSubPredicate.Type<T> register(String id, ComponentSubPredicate.Type<T> type) {
        return Registry.register(
                Registries.DATA_COMPONENT_PREDICATE_TYPE, Dunchanting.id(id), type);
    }

    public static void onInitialize() {}

}
