package net.ramixin.dunchanting.items.components;

import com.google.common.base.Functions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record EnchantmentSlot(Optional<RegistryEntry<Enchantment>> first, Optional<RegistryEntry<Enchantment>> second, Optional<RegistryEntry<Enchantment>> third) {

    public static final EnchantmentSlot DEFAULT = new EnchantmentSlot(Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<EnchantmentSlot> CODEC = Codec.either(
            Enchantment.ENTRY_CODEC.xmap(
                    Optional::of,
                    Optional::orElseThrow
            ),
            Codec.unit("").xmap(
                    Functions.<Optional<RegistryEntry<Enchantment>>>constant(Optional.empty()),
                    Functions.constant("")
            )
    ).xmap(
            Either::unwrap,
            enchantment -> enchantment.isEmpty()
                    ? Either.right(enchantment)
                    : Either.left(enchantment)
    ).listOf(3,3).xmap(
            slots -> new EnchantmentSlot(slots.getFirst(), slots.get(1), slots.get(2)),
            option -> List.of(option.first(), option.second(), option.third())
    );

    public static final PacketCodec<RegistryByteBuf, EnchantmentSlot> PACKET_CODEC = PacketCodec.of(EnchantmentSlot::encodePacket, EnchantmentSlot::decodePacket);

    private static EnchantmentSlot decodePacket(RegistryByteBuf buf) {
        return new EnchantmentSlot(
                buf.readBoolean() ? null : Enchantment.ENTRY_PACKET_CODEC.decode(buf),
                buf.readBoolean() ? null : Enchantment.ENTRY_PACKET_CODEC.decode(buf),
                buf.readBoolean() ? null : Enchantment.ENTRY_PACKET_CODEC.decode(buf)
        );
    }

    private static void encodePacket(EnchantmentSlot slot, RegistryByteBuf buf) {
        for(int i = 0; i < 3; i++) {
            boolean b = slot.isLocked(i);
            buf.writeBoolean(b);
            if(b) continue;
            RegistryEntry<Enchantment> enchant = slot.getOrThrow(i);
            Enchantment.ENTRY_PACKET_CODEC.encode(buf, enchant);
        }
    }


    public EnchantmentSlot(@Nullable RegistryEntry<Enchantment> first, @Nullable RegistryEntry<Enchantment> second, @Nullable RegistryEntry<Enchantment> third) {
        this(Optional.ofNullable(first), Optional.ofNullable(second), Optional.ofNullable(third));
    }

    public Optional<RegistryEntry<Enchantment>> getOptional(int index) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> first;
            case 1 -> second;
            default -> third;
        };
    }

    public boolean isLocked(int index) {
        return getOptional(index).isEmpty();
    }

    public RegistryEntry<Enchantment> getOrThrow(int index) {
        return getOptional(index).orElseThrow();
    }

    public EnchantmentSlot withEnchantment(RegistryEntry<Enchantment> entry, int optionId) {
        return new EnchantmentSlot(
                optionId == 0 ? Optional.of(entry) : first,
                optionId == 1 ? Optional.of(entry) : second,
                optionId == 2 ? Optional.of(entry) : third
        );
    }

}
