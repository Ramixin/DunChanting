package net.ramixin.dunchanting.mixins.gilded;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin {

    @WrapMethod(method = "getName(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/text/Text;")
    private Text changeEnchantedBookNameIfGildedBook(ItemStack stack, Operation<Text> original) {
        Text name = original.call(stack);
        if(!stack.isOf(Items.ENCHANTED_BOOK)) return name;
        Gilded gilded = stack.get(ModItemComponents.GILDED);
        if(gilded == null) return name;
        return Text.translatable("item.minecraft.gilded_book").formatted(Formatting.GOLD);
    }

}
