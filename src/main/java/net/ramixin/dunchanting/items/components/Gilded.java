package net.ramixin.dunchanting.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;

public record Gilded(Holder<Enchantment> enchantmentEntry) {

    public static final Codec<Gilded> CODEC = Enchantment.CODEC.xmap(Gilded::new, Gilded::enchantmentEntry);
    public static final StreamCodec<RegistryFriendlyByteBuf, Gilded> PACKET_CODEC = Enchantment.STREAM_CODEC.map(Gilded::new, Gilded::enchantmentEntry);

}
