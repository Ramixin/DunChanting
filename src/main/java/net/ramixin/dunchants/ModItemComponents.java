package net.ramixin.dunchants;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ModItemComponents {

    public static final ComponentType<List<List<String>>> ENCHANTMENT_OPTIONS = register("enchantment_options", (builder) -> builder.codec(Codec.list(Codec.list(Codec.STRING, 1, 3), 3, 3)).packetCodec(PacketCodec.of(
            (value, buf) -> {
                for(List<String> list : value) {
                    buf.writeInt(list.size());
                    list.forEach(buf::writeString);
                }
            },
            buf -> {
                List<List<String>> result = new ArrayList<>();
                for(int i = 0; i < 3; i++) {
                    int size = buf.readInt();
                    List<String> list = new ArrayList<>();
                    for(int l = 0; l < size; l++) list.add(buf.readString());
                    result.add(list);
                }
                return result;
            }
    )));


    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, DungeonEnchants.id(id), builderOperator.apply(ComponentType.builder()).build());
    }

    public static void onInitialize() {}

}
