package net.ramixin.dunchanting.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.ramixin.dunchanting.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.ModUtils;
import net.ramixin.dunchanting.util.PlayerEntityDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityDuck {

    @Shadow public int experienceLevel;

    @Unique
    private int enchantmentPoints;

    @Unique
    private int highestEnchantmentPoints;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    private void changeXpProgression(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(50 * (experienceLevel + 1));
    }

    @ModifyReturnValue(method =
            //? if >=1.21.4 {
            "shouldAlwaysDropExperience"
            //?} else
            /*"shouldAlwaysDropXp"*/
            , at = @At("RETURN"))
    private boolean changeShouldAlwaysDropXp(boolean original) {
        return true;
    }

    @ModifyReturnValue(method =
            //? if >=1.21.4 {
            "getExperienceToDrop"
            //?} else
            /*"getXpToDrop"*/
            , at = @At("RETURN"))
    private int preventXpOrbsFromDropping(int original) {
        return 0;
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeEnchantmentPointsToData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("EnchantmentPoints", enchantmentPoints);
        nbt.putInt("HighestEnchantmentPoints", highestEnchantmentPoints);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readEnchantmentPointsFromData(NbtCompound nbt, CallbackInfo ci) {
        enchantmentPoints = nbt.getInt("EnchantmentPoints");
        highestEnchantmentPoints = nbt.getInt("HighestEnchantmentPoints");
    }

    @Inject(method = "addExperienceLevels", at = @At("TAIL"))
    private void grantEnchantmentPointOnLevelUp(int levels, CallbackInfo ci) {
        if(levels == 0) return;
        int levelsToAdd;
        if(highestEnchantmentPoints > this.experienceLevel) levelsToAdd = levels - ( this.experienceLevel - highestEnchantmentPoints );
        else levelsToAdd = levels;
        if(levelsToAdd < 0) return;
        enchantmentPoints += levelsToAdd;
        //noinspection ConstantValue
        if(!(((LivingEntity) this) instanceof ServerPlayerEntity serverPlayer)) return;
        ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(enchantmentPoints));
    }


    @Override
    public void dungeonEnchants$changeEnchantmentPoints(int delta) {
        enchantmentPoints += delta;
    }

    @Override
    public void dungeonEnchants$setEnchantmentPoints(int enchantmentPoints) {
        this.enchantmentPoints = enchantmentPoints;
    }

    @Override
    public int dungeonEnchants$getEnchantmentPoints() {
        return enchantmentPoints;
    }



    @WrapOperation(method = "vanishCursedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasAnyEnchantmentsWith(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/ComponentType;)Z"))
    private boolean applyLeveledVanishingCurse(ItemStack stack, ComponentType<?> componentType, Operation<Boolean> original) {
        boolean val = original.call(stack, componentType);
        return val | ModUtils.getLeveledEnchantmentEffectValue(ModEnchantmentEffects.LEVELED_PREVENT_EQUIPMENT_DROP, getWorld(), stack);
    }
}
