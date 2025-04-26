package net.ramixin.dunchanting.mixins.enchantments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @WrapOperation(method = "getProjectileCount", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableFloat;intValue()I", remap = false))
    private static int addRandomChanceToProjectileCount(MutableFloat instance, Operation<Integer> original, @Local(argsOnly = true) ServerWorld world) {
        if(world.getRandom().nextBoolean()) return Math.round(instance.getValue());
        else return original.call(instance);
    }

}
