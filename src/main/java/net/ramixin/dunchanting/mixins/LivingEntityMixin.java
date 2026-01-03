package net.ramixin.dunchanting.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {


    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "wasExperienceConsumed", at = @At("RETURN"))
    private boolean preventXpDrop(boolean original) {
        if((Entity)this instanceof Player)
            return true;
        return original;
    }

    @ModifyReturnValue(method = "shouldDropExperience", at = @At("RETURN"))
    private boolean ISaidDoNotDropTheXp(boolean original) {
        if((Entity)this instanceof Player)
            return false;
        return original;
    }
}
