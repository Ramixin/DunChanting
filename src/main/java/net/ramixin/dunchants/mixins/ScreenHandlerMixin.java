package net.ramixin.dunchants.mixins;

import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @ModifyArg(method = "addPlayerInventorySlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;<init>(Lnet/minecraft/inventory/Inventory;III)V"), index = 3)
    private int moveInventorySlotsDownInEnchantingScreen(int y) {
        if(((ScreenHandler)(Object)this) instanceof EnchantmentScreenHandler) return y + 8;
        return y;
    }

    @ModifyArg(method = "addPlayerHotbarSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;<init>(Lnet/minecraft/inventory/Inventory;III)V"), index = 3)
    private int moveHotBarSlotsDownInEnchantingScreen(int y) {
        if(((ScreenHandler)(Object)this) instanceof EnchantmentScreenHandler) return y + 8;
        return y;
    }

}
