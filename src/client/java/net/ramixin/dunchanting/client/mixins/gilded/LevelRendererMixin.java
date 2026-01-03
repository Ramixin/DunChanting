package net.ramixin.dunchanting.client.mixins.gilded;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.ramixin.dunchanting.client.ModRenderLayers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/rendertype/RenderType;)V", ordinal = 16))
    private void addGildedRenderLayersToImmediateRendering(GpuBufferSlice gpuBufferSlice, LevelRenderState levelRenderState, ProfilerFiller profilerFiller, Matrix4f matrix4f, ResourceHandle<?> resourceHandle, ResourceHandle<?> resourceHandle2, boolean bl, ResourceHandle<?> resourceHandle3, ResourceHandle<?> resourceHandle4, CallbackInfo ci, @Local(ordinal = 0) MultiBufferSource.BufferSource immediate) {
        immediate.endBatch(ModRenderLayers.GILDED_ARMOR_ENTITY_GLINT);
        immediate.endBatch(ModRenderLayers.GILDED_GLINT);
        immediate.endBatch(ModRenderLayers.GILDED_GLINT_TRANSLUCENT);
        immediate.endBatch(ModRenderLayers.GILDED_ENTITY_GLINT);
    }

}
