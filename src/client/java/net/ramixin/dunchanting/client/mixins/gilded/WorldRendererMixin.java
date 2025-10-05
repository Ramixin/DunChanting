package net.ramixin.dunchanting.client.mixins.gilded;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.Handle;
import net.minecraft.util.profiler.Profiler;
import net.ramixin.dunchanting.client.ModRenderLayers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw(Lnet/minecraft/client/render/RenderLayer;)V", ordinal = 16))
    private void addGildedRenderLayersToImmediateRendering(GpuBufferSlice gpuBufferSlice, WorldRenderState worldRenderState, Profiler profiler, Matrix4f matrix4f, Handle<?> handle, Handle<?> handle2, boolean bl, Frustum frustum, Handle<?> handle3, Handle<?> handle4, CallbackInfo ci, @Local(ordinal = 0) VertexConsumerProvider.Immediate immediate) {
        immediate.draw(ModRenderLayers.GILDED_ARMOR_ENTITY_GLINT);
        immediate.draw(ModRenderLayers.GILDED_GLINT);
        immediate.draw(ModRenderLayers.GILDED_GLINT_TRANSLUCENT);
        immediate.draw(ModRenderLayers.GILDED_ENTITY_GLINT);
    }

}
