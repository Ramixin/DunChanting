package net.ramixin.dunchants.loot;

import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.ramixin.dunchants.DungeonEnchants;

public class ModSubPredicateTypes {

    public static final ComponentSubPredicate.Type<EnchantmentLevelChancePredicate> LEVELED_CHANCE = register("leveled_chance", new ComponentSubPredicate.Type<>(EnchantmentLevelChancePredicate.CODEC));

    private static <T extends ItemSubPredicate> ComponentSubPredicate.Type<T> register(String id, ComponentSubPredicate.Type<T> type) {
        return Registry.register(Registries.ITEM_SUB_PREDICATE_TYPE, DungeonEnchants.id(id), type);
    }

    public static void onInitialize() {}

}
