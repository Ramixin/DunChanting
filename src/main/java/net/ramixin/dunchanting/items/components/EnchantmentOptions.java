package net.ramixin.dunchanting.items.components;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record EnchantmentOptions(Optional<EnchantmentSlot> first, Optional<EnchantmentSlot> second, Optional<EnchantmentSlot> third) {

    private static final OldCodecConverter OLD_CODEC_CONVERTER = new OldCodecConverter();

    public static final Codec<EnchantmentOptions> OLD_CODEC = OLD_CODEC_CONVERTER.listOf(9,9).xmap(
            optionals -> {
                List<List<Optional<Holder<Enchantment>>>> chunks = Lists.partition(optionals, 3);
                EnchantmentSlot[] slots = new EnchantmentSlot[3];
                for(int i = 0; i < 3; i++) {
                    List<Optional<Holder<Enchantment>>> chunk = chunks.get(i);
                    if(chunk.getFirst().isEmpty() && chunk.get(1).isEmpty() && chunk.get(2).isEmpty()) continue;
                    slots[i] = new EnchantmentSlot(chunk.get(0), chunk.get(1), chunk.get(2));
                }
                return new EnchantmentOptions(Optional.ofNullable(slots[0]), Optional.ofNullable(slots[1]), Optional.ofNullable(slots[2]));
            },
            options -> {
                throw new UnsupportedOperationException("OLD CODEC DOES NOT SUPPORT ENCODING");
            }
    );

    public static final Codec<EnchantmentOptions> NEW_CODEC = Codec.either(
            EnchantmentSlot.CODEC.xmap(
                    Optional::of,
                    Optional::orElseThrow
            ),
            MapCodec.unitCodec("").xmap(
                    Functions.<Optional<EnchantmentSlot>>constant(Optional.empty()),
                    Functions.constant("")
            )
    ).xmap(
            Either::unwrap,
            enchantment -> enchantment.isEmpty()
                    ? Either.right(enchantment)
                    : Either.left(enchantment)
    ).listOf(3,3).xmap(
            options -> new EnchantmentOptions(options.getFirst(), options.get(1), options.get(2)),
            options -> List.of(options.first(), options.second(), options.third())
    );

    public static final Codec<EnchantmentOptions> CODEC = Codec.either(NEW_CODEC, OLD_CODEC).xmap(Either::unwrap, Either::left);

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentOptions> PACKET_CODEC = StreamCodec.ofMember(EnchantmentOptions::encodePacket, EnchantmentOptions::decodePacket);

    public static final EnchantmentOptions DEFAULT = new EnchantmentOptions(Optional.empty(), Optional.empty(), Optional.empty());

    public EnchantmentOptions(@Nullable EnchantmentSlot first, @Nullable EnchantmentSlot second, @Nullable EnchantmentSlot third) {
        this(Optional.ofNullable(first), Optional.ofNullable(second), Optional.ofNullable(third));
    }

    public Optional<EnchantmentSlot> getOptional(int index) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> first;
            case 1 -> second;
            default -> third;
        };
    }

    public boolean hasEmptySlot(int index) {
        return getOptional(index).isEmpty();
    }

    public EnchantmentSlot getOrThrow(int index) {
        return getOptional(index).orElseThrow();
    }

    public EnchantmentOptions withSlot(int index, EnchantmentSlot option) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> new EnchantmentOptions(Optional.of(option), second, third);
            case 1 -> new EnchantmentOptions(first, Optional.of(option), third);
            default -> new EnchantmentOptions(first, second, Optional.of(option));
        };
    }

    public EnchantmentOptions withClearedSlots(boolean first, boolean second, boolean third) {
        return new EnchantmentOptions(
                first ? this.first : Optional.empty(),
                second ? this.second : Optional.empty(),
                third ? this.third : Optional.empty()
        );
    }

    private static EnchantmentOptions decodePacket(RegistryFriendlyByteBuf buf) {
        EnchantmentSlot[] options = new EnchantmentSlot[3];
        for(int i = 0; i < 3; i++) {
            if(buf.readBoolean()) continue;
            options[i] = EnchantmentSlot.PACKET_CODEC.decode(buf);
        }
        return new EnchantmentOptions(options[0], options[1], options[2]);
    }

    private static void encodePacket(EnchantmentOptions value, RegistryFriendlyByteBuf buf) {
        for(int j = 0; j < 3; j++) {
            boolean b = value.hasEmptySlot(j);
            buf.writeBoolean(b);
            if(b) continue;
            EnchantmentSlot option = value.getOrThrow(j);
            EnchantmentSlot.PACKET_CODEC.encode(buf, option);
        }
    }

    public EnchantmentOptions withEnchantment(Holder<Enchantment> enchant, int slotId, int optionId) {
        EnchantmentSlot[] options = new EnchantmentSlot[3];
        for(int i = 0; i < 3; i++) {
            if(slotId != i) options[i] = getOptional(i).orElse(null);
            else options[i] = getOptional(i).orElse(EnchantmentSlot.DEFAULT).withEnchantment(enchant, optionId);
        }
        return new EnchantmentOptions(options[0], options[1], options[2]);
    }

    private static class OldCodecConverter implements Codec<Optional<Holder<Enchantment>>> {

        private static final Codec<Optional<String>> OPTIONAL_STRING_CODEC = Codec.STRING.xmap(
                string -> {
                    if (string.isEmpty()) return Optional.empty();
                    return Optional.of(string);
                },
                s -> s.orElse("")
        );

        @Override
        public <T> DataResult<Pair<Optional<Holder<Enchantment>>, T>> decode(DynamicOps<T> dynamicOps, T t) {
            if (!(dynamicOps instanceof RegistryOps<T> registryOps))
                return DataResult.error(() -> "Can't access registry to upgrade data");

            DataResult<Pair<Optional<String>, T>> stringsResult = OPTIONAL_STRING_CODEC.decode(registryOps, t);
            if(stringsResult.isError()) return DataResult.error(() -> "Failed to decode old component data");
            Pair<Optional<String>, T> pair = stringsResult.result().orElseThrow();

            Optional<HolderGetter<Enchantment>> maybeLookup = registryOps.getter(Registries.ENCHANTMENT);
            if(maybeLookup.isEmpty()) return DataResult.error(() -> "Failed to obtain registry lookup");
            HolderGetter<Enchantment> lookup = maybeLookup.get();

            Optional<Holder<Enchantment>> result = pair.getFirst().flatMap(string -> {
                ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(string));
                return lookup.get(key);
            });
            return DataResult.success(Pair.of(result, pair.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(Optional<Holder<Enchantment>> enchantmentRegistryEntry, DynamicOps<T> dynamicOps, T t) {
            return DataResult.error(() -> "Cannot encode old component data");
        }
    }
}
