package net.ramixin.dunchants.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.ramixin.dunchants.enchantments.ModEnchantmentEffects;
import net.ramixin.util.DungeonEnchantsUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow public int experienceLevel;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    private void changeXpProgression(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) (50 * Math.pow(1.25, experienceLevel)));
    }

    @ModifyReturnValue(method = "shouldAlwaysDropExperience", at = @At("RETURN"))
    private boolean changeShouldAlwaysDropXp(boolean original) {
        return true;
    }

    @ModifyReturnValue(method = "getExperienceToDrop", at = @At("RETURN"))
    private int preventXpOrbsFromDropping(int original) {
        return 0;
    }

    @WrapOperation(method = "vanishCursedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasAnyEnchantmentsWith(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/ComponentType;)Z"))
    private boolean applyLeveledVanishingCurse(ItemStack stack, ComponentType<?> componentType, Operation<Boolean> original) {
        boolean val = original.call(stack, componentType);
        return val | DungeonEnchantsUtils.getLeveledEnchantmentEffectValue(ModEnchantmentEffects.LEVELED_PREVENT_EQUIPMENT_DROP, getWorld(), stack);
    }
}
