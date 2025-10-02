package net.ramixin.dunchanting.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;

public record Gilded(RegistryEntry<Enchantment> enchantmentEntry) {

    public static final Codec<Gilded> CODEC = Enchantment.ENTRY_CODEC.xmap(Gilded::new, Gilded::enchantmentEntry);
    public static final PacketCodec<RegistryByteBuf, Gilded> PACKET_CODEC = Enchantment.ENTRY_PACKET_CODEC.xmap(Gilded::new, Gilded::enchantmentEntry);

}
