package net.ramixin.dunchanting.mixins.grindstone;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.ramixin.dunchanting.menus.ModGrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GrindstoneBlock.class)
public class GrindstoneBlockMixin {

    @ModifyReturnValue(method = "method_17469", at = @At(value = "RETURN"))
    private static AbstractContainerMenu redirectScreenCreationBecauseThisMenuIsTooComplexToTryAndUseMixins(
            AbstractContainerMenu original,
            @Local(argsOnly = true) int syncId,
            @Local(argsOnly = true) Inventory inventory,
            @Local(argsOnly = true) Level world,
            @Local(argsOnly = true)BlockPos pos
    ) {

        return new ModGrindstoneMenu(syncId, inventory, ContainerLevelAccess.create(world, pos));
    }

}
