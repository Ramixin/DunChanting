package net.ramixin.dunchanting.client.mixins.gilded;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.ramixin.dunchanting.client.ModRenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilderStorage.class)
public abstract class BufferBuilderStorageMixin {

    @Shadow
    private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> builderStorage, RenderLayer layer) {
        throw new AssertionError();
    }

    @Inject(method = "method_54639", at = @At("TAIL"))
    private void addStorageMappingsForGildedRenderLayers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map, CallbackInfo ci) {
        assignBufferBuilder(map, ModRenderLayers.GILDED_GLINT_TRANSLUCENT);
        assignBufferBuilder(map, ModRenderLayers.GILDED_GLINT);
        assignBufferBuilder(map, ModRenderLayers.GILDED_ARMOR_ENTITY_GLINT);
        assignBufferBuilder(map, ModRenderLayers.GILDED_ENTITY_GLINT);
    }

}
