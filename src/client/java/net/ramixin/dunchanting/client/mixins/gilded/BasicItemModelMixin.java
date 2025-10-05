package net.ramixin.dunchanting.client.mixins.gilded;

import com.chocohead.mm.api.ClassTinkerers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.item.ItemStack;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(BasicItemModel.class)
public class BasicItemModelMixin {

    @ModifyExpressionValue(method = "update", at = {@At(value = "FIELD", target = "Lnet/minecraft/client/render/item/ItemRenderState$Glint;SPECIAL:Lnet/minecraft/client/render/item/ItemRenderState$Glint;"), @At(value = "FIELD", target = "Lnet/minecraft/client/render/item/ItemRenderState$Glint;STANDARD:Lnet/minecraft/client/render/item/ItemRenderState$Glint;")})
    private ItemRenderState.Glint changeGlintToGildedGlintIfGilded(ItemRenderState.Glint original, @Local(argsOnly = true) ItemStack stack) {
        Gilded gilded = stack.get(ModItemComponents.GILDED);
        if(gilded == null) return original;
        return switch(original) {
            case STANDARD -> ClassTinkerers.getEnum(ItemRenderState.Glint.class, "STANDARD_GILDED");
            case SPECIAL -> ClassTinkerers.getEnum(ItemRenderState.Glint.class, "SPECIAL_GILDED");
            default -> original;
        };
    }
}
