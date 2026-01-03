package net.ramixin.dunchanting.client.mixins.gilded;

import com.chocohead.mm.api.ClassTinkerers;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.ramixin.dunchanting.client.ModRenderLayers;
import net.ramixin.dunchanting.client.util.DualDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    private static boolean useTransparentGlint(RenderType renderLayer) {
        throw new AssertionError();
    }

    @Definition(id = "SPECIAL", field = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;SPECIAL:Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;")
    @Expression("? == SPECIAL")
    @ModifyExpressionValue(method = "renderItem", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean allowSpecialGildedGlintToEnterIfStatement(boolean original, @Local(argsOnly = true) ItemStackRenderState.FoilType glintVariant) {
        return original || glintVariant == ClassTinkerers.getEnum(ItemStackRenderState.FoilType.class, "SPECIAL_GILDED");
    }

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/rendertype/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer applyGildedGlintIfStandardGilded(MultiBufferSource vertexConsumers, RenderType layer, boolean solid, boolean glint, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemStackRenderState.FoilType glintVariant) {
        if(glintVariant == ItemStackRenderState.FoilType.NONE) return original.call(vertexConsumers, layer, solid, false);
        VertexConsumer consumer = original.call(vertexConsumers, layer, solid, true);
        if(glintVariant.ordinal() == ItemStackRenderState.FoilType.STANDARD.ordinal())
            return consumer;
        if(!(consumer instanceof VertexMultiConsumer.Double dual)) return consumer;
        VertexConsumer usingConsumer = ((DualDuck) dual).dunchanting$getSecond();
        RenderType glintLayer;
        if(useTransparentGlint(layer))
            glintLayer = ModRenderLayers.GILDED_GLINT_TRANSLUCENT;
        else
            if(solid)
                glintLayer = ModRenderLayers.GILDED_GLINT;
            else
                glintLayer = ModRenderLayers.GILDED_ENTITY_GLINT;

        return VertexMultiConsumer.create(vertexConsumers.getBuffer(glintLayer), usingConsumer);
    }

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getSpecialFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer applyGildedGlintIfSpecialGilded(MultiBufferSource consumers, RenderType layer, PoseStack.Pose matrix, Operation<VertexConsumer> original, @Local(argsOnly = true) ItemStackRenderState.FoilType glintVariant) {
        VertexConsumer consumer = original.call(consumers, layer, matrix);
        if(glintVariant == ItemStackRenderState.FoilType.SPECIAL)
            return consumer;
        if(!(consumer instanceof VertexMultiConsumer.Double dual))
            return consumer;
        VertexConsumer usingConsumer = ((DualDuck) dual).dunchanting$getSecond();
        RenderType glintLayer;
        if(useTransparentGlint(layer))
            glintLayer = ModRenderLayers.GILDED_GLINT_TRANSLUCENT;
        else
            glintLayer = ModRenderLayers.GILDED_GLINT;
        return VertexMultiConsumer.create(consumers.getBuffer(glintLayer), usingConsumer);
    }

}
