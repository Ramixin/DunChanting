package net.ramixin.dunchanting.mixins.gilded;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin {

    @WrapMethod(method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;")
    private Component changeEnchantedBookNameIfGildedBook(ItemStack stack, Operation<Component> original) {
        Component name = original.call(stack);
        Gilded gilded = stack.get(ModDataComponents.GILDED);
        if(gilded == null) return name;
        return Component.translatable("item.dunchanting.gilded_prefix", name).withStyle(ChatFormatting.GOLD);
    }

}
