package net.ramixin.dunchanting.items.components;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.ramixin.dunchanting.util.ModUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record EnchantmentOptions(Optional<EnchantmentSlot> first, Optional<EnchantmentSlot> second, Optional<EnchantmentSlot> third) {

    private static final OldCodecConverter OLD_CODEC_CONVERTER = new OldCodecConverter();

    public static final Codec<EnchantmentOptions> OLD_CODEC = OLD_CODEC_CONVERTER.listOf(9,9).xmap(
            optionals -> {
                List<List<Optional<RegistryEntry<Enchantment>>>> chunks = Lists.partition(optionals, 3);
                EnchantmentSlot[] slots = new EnchantmentSlot[3];
                for(int i = 0; i < 3; i++) {
                    List<Optional<RegistryEntry<Enchantment>>> chunk = chunks.get(i);
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
            Codec.unit("").xmap(
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

    public static final PacketCodec<RegistryByteBuf, EnchantmentOptions> PACKET_CODEC = PacketCodec.of(EnchantmentOptions::encodePacket, EnchantmentOptions::decodePacket);

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

    public boolean isLocked(int index) {
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

    public boolean isInvalid(ItemStack stack, World world) {
        List<RegistryEntry<Enchantment>> enchantList = ModUtils.getPossibleEnchantments(world.getRegistryManager(), stack);
        if(enchantList.size() <= 1) return true;
        for (int i = 0; i < 3; i++) {
            if(isLocked(i)) continue;
            EnchantmentSlot option = this.getOrThrow(i);
            for(int j = 0; j < 3; j++) {
                if(option.isLocked(j)) continue;
                RegistryEntry<Enchantment> enchant = option.getOrThrow(j);
                if(!enchantList.contains(enchant)) return true;
            }
        }
        return false;
    }

    private static EnchantmentOptions decodePacket(RegistryByteBuf buf) {
        EnchantmentSlot[] options = new EnchantmentSlot[3];
        for(int i = 0; i < 3; i++) {
            if(buf.readBoolean()) continue;
            options[i] = EnchantmentSlot.PACKET_CODEC.decode(buf);
        }
        return new EnchantmentOptions(options[0], options[1], options[2]);
    }

    private static void encodePacket(EnchantmentOptions value, RegistryByteBuf buf) {
        for(int j = 0; j < 3; j++) {
            boolean b = value.isLocked(j);
            buf.writeBoolean(b);
            if(b) continue;
            EnchantmentSlot option = value.getOrThrow(j);
            EnchantmentSlot.PACKET_CODEC.encode(buf, option);
        }
    }

    public EnchantmentOptions withEnchantment(RegistryEntry<Enchantment> enchant, int slotId, int optionId) {
        EnchantmentSlot[] options = new EnchantmentSlot[3];
        for(int i = 0; i < 3; i++) {
            if(slotId != i) options[i] = getOptional(i).orElse(null);
            else options[i] = getOptional(i).orElse(EnchantmentSlot.DEFAULT).withEnchantment(enchant, optionId);
        }
        return new EnchantmentOptions(options[0], options[1], options[2]);
    }

    private static class OldCodecConverter implements Codec<Optional<RegistryEntry<Enchantment>>> {

        private static final Codec<Optional<String>> OPTIONAL_STRING_CODEC = Codec.STRING.xmap(
                string -> {
                    if (string.isEmpty()) return Optional.empty();
                    return Optional.of(string);
                },
                s -> s.orElse("")
        );

        @Override
        public <T> DataResult<Pair<Optional<RegistryEntry<Enchantment>>, T>> decode(DynamicOps<T> dynamicOps, T t) {
            if (!(dynamicOps instanceof RegistryOps<T> registryOps))
                return DataResult.error(() -> "Can't access registry to upgrade data");

            DataResult<Pair<Optional<String>, T>> stringsResult = OPTIONAL_STRING_CODEC.decode(registryOps, t);
            if(stringsResult.isError()) return DataResult.error(() -> "Failed to decode old component data");
            Pair<Optional<String>, T> pair = stringsResult.result().orElseThrow();

            Optional<RegistryEntryLookup<Enchantment>> maybeLookup = registryOps.getEntryLookup(RegistryKeys.ENCHANTMENT);
            if(maybeLookup.isEmpty()) return DataResult.error(() -> "Failed to obtain registry lookup");
            RegistryEntryLookup<Enchantment> lookup = maybeLookup.get();

            Optional<RegistryEntry<Enchantment>> result = pair.getFirst().flatMap(string -> {
                RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(string));
                return lookup.getOptional(key);
            });
            return DataResult.success(Pair.of(result, pair.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(Optional<RegistryEntry<Enchantment>> enchantmentRegistryEntry, DynamicOps<T> dynamicOps, T t) {
            return DataResult.error(() -> "Cannot encode old component data");
        }
    }
}
