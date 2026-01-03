package net.ramixin.dunchanting.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.NonNull;

public record EnchantmentLevelChancePredicate(Holder<Enchantment> enchantment) implements SingleComponentItemPredicate<ItemEnchantments> {

    private static final RandomSource RANDOM = RandomSource.create();
    public static final Codec<EnchantmentLevelChancePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryFixedCodec.create(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(EnchantmentLevelChancePredicate::enchantment)
    ).apply(instance, EnchantmentLevelChancePredicate::new));

    @Override
    public @NonNull DataComponentType<ItemEnchantments> componentType() {
        return DataComponents.ENCHANTMENTS;
    }

    @Override
    public boolean matches(ItemEnchantments component) {
        int level = component.getLevel(enchantment);
        return level >= RANDOM.nextIntBetweenInclusive(1, 3);
    }
}
