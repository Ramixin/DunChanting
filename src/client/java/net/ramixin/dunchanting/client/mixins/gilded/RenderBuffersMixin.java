package net.ramixin.dunchanting.client.mixins.gilded;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.ramixin.dunchanting.client.ModRenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBuffers.class)
public abstract class RenderBuffersMixin {

    @Shadow
    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> builderStorage, RenderType layer) {
        throw new AssertionError();
    }

    @Inject(method = "method_54639", at = @At("TAIL"))
    private void addStorageMappingsForGildedRenderLayers(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, CallbackInfo ci) {
        put(map, ModRenderLayers.GILDED_GLINT_TRANSLUCENT);
        put(map, ModRenderLayers.GILDED_GLINT);
        put(map, ModRenderLayers.GILDED_ARMOR_ENTITY_GLINT);
        put(map, ModRenderLayers.GILDED_ENTITY_GLINT);
    }

}
