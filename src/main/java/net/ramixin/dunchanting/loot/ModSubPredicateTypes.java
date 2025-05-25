package net.ramixin.dunchanting.loot;

//? <1.21.5 {
/*import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.predicate.item.ItemSubPredicate;
*///?} else {
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.predicate.component.ComponentPredicate;
//?}
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.ramixin.dunchanting.Dunchanting;

public class ModSubPredicateTypes {

    public static final ComponentSubPredicate.Type<EnchantmentLevelChancePredicate> LEVELED_CHANCE = register("leveled_chance", new ComponentSubPredicate.Type<>(EnchantmentLevelChancePredicate.CODEC));

    private static <T extends /*? >=1.21.5 {*/ComponentPredicate /*?} else {*/ /*ItemSubPredicate*//*?}*/> ComponentSubPredicate.Type<T> register(String id, ComponentSubPredicate.Type<T> type) {
        return Registry.register(
                /*? >=1.21.5 {*/
                Registries.DATA_COMPONENT_PREDICATE_TYPE
                /*?} else {*/
                /*Registries.ITEM_SUB_PREDICATE_TYPE
                *//*?}*/
                , Dunchanting.id(id), type);
    }

    public static void onInitialize() {}

}
