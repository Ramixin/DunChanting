package net.ramixin.dunchanting.client.mixins.gilded;

import com.chocohead.mm.api.ClassTinkerers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.item.ItemStack;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BasicItemModel.class)
public class BasicItemModelMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderState$LayerRenderState;setGlint(Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"))
    private void setGlintToGildedIfGilded(ItemRenderState.LayerRenderState instance, ItemRenderState.Glint glint, Operation<Void> original, @Local(argsOnly = true) ItemStack stack) {
        if(glint == ItemRenderState.Glint.NONE) {
            original.call(instance, glint);
            return;
        }
        Gilded gilded = stack.get(ModItemComponents.GILDED);
        if(gilded == null) {
            original.call(instance, glint);
            return;
        }
        switch(glint) {
            case STANDARD -> original.call(instance, ClassTinkerers.getEnum(ItemRenderState.Glint.class, "STANDARD_GILDED"));
            case SPECIAL -> original.call(instance, ClassTinkerers.getEnum(ItemRenderState.Glint.class, "SPECIAL_GILDED"));
            default -> original.call(instance, glint);
        }

    }

}
