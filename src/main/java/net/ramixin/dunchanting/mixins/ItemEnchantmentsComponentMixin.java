package net.ramixin.dunchanting.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import net.ramixin.dunchanting.util.ItemEnchantmentsComponentDuck;
import net.ramixin.dunchanting.util.ModTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEnchantmentsComponent.class)
public class ItemEnchantmentsComponentMixin implements ItemEnchantmentsComponentDuck {

    @Shadow @Final
    Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments;

    @Override
    public Object2IntOpenHashMap<RegistryEntry<Enchantment>> dungeonEnchants$getEnchantments() {
        return this.enchantments;
    }

    @WrapOperation(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getName(Lnet/minecraft/registry/entry/RegistryEntry;I)Lnet/minecraft/text/Text;"))
    private Text changeEnchantmentColorIfGildedEnchantment(RegistryEntry<Enchantment> enchantment, int level, Operation<Text> original, @Local(argsOnly = true) ComponentsAccess components) {
        Text text = original.call(enchantment, level);
        Gilded gilded = components.get(ModItemComponents.GILDED);
        if(gilded == null) return text;
        if(!gilded.enchantmentEntry().equals(enchantment)) return text;
        if(enchantment.isIn(ModTags.POWERFUL_ENCHANTMENT)) return text.copy().formatted(Formatting.GOLD);
        return text.copy().formatted(Formatting.YELLOW);
    }

}
