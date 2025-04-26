package net.ramixin.dunchanting.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.ramixin.dunchanting.Dunchanting;

public record EnchantmentPointsUpdateS2CPayload(int value) implements CustomPayload {

    public static final CustomPayload.Id<EnchantmentPointsUpdateS2CPayload> PACKET_ID = new CustomPayload.Id<>(Dunchanting.id("enchantment_points_update"));
    public static final PacketCodec<RegistryByteBuf, EnchantmentPointsUpdateS2CPayload> PACKET_CODEC = PacketCodec.of(EnchantmentPointsUpdateS2CPayload::write, EnchantmentPointsUpdateS2CPayload::new);


    public EnchantmentPointsUpdateS2CPayload(RegistryByteBuf buf) {
        this(buf.readInt());
    }

    private void write(RegistryByteBuf buf) {
        buf.writeInt(value);
    }


    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
