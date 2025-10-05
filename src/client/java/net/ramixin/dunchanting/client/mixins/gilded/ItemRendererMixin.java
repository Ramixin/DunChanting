package net.ramixin.dunchanting.client.mixins.gilded;

import com.chocohead.mm.api.ClassTinkerers;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.ramixin.dunchanting.client.ModRenderLayers;
import net.ramixin.dunchanting.client.util.DualDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    private static boolean useTransparentGlint(RenderLayer renderLayer) {
        throw new AssertionError();
    }

    @Definition(id = "SPECIAL", field = "Lnet/minecraft/client/render/item/ItemRenderState$Glint;SPECIAL:Lnet/minecraft/client/render/item/ItemRenderState$Glint;")
    @Expression("? == SPECIAL")
    @ModifyExpressionValue(method = "renderItem", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean allowSpecialGildedGlintToEnterIfStatement(boolean original, @Local(argsOnly = true) ItemRenderState.Glint glintVariant) {
        return original || glintVariant == ClassTinkerers.getEnum(ItemRenderState.Glint.class, "SPECIAL_GILDED");
    }

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"))
    private static VertexConsumer applyGildedGlintIfStandardGilded(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemRenderState.Glint glintVariant) {
        if(glintVariant == ItemRenderState.Glint.NONE) return original.call(vertexConsumers, layer, solid, false);
        VertexConsumer consumer = original.call(vertexConsumers, layer, solid, true);
        if(glintVariant.ordinal() == ItemRenderState.Glint.STANDARD.ordinal())
            return consumer;
        if(!(consumer instanceof VertexConsumers.Dual dual)) return consumer;
        VertexConsumer usingConsumer = ((DualDuck) dual).dunchanting$getSecond();
        RenderLayer glintLayer;
        if(useTransparentGlint(layer))
            glintLayer = ModRenderLayers.GILDED_GLINT_TRANSLUCENT;
        else
            if(solid)
                glintLayer = ModRenderLayers.GILDED_GLINT;
            else
                glintLayer = ModRenderLayers.GILDED_ENTITY_GLINT;

        return VertexConsumers.union(vertexConsumers.getBuffer(glintLayer), usingConsumer);
    }

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getSpecialItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack$Entry;)Lnet/minecraft/client/render/VertexConsumer;"))
    private static VertexConsumer applyGildedGlintIfSpecialGilded(VertexConsumerProvider consumers, RenderLayer layer, MatrixStack.Entry matrix, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemRenderState.Glint glintVariant) {
        VertexConsumer consumer = original.call(consumers, layer, matrix);
        if(glintVariant == ItemRenderState.Glint.SPECIAL)
            return consumer;
        if(!(consumer instanceof VertexConsumers.Dual dual))
            return consumer;
        VertexConsumer usingConsumer = ((DualDuck) dual).dunchanting$getSecond();
        RenderLayer glintLayer;
        if(useTransparentGlint(layer))
            glintLayer = ModRenderLayers.GILDED_GLINT_TRANSLUCENT;
        else
            glintLayer = ModRenderLayers.GILDED_GLINT;
        return VertexConsumers.union(consumers.getBuffer(glintLayer), usingConsumer);
    }

}
