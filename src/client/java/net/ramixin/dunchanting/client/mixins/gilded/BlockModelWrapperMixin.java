package net.ramixin.dunchanting.client.mixins.gilded;

import com.chocohead.mm.api.ClassTinkerers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(BlockModelWrapper.class)
public class BlockModelWrapperMixin {

    @ModifyExpressionValue(method = "update", at = {@At(value = "FIELD", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;SPECIAL:Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;", opcode = Opcodes.GETSTATIC), @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;STANDARD:Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;", opcode = Opcodes.GETSTATIC)})
    private ItemStackRenderState.FoilType changeGlintToGildedGlintIfGilded(ItemStackRenderState.FoilType original, @Local(argsOnly = true) ItemStack stack) {
        Gilded gilded = stack.get(ModDataComponents.GILDED);
        if(gilded == null) return original;
        return switch(original) {
            case STANDARD -> ClassTinkerers.getEnum(ItemStackRenderState.FoilType.class, "STANDARD_GILDED");
            case SPECIAL -> ClassTinkerers.getEnum(ItemStackRenderState.FoilType.class, "SPECIAL_GILDED");
            default -> original;
        };
    }
}
