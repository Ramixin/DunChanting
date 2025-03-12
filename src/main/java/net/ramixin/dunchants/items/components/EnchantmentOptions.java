package net.ramixin.dunchants.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.ramixin.util.ModUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static net.ramixin.util.ModUtils.enchantmentIsInvalid;

public record EnchantmentOptions(Optional<EnchantmentOption> first, Optional<EnchantmentOption> second, Optional<EnchantmentOption> third) {

    public static final Codec<EnchantmentOptions> CODEC = Codec.STRING.listOf(9,9).xmap(EnchantmentOptions::decode, EnchantmentOptions::encode);

    public static final PacketCodec<RegistryByteBuf, EnchantmentOptions> PACKET_CODEC = PacketCodec.of(EnchantmentOptions::encodePacket, EnchantmentOptions::decodePacket);

    public static final EnchantmentOptions DEFAULT = new EnchantmentOptions(Optional.empty(), Optional.empty(), Optional.empty());

    public Optional<EnchantmentOption> getOptional(int index) {
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

    public EnchantmentOption get(int index) {
        return getOptional(index).orElseThrow();
    }

    public boolean isInvalid(ItemStack stack, World world) {
        List<RegistryEntry<Enchantment>> enchantList = ModUtils.getPossibleEnchantments(world.getRegistryManager(), stack);
        if(enchantList.size() <= 1) return true;
        for (int i = 0; i < 3; i++) {
            if(isLocked(i)) continue;
            EnchantmentOption option = get(i);
            for(int j = 0; j < 3; j++) {
                if(option.isLocked(j)) continue;
                String enchant = option.get(j);
                if(enchantmentIsInvalid(enchant, world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT), enchantList)) return true;
            }
        }
        return false;
    }

    private static EnchantmentOptions decode(List<String> list) {
        if(list.size() != 9) throw new IndexOutOfBoundsException("enchantment options must have 9 arguments");
        EnchantmentOption[] options = new EnchantmentOption[3];
        Iterator<String> iterator = list.iterator();
        for(int i = 0; i < 3; i++) {
            EnchantmentOption option = ModUtils.decodeGenericTripleOptionalList(iterator, EnchantmentOption::new, String::isEmpty);
            if(option.isLocked(0)) options[i] = null;
            else options[i] = option;
        }
        return new EnchantmentOptions(Optional.ofNullable(options[0]), Optional.ofNullable(options[1]), Optional.ofNullable(options[2]));
    }

    private List<String> encode() {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            getOptional(i).ifPresentOrElse(
                    option -> list.addAll(ModUtils.encodeGenericTripleOptionalList(option::getOptional, "")),
                    () -> { for (int l = 0; l < 3; l++) list.add(""); }
            );
        }
        return list;
    }

    private static EnchantmentOptions decodePacket(RegistryByteBuf buf) {
        EnchantmentOption[] options = new EnchantmentOption[3];
        for(int i = 0; i < 3; i++) {
            int count = buf.readInt();
            if(count == 0) continue;

            String[] enchants = new String[3];
            for(int j = 0; j < count; j++) enchants[j] = buf.readString();

            if(enchants[0] != null || enchants[1] != null || enchants[2] != null)
                options[i] = new EnchantmentOption(Optional.ofNullable(enchants[0]), Optional.ofNullable(enchants[1]), Optional.ofNullable(enchants[2]));
        }
        return new EnchantmentOptions(Optional.ofNullable(options[0]), Optional.ofNullable(options[1]), Optional.ofNullable(options[2]));
    }

    private static void encodePacket(EnchantmentOptions value, RegistryByteBuf buf) {
        for(int j = 0; j < 3; j++) {
            if(value.isLocked(j)) {
                buf.writeInt(0);
                continue;
            }
            EnchantmentOption option = value.get(j);
            buf.writeInt(option.unlockedCount());
            for(int k = 0; k < 3; k++) {
                if(option.isLocked(k)) continue;
                buf.writeString(option.get(k));
            }
        }
    }

}
