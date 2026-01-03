package net.ramixin.dunchanting.items.components;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.ramixin.dunchanting.Dunchanting;

import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final DataComponentType<EnchantmentOptions> UNLOCKED_ENCHANTMENT_OPTIONS = register("enchantment_options", (builder) -> builder.persistent(EnchantmentOptions.CODEC).networkSynchronized(EnchantmentOptions.PACKET_CODEC));
    public static final DataComponentType<EnchantmentOptions> LOCKED_ENCHANTMENT_OPTIONS = register("locked_enchantment_options", (builder) -> builder.persistent(EnchantmentOptions.CODEC).networkSynchronized(EnchantmentOptions.PACKET_CODEC));
    public static final DataComponentType<SelectedEnchantments> SELECTED_ENCHANTMENTS = register("selected_enchantments", (builder) -> builder.persistent(SelectedEnchantments.CODEC).networkSynchronized(SelectedEnchantments.PACKET_CODEC));
    public static final DataComponentType<Unit> BOUND = register("bound", (builder) -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final DataComponentType<Attributions> ATTRIBUTIONS = register("attributions", (builder) -> builder.persistent(Attributions.CODEC).networkSynchronized(Attributions.PACKET_CODEC));
    public static final DataComponentType<Gilded> GILDED = register("gilded", (builder) -> builder.persistent(Gilded.CODEC).networkSynchronized(Gilded.PACKET_CODEC));


    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        //return builderOperator.apply(ComponentType.builder()).build();
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Dunchanting.id(id), builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void onInitialize() {}

}
