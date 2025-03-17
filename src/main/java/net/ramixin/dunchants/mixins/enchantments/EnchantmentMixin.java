package net.ramixin.dunchants.mixins.enchantments;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.ramixin.dunchants.enchantments.LeveledEnchantmentEffect;
import net.ramixin.dunchants.util.EnchantmentDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements EnchantmentDuck {

    @Shadow public abstract ComponentMap effects();

    @Override
    public boolean dungeonEnchants$getLeveledEffectResult(ComponentType<LeveledEnchantmentEffect> type, World world, int level) {
        if(!this.effects().contains(type)) return false;
        return level >= world.getRandom().nextBetween(1, 3);
    }

    @ModifyReturnValue(method = "isAcceptableItem", at = @At(value = "RETURN"))
    private boolean preventMoreThanThreeEnchantments(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original && itemStack.getEnchantments().getEnchantments().size() < 3;
    }

}
