package net.ramixin.dunchanting.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.util.ItemEnchantmentsDuck;
import net.ramixin.dunchanting.util.ModTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEnchantments.class)
public class ItemEnchantmentsMixin implements ItemEnchantmentsDuck {

    @Shadow @Final
    Object2IntOpenHashMap<Holder<Enchantment>> enchantments;

    @Override
    public Object2IntOpenHashMap<Holder<Enchantment>> dungeonEnchants$getEnchantments() {
        return this.enchantments;
    }

    @WrapOperation(method = "addToTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getFullname(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/Component;"))
    private Component changeEnchantmentColorIfGildedEnchantment(Holder<Enchantment> enchantment, int level, Operation<Component> original, @Local(argsOnly = true) DataComponentGetter components) {
        Component text = original.call(enchantment, level);
        Gilded gilded = components.get(ModDataComponents.GILDED);
        if(gilded == null) return text;
        if(!gilded.enchantmentEntry().equals(enchantment)) return text;
        if(enchantment.is(ModTags.POWERFUL_ENCHANTMENT)) return text.copy().withStyle(ChatFormatting.GOLD);
        return text.copy().withStyle(ChatFormatting.YELLOW);
    }

}
