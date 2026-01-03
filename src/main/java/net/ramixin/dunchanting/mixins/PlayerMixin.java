package net.ramixin.dunchanting.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.ramixin.dunchanting.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchanting.util.ModUtil;
import net.ramixin.dunchanting.util.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerDuck {

    @Shadow public int experienceLevel;

    @Shadow public abstract void giveExperiencePoints(int experience);

    @Shadow public float experienceProgress;
    @Unique
    private int enchantmentPoints;

    @Unique
    private int levelingMetaID = 1; // latest

    @Unique
    private int highestEnchantmentPoints;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getXpNeededForNextLevel", at = @At("HEAD"), cancellable = true)
    private void changeXpProgression(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(25 * (experienceLevel) + 50);
    }

    @ModifyReturnValue(method = "isAlwaysExperienceDropper", at = @At("RETURN"))
    private boolean changeShouldAlwaysDropXp(boolean original) {
        return false;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeEnchantmentPointsToData(ValueOutput view, CallbackInfo ci) {
        view.putInt("EnchantmentPoints", enchantmentPoints);
        view.putInt("HighestEnchantmentPoints", highestEnchantmentPoints);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readEnchantmentPointsFromData(ValueInput view, CallbackInfo ci) {

        enchantmentPoints = view.getIntOr("EnchantmentPoints", 0);
        highestEnchantmentPoints = view.getIntOr("HighestEnchantmentPoints", 0);
    }

    @Inject(method = "giveExperienceLevels", at = @At("TAIL"))
    private void grantEnchantmentPointOnLevelUp(int levels, CallbackInfo ci) {
        if(levels == 0) return;
        int levelsToAdd;
        if(highestEnchantmentPoints > this.experienceLevel) levelsToAdd = levels - ( this.experienceLevel - highestEnchantmentPoints );
        else levelsToAdd = levels;
        if(levelsToAdd < 0) return;
        enchantmentPoints += levelsToAdd;
        //noinspection ConstantValue
        if(!(((LivingEntity) this) instanceof ServerPlayer serverPlayer)) return;
        if(serverPlayer.connection == null) return;
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



    @WrapOperation(method = "destroyVanishingCursedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;has(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/component/DataComponentType;)Z"))
    private boolean applyLeveledVanishingCurse(ItemStack stack, DataComponentType<?> componentType, Operation<Boolean> original) {
        boolean val = original.call(stack, componentType);
        return val || ModUtil.getLeveledEnchantmentEffectValue(ModEnchantmentEffects.LEVELED_PREVENT_EQUIPMENT_DROP, level(), stack);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeLevelingMetadata(ValueOutput view, CallbackInfo ci) {
        view.putInt("levelingMetaID", levelingMetaID);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLevelingMetadata(ValueInput view, CallbackInfo ci) {
        levelingMetaID = view.getIntOr("levelingMetaID", 0);

        if(levelingMetaID == 0) {
            int points = (int) (12.5 * Math.pow(this.experienceLevel + this.experienceProgress, 2));
            giveExperiencePoints(points);
            levelingMetaID = 1;
        }
    }
}
