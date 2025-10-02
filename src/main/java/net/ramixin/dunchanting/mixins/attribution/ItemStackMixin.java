package net.ramixin.dunchanting.mixins.attribution;

import net.minecraft.component.ComponentHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.ramixin.dunchanting.AttributionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {

    @Inject(method = "onDurabilityChange(ILnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private void attributePointsOnItemBreak(int amount, ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo ci) {
        if(player == null) return;
        AttributionManager.redistribute((ItemStack) (Object)this, player.getEntityWorld());
    }

}
