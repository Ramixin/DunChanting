package net.ramixin.dunchanting.mixins.enchantments;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.ramixin.dunchanting.enchantments.LeveledEnchantmentEffect;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.util.EnchantmentDuck;
import net.ramixin.dunchanting.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements EnchantmentDuck {

    @Shadow public abstract DataComponentMap effects();

    @Override
    public boolean dungeonEnchants$getLeveledEffectResult(DataComponentType<LeveledEnchantmentEffect> type, Level world, int level) {
        if(!this.effects().has(type)) return false;
        return level >= world.getRandom().nextIntBetweenInclusive(1, 3);
    }

    @ModifyReturnValue(method = "canEnchant", at = @At(value = "RETURN"))
    private boolean preventMoreThanThreeEnchantments(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        int max;
        Gilded gilded = itemStack.get(ModDataComponents.GILDED);
        if(gilded == null) max = 3;
        else max = 4;
        return original && itemStack.getEnchantments().keySet().size() < max;
    }

    @ModifyReturnValue(method = "getFullname", at = @At("RETURN"))
    private static Component changeNameColor(Component original, @Local(argsOnly = true) Holder<Enchantment> entry) {
        if(!(original instanceof MutableComponent text)) return original;
        if(entry.is(ModTags.POWERFUL_ENCHANTMENT)) return text.withStyle(ChatFormatting.DARK_PURPLE);
        return text.withStyle(ChatFormatting.DARK_AQUA);
    }

}
