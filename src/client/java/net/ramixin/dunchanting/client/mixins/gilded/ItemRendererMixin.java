package net.ramixin.dunchanting.client.mixins.gilded;

import com.chocohead.mm.api.ClassTinkerers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.ramixin.dunchanting.client.ModRenderLayers;
import net.ramixin.dunchanting.client.util.DualDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"))
    private static VertexConsumer applyGildedGlintIfStandardGilded(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemRenderState.Glint glintVariant) {
        if(glintVariant == ItemRenderState.Glint.NONE) return original.call(vertexConsumers, layer, solid, false);
        if(glintVariant != ClassTinkerers.getEnum(ItemRenderState.Glint.class, "STANDARD_GILDED")) return original.call(vertexConsumers, layer, solid, true);
        VertexConsumer consumer = original.call(vertexConsumers, layer, solid, true);
        if(!(consumer instanceof VertexConsumers.Dual dual)) return consumer;
        DualDuck duck = (DualDuck) dual;
        return VertexConsumers.union(vertexConsumers.getBuffer(ModRenderLayers.GILDED_GLINT_TRANSLUCENT), duck.dunchanting$getSecond());
    }

}
