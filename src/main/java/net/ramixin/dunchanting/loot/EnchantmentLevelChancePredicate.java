package net.ramixin.dunchanting.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.math.random.Random;

public record EnchantmentLevelChancePredicate(RegistryEntry<Enchantment> enchantment) implements ComponentSubPredicate<ItemEnchantmentsComponent> {

    private static final Random RANDOM = Random.create();
    public static final Codec<EnchantmentLevelChancePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryFixedCodec.of(RegistryKeys.ENCHANTMENT).fieldOf("enchantment").forGetter(EnchantmentLevelChancePredicate::enchantment)
    ).apply(instance, EnchantmentLevelChancePredicate::new));

    @Override
    public ComponentType<ItemEnchantmentsComponent> getComponentType() {
        return DataComponentTypes.ENCHANTMENTS;
    }

    @Override
    public boolean test(ItemStack stack, ItemEnchantmentsComponent component) {
        int level = component.getLevel(enchantment);
        return level >= RANDOM.nextBetween(1, 3);
    }
}
