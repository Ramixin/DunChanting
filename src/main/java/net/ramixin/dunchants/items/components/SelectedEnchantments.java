package net.ramixin.dunchants.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.ramixin.dunchants.util.ModUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record SelectedEnchantments(Optional<Integer> first, Optional<Integer> second, Optional<Integer> third) {

    public static final Codec<SelectedEnchantments> CODEC = Codec.intRange(-1,2).listOf().xmap(
            list -> ModUtils.decodeGenericTripleOptionalList(list.iterator(), SelectedEnchantments::new, integer -> integer == -1),
            option -> ModUtils.encodeGenericTripleOptionalList(option::getOptional, -1)
    );

    public static final PacketCodec<RegistryByteBuf, SelectedEnchantments> PACKET_CODEC = PacketCodec.of(SelectedEnchantments::encode, SelectedEnchantments::decode);

    public static final SelectedEnchantments DEFAULT = new SelectedEnchantments(Optional.empty(), Optional.empty(), Optional.empty());

    public SelectedEnchantments(@Nullable Integer first, @Nullable Integer second, @Nullable Integer third) {
        this(Optional.ofNullable(first), Optional.ofNullable(second), Optional.ofNullable(third));
    }

    private Optional<Integer> getOptional(int index) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> first;
            case 1 -> second;
            default -> third;
        };
    }

    public boolean hasSelection(int index) {
        return getOptional(index).isPresent();
    }

    public int get(int index) {
        return getOptional(index).orElseThrow();
    }

    public SelectedEnchantments enchantOption(int index, int enchantmentIndex) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> new SelectedEnchantments(Optional.of(enchantmentIndex), second, third);
            case 1 -> new SelectedEnchantments(first, Optional.of(enchantmentIndex), third);
            default -> new SelectedEnchantments(first, second, Optional.of(enchantmentIndex));
        };
    }

    public SelectedEnchantments disenchantOption(int index) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> new SelectedEnchantments(Optional.empty(), second, third);
            case 1 -> new SelectedEnchantments(first, Optional.empty(), third);
            default -> new SelectedEnchantments(first, second, Optional.empty());
        };
    }

    private static void encode(SelectedEnchantments value, RegistryByteBuf buf) {
        for(int i = 0; i < 3; i++) {
            Optional<Integer> optional = value.getOptional(i);
            buf.writeBoolean(optional.isPresent());
            optional.ifPresent(buf::writeInt);
        }
    }

    private static SelectedEnchantments decode(RegistryByteBuf buf) {
        Integer[] enchants = new Integer[3];
        for(int i = 0; i < 3; i++) {
            if(!buf.readBoolean()) continue;
            enchants[i] = buf.readInt();
        }
        return new SelectedEnchantments(Optional.ofNullable(enchants[0]), Optional.ofNullable(enchants[1]), Optional.ofNullable(enchants[2]));
    }

}
