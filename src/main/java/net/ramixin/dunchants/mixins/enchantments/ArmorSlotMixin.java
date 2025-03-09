package net.ramixin.dunchants.mixins.enchantments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ArmorSlot;
import net.minecraft.util.Unit;
import net.ramixin.dunchants.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.util.DungeonEnchantsUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorSlot.class)
public class ArmorSlotMixin {

    @Shadow @Final private LivingEntity entity;

    @Inject(method = "setStack", at = @At("HEAD"))
    private void addBoundComponentIfHasBinding(ItemStack stack, ItemStack previousStack, CallbackInfo ci) {
        if(DungeonEnchantsUtils.getLeveledEnchantmentEffectValue(ModEnchantmentEffects.LEVELED_PREVENT_ARMOR_CHANGE, this.entity.getWorld(), stack))
            stack.set(ModItemComponents.BOUND, Unit.INSTANCE);
        else
            stack.remove(ModItemComponents.BOUND);
    }

    @WrapOperation(method = "canTakeItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasAnyEnchantmentsWith(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/ComponentType;)Z"))
    private boolean applyLeveledBindingCurse(ItemStack stack, ComponentType<?> componentType, Operation<Boolean> original, @Local(argsOnly = true) PlayerEntity player) {
        boolean val = original.call(stack, componentType);
        return val | stack.contains(ModItemComponents.BOUND);
    }

}
