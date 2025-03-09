package net.ramixin.dunchants.items;

import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Unit;
import net.ramixin.dunchants.DungeonEnchants;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;

import java.util.function.UnaryOperator;

public class ModItemComponents {

    public static final ComponentType<EnchantmentOptions> ENCHANTMENT_OPTIONS = register("enchantment_options", (builder) -> builder.codec(EnchantmentOptions.CODEC).packetCodec(EnchantmentOptions.PACKET_CODEC));
    public static final ComponentType<SelectedEnchantments> SELECTED_ENCHANTMENTS = register("selected_enchantments", (builder) -> builder.codec(SelectedEnchantments.CODEC).packetCodec(SelectedEnchantments.PACKET_CODEC));
    public static final ComponentType<Unit> BOUND = register("bound", (builder) -> builder.codec(Unit.CODEC).packetCodec(PacketCodec.unit(Unit.INSTANCE)));


    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        //return builderOperator.apply(ComponentType.builder()).build();
        return Registry.register(Registries.DATA_COMPONENT_TYPE, DungeonEnchants.id(id), builderOperator.apply(ComponentType.builder()).build());
    }

    public static void onInitialize() {}

}
