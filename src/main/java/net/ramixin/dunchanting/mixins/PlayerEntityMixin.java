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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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

    @Shadow public abstract void addExperience(int experience);

    @Shadow public float experienceProgress;
    @Unique
    private int enchantmentPoints;

    @Unique
    private int levelingMetaID = 1; // latest

    @Unique
    private int highestEnchantmentPoints;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    private void changeXpProgression(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(25 * (experienceLevel) + 50);
    }

    @ModifyReturnValue(method = "shouldAlwaysDropExperience", at = @At("RETURN"))
    private boolean changeShouldAlwaysDropXp(boolean original) {
        return true;
    }

    @ModifyReturnValue(method = "getExperienceToDrop", at = @At("RETURN"))
    private int preventXpOrbsFromDropping(int original) {
        return 0;
    }


    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void writeEnchantmentPointsToData(WriteView view, CallbackInfo ci) {
        view.putInt("EnchantmentPoints", enchantmentPoints);
        view.putInt("HighestEnchantmentPoints", highestEnchantmentPoints);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void readEnchantmentPointsFromData(ReadView view, CallbackInfo ci) {

        enchantmentPoints = view.getInt("EnchantmentPoints", 0);
        highestEnchantmentPoints = view.getInt("HighestEnchantmentPoints", 0);
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
        if(serverPlayer.networkHandler == null) return;
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
        return val || ModUtils.getLeveledEnchantmentEffectValue(ModEnchantmentEffects.LEVELED_PREVENT_EQUIPMENT_DROP, getEntityWorld(), stack);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void writeLevelingMetadata(WriteView view, CallbackInfo ci) {
        view.putInt("levelingMetaID", levelingMetaID);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void readLevelingMetadata(ReadView view, CallbackInfo ci) {
        levelingMetaID = view.getInt("levelingMetaID", 0);

        if(levelingMetaID == 0) {
            int points = (int) (12.5 * Math.pow(this.experienceLevel + this.experienceProgress, 2));
            addExperience(points);
            levelingMetaID = 1;
        }
    }
}
