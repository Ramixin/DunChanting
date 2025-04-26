package net.ramixin.dunchanting.mixins.anvil;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.ramixin.dunchanting.util.ModUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgingScreenHandler.class)
public abstract class ForgingScreenHandlerMixin extends ScreenHandler {

    @Shadow @Final protected ScreenHandlerContext context;

    @Unique
    private int playerLevel = 0;

    protected ForgingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    //? >=1.21.2 {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ForgingScreenHandler;addPlayerSlots(Lnet/minecraft/inventory/Inventory;II)V"))
    private void movePlayerSlots(ForgingScreenHandler instance, Inventory inventory, int i, int j, Operation<Void> original) {
        int y;
        if(((ScreenHandler)this) instanceof AnvilScreenHandler) y = j + 16;
        else y = j;
        original.call(instance, inventory, i, y);
    }
    //?} else {
    /*@WrapOperation(method = "addPlayerInventorySlots", at = @At(value = "NEW", target = "(Lnet/minecraft/inventory/Inventory;III)Lnet/minecraft/screen/slot/Slot;"))
    private Slot movePlayerSlots(Inventory inventory, int index, int x, int y, Operation<Slot> original) {
        return original.call(inventory, index, x, y + 16);
    }
    *///?}


    @Inject(method = "<init>", at = @At("TAIL"))
    private void updatePlayerLevel(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context
            //? >=1.21.2
            , ForgingSlotsManager forgingSlotsManager
            , CallbackInfo ci) {
        playerLevel = playerInventory.player.experienceLevel;
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    private void updateComponentsOnContentChange(Inventory inventory, CallbackInfo ci) {
        //noinspection ConstantValue
        if(!(((ScreenHandler)this) instanceof AnvilScreenHandler)) return;
        ItemStack stack = inventory.getStack(0);
        this.context.run((world, pos) -> {
            if(!stack.isEmpty()) ModUtils.updateOptionsIfInvalid(stack, world, playerLevel);
        });
    }

}
