package net.ramixin.dunchanting.mixins.grindstone;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ramixin.dunchanting.handlers.ModGrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GrindstoneBlock.class)
public class GrindStoneBlockMixin {

    @ModifyReturnValue(method = "method_17469", at = @At(value = "RETURN"))
    private static ScreenHandler redirectScreenCreationBecauseThisMenuIsTooComplexToTryAndUseMixins(
            ScreenHandler original,
            @Local(argsOnly = true) int syncId,
            @Local(argsOnly = true) PlayerInventory inventory,
            @Local(argsOnly = true) World world,
            @Local(argsOnly = true)BlockPos pos
    ) {

        return new ModGrindstoneScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos));
    }

}
