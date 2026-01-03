package net.ramixin.dunchanting.client.mixins.gilded;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.ramixin.dunchanting.client.util.DualDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexMultiConsumer.Double.class)
public class DoubleMixin implements DualDuck {

    @Shadow @Final private VertexConsumer second;

    @Override
    public VertexConsumer dunchanting$getSecond() {
        return this.second;
    }
}
