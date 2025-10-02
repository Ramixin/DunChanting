package net.ramixin.dunchanting.items.components;

import com.google.common.base.Functions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.ramixin.dunchanting.util.ModUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record EnchantmentOptions(Optional<EnchantmentSlot> first, Optional<EnchantmentSlot> second, Optional<EnchantmentSlot> third) {

    public static final Codec<EnchantmentOptions> CODEC = Codec.either(
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

}
