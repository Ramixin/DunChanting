package net.ramixin.dunchanting.client.mixins.gilded;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.ItemStack;
import net.ramixin.dunchanting.client.ModRenderLayers;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {

    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getArmorEntityGlint()Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer changeArmorGlintToGildedGlintIfGilded(RenderLayer original, @Local(argsOnly = true) ItemStack stack) {
        Gilded gilded = stack.get(ModItemComponents.GILDED);
        if(gilded == null) return original;
        return ModRenderLayers.GILDED_ARMOR_ENTITY_GLINT;
    }

}
