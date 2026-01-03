package net.ramixin.dunchanting.mixins.enchantments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.item.ItemStack;
import net.ramixin.dunchanting.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.util.ModUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorSlot.class)
public class ArmorSlotMixin {

    @Shadow @Final private LivingEntity owner;

    @Inject(method = "setByPlayer", at = @At("HEAD"))
    private void addBoundComponentIfHasBinding(ItemStack stack, ItemStack previousStack, CallbackInfo ci) {
        if(ModUtil.getLeveledEnchantmentEffectValue(ModEnchantmentEffects.LEVELED_PREVENT_ARMOR_CHANGE, this.owner.level(), stack))
            stack.set(ModDataComponents.BOUND, Unit.INSTANCE);
        else
            stack.remove(ModDataComponents.BOUND);
    }

    @WrapOperation(method = "mayPickup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;has(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/component/DataComponentType;)Z"))
    private boolean applyLeveledBindingCurse(ItemStack stack, DataComponentType<?> componentType, Operation<Boolean> original) {
        boolean val = original.call(stack, componentType);
        return val | stack.has(ModDataComponents.BOUND);
    }

}
