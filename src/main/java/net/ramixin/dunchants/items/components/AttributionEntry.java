package net.ramixin.dunchants.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record AttributionEntry(UUID playerUUID, int points) {

    public static final Codec<AttributionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(AttributionEntry::playerUUID),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("points").forGetter(AttributionEntry::points)
    ).apply(instance, instance.stable(AttributionEntry::new)));

    public static final PacketCodec<RegistryByteBuf, AttributionEntry> PACKET_CODEC = PacketCodec.of(AttributionEntry::write, AttributionEntry::new);

    public AttributionEntry(RegistryByteBuf buf) {
        this(buf.readUuid(), buf.readInt());
    }

    private static void write(AttributionEntry entry, RegistryByteBuf buf) {
        buf.writeUuid(entry.playerUUID());
        buf.writeInt(entry.points());
    }
}
