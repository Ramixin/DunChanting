package net.ramixin.dunchanting.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.ramixin.dunchanting.util.ItemEnchantmentsComponentDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEnchantmentsComponent.class)
public class ItemEnchantmentsComponentMixin implements ItemEnchantmentsComponentDuck {

    @Shadow @Final
    Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchantments;

    @Override
    public Object2IntOpenHashMap<RegistryEntry<Enchantment>> dungeonEnchants$getEnchantments() {
        return this.enchantments;
    }

}
