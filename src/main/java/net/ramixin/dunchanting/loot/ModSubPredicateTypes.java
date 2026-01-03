package net.ramixin.dunchanting.loot;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.ramixin.dunchanting.Dunchanting;

public class ModSubPredicateTypes {

    @SuppressWarnings("unused")
    public static final SingleComponentItemPredicate.Type<EnchantmentLevelChancePredicate> LEVELED_CHANCE = register("leveled_chance", EnchantmentLevelChancePredicate.CODEC);

    private static <T extends DataComponentPredicate> SingleComponentItemPredicate.Type<T> register(String id, Codec<T> codec) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, Dunchanting.id(id), new DataComponentPredicate.ConcreteType<>(codec));
    }

    public static void onInitialize() {}

}
