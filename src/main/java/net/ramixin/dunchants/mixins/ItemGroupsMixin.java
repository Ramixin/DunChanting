package net.ramixin.dunchants.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {

    @WrapOperation(
            method = "method_59972",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I")
    )
    private static int changeEnchantmentBootstrapMaxLevelsBecauseTheCreativeMenuIsJank(Enchantment instance, Operation<Integer> original) {
        return 3;
    }

    @WrapOperation(
            method = "method_59969",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I")
    )
    private static int changeEnchantmentBootstrapMaxLevelsBecauseTheCreativeMenuIsJankTwo(Enchantment instance, Operation<Integer> original) {
        return 3;
    }

}
