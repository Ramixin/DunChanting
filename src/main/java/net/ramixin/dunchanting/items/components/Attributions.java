package net.ramixin.dunchanting.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Attributions(List<AttributionEntry> first, List<AttributionEntry> second, List<AttributionEntry> third, List<AttributionEntry> persistent) {

    public static final Codec<Attributions> CODEC = AttributionEntry.CODEC.listOf().listOf(3,4).xmap(
            lists -> new Attributions(new ArrayList<>(lists.getFirst()), new ArrayList<>(lists.get(1)), new ArrayList<>(lists.get(2)), lists.size() > 3 ? new ArrayList<>(lists.get(3)) : new ArrayList<>()),
            attributions -> List.of(attributions.first, attributions.second, attributions.third, attributions.persistent)
    );

    public static final PacketCodec<RegistryByteBuf, Attributions> PACKET_CODEC = PacketCodec.of(Attributions::write, Attributions::read);

    public static Attributions createNew() {
        return new Attributions(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public static Attributions read(RegistryByteBuf buf) {
        List<List<AttributionEntry>> entries = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            int size = buf.readInt();
            List<AttributionEntry> attributions = new ArrayList<>(size);
            for(int j = 0; j < size; j++) attributions.add(AttributionEntry.PACKET_CODEC.decode(buf));
            entries.add(attributions);
        }
        return new Attributions(entries.get(0), entries.get(1), entries.get(2), entries.get(3));
    }

    public static void write(Attributions attributions, RegistryByteBuf buf) {
        for(int i = 0; i < 4; i++) {
            List<AttributionEntry> entries;
            if(i == 3) entries = attributions.persistent();
            else entries = attributions.get(i);
            buf.writeInt(entries.size());
            for(AttributionEntry entry : entries) AttributionEntry.PACKET_CODEC.encode(buf, entry);
        }
    }

    public List<AttributionEntry> get(int index) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> first;
            case 1 -> second;
            default -> third;
        };
    }

    public void addAttribute(int index, UUID playerUUID, int points) {
        List<AttributionEntry> entries = get(index);
        appendEntry(playerUUID, points, entries);
    }

    public void addPersistentAttribute(UUID playerUUID, int points) {
        List<AttributionEntry> entries = persistent();
        appendEntry(playerUUID, points, entries);
    }

    private void appendEntry(UUID playerUUID, int points, List<AttributionEntry> entries) {
        for(int i = 0; i < entries.size(); i++) {
            AttributionEntry entry = entries.get(i);
            if(!entry.playerUUID().equals(playerUUID)) continue;
            entries.remove(i);
            entries.add(i, new AttributionEntry(playerUUID, points + entry.points()));
            return;
        }
        entries.add(new AttributionEntry(playerUUID, points));
    }

}
