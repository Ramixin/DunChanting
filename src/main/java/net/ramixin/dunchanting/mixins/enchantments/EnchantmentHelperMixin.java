package net.ramixin.dunchanting.mixins.enchantments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @WrapOperation(method = "processProjectileCount", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableFloat;intValue()I", remap = false))
    private static int addRandomChanceToProjectileCount(MutableFloat instance, Operation<Integer> original, @Local(argsOnly = true) ServerLevel world) {
        if(world.getRandom().nextBoolean()) return Math.round((float) instance.get());
        else return original.call(instance);
    }

}
