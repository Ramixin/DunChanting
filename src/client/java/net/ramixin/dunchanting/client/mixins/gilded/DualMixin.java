package net.ramixin.dunchanting.client.mixins.gilded;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.ramixin.dunchanting.client.util.DualDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexConsumers.Dual.class)
public class DualMixin implements DualDuck {

    @Shadow @Final private VertexConsumer second;

    @Override
    public VertexConsumer dunchanting$getSecond() {
        return this.second;
    }
}
