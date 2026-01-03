package net.ramixin.dunchanting.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.ramixin.dunchanting.items.components.Gilded;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import org.jspecify.annotations.NonNull;

public class GildFunction implements LootItemFunction {

    public static final MapCodec<GildFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            ConstantValue.CODEC.fieldOf("chance").forGetter(GildFunction::getChance)
            ).apply(instance, GildFunction::new)
    );

    private final ConstantValue chance;

    protected GildFunction(ConstantValue chance) {
        this.chance = chance;
    }

    @Override
    public @NonNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return ModFunctionTypes.GILD;
    }

    public ConstantValue getChance() {
        return chance;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        RandomSource random = lootContext.getRandom();
        float randomFloat = random.nextFloat();
        if(randomFloat > chance.getFloat(lootContext)) return stack;
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if(enchantments.isEmpty()) return stack;
        Holder<Enchantment> enchantment = enchantments.keySet().iterator().next();
        stack.set(ModDataComponents.GILDED, new Gilded(enchantment));
        return stack;
    }
}
