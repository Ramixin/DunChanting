package net.ramixin.dunchanting.items.components;

import com.google.common.base.Functions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record EnchantmentSlot(Optional<Holder<Enchantment>> first, Optional<Holder<Enchantment>> second, Optional<Holder<Enchantment>> third) {

    public static final EnchantmentSlot DEFAULT = new EnchantmentSlot(Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<EnchantmentSlot> CODEC = Codec.either(
            Enchantment.CODEC.xmap(
                    Optional::of,
                    Optional::orElseThrow
            ),
            MapCodec.unitCodec("").xmap(
                    Functions.<Optional<Holder<Enchantment>>>constant(Optional.empty()),
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

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentSlot> PACKET_CODEC = StreamCodec.ofMember(EnchantmentSlot::encodePacket, EnchantmentSlot::decodePacket);

    private static EnchantmentSlot decodePacket(RegistryFriendlyByteBuf buf) {
        return new EnchantmentSlot(
                buf.readBoolean() ? null : Enchantment.STREAM_CODEC.decode(buf),
                buf.readBoolean() ? null : Enchantment.STREAM_CODEC.decode(buf),
                buf.readBoolean() ? null : Enchantment.STREAM_CODEC.decode(buf)
        );
    }

    private static void encodePacket(EnchantmentSlot slot, RegistryFriendlyByteBuf buf) {
        for(int i = 0; i < 3; i++) {
            boolean b = slot.isLocked(i);
            buf.writeBoolean(b);
            if(b) continue;
            Holder<Enchantment> enchant = slot.getOrThrow(i);
            Enchantment.STREAM_CODEC.encode(buf, enchant);
        }
    }


    public EnchantmentSlot(@Nullable Holder<Enchantment> first, @Nullable Holder<Enchantment> second, @Nullable Holder<Enchantment> third) {
        this(Optional.ofNullable(first), Optional.ofNullable(second), Optional.ofNullable(third));
    }

    public Optional<Holder<Enchantment>> getOptional(int index) {
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

    public Holder<Enchantment> getOrThrow(int index) {
        return getOptional(index).orElseThrow();
    }

    public EnchantmentSlot withEnchantment(Holder<Enchantment> entry, int optionId) {
        return new EnchantmentSlot(
                optionId == 0 ? Optional.of(entry) : first,
                optionId == 1 ? Optional.of(entry) : second,
                optionId == 2 ? Optional.of(entry) : third
        );
    }

}
