package net.ramixin.dunchanting.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.dunchanting.Dunchanting;
import org.jspecify.annotations.NonNull;

public record EnchantmentPointsUpdateS2CPayload(int value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EnchantmentPointsUpdateS2CPayload> PACKET_ID = new CustomPacketPayload.Type<>(Dunchanting.id("enchantment_points_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentPointsUpdateS2CPayload> PACKET_CODEC = StreamCodec.ofMember(EnchantmentPointsUpdateS2CPayload::write, EnchantmentPointsUpdateS2CPayload::new);


    public EnchantmentPointsUpdateS2CPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(value);
    }


    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
