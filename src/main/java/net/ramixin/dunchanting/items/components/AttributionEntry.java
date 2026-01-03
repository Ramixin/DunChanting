package net.ramixin.dunchanting.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record AttributionEntry(UUID playerUUID, int points) {

    public static final Codec<AttributionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.AUTHLIB_CODEC.fieldOf("uuid").forGetter(AttributionEntry::playerUUID),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("points").forGetter(AttributionEntry::points)
    ).apply(instance, instance.stable(AttributionEntry::new)));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributionEntry> PACKET_CODEC = StreamCodec.ofMember(AttributionEntry::write, AttributionEntry::new);

    public AttributionEntry(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt());
    }

    private static void write(AttributionEntry entry, RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entry.playerUUID());
        buf.writeInt(entry.points());
    }
}
