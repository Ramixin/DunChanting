package net.ramixin.dunchanting.mixins.anvil;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin extends AbstractContainerMenu {

    protected ItemCombinerMenuMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ItemCombinerMenu;addStandardInventorySlots(Lnet/minecraft/world/Container;II)V"))
    private void movePlayerSlots(ItemCombinerMenu instance, Container inventory, int i, int j, Operation<Void> original) {
        int y;
        if(((AbstractContainerMenu)this) instanceof AnvilMenu) y = j + 16;
        else y = j;
        original.call(instance, inventory, i, y);
    }

}
