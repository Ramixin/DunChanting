package net.ramixin.dunchanting.client.util;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.ramixin.dunchanting.client.DunchantingClient;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModUtils;

import java.util.Optional;
import java.util.function.Function;

public interface ModClientUtils {

    static String getDescriptionTranslationKey(RegistryKey<Enchantment> key, int levelVariant) {
        Identifier id = key.getValue();
        String levelAddition = levelVariant > 0 ? "."+levelVariant : "";
        return String.format("enchantment.%s.%s.desc%s", id.getNamespace(), id.getPath(), levelAddition);
    }

    static String getCostTranslationKey(int levelVariant, boolean powerful) {
        String powerAddition = powerful ? "powerful" : "common";
        return String.format("container.enchant.%s.%s", powerAddition, levelVariant);
    }

    static SpriteIdentifier getEnchantmentIcon(RegistryEntry<Enchantment> entry, int index, SpriteIdentifier defaultIcon, boolean grayscale, boolean large, Function<SpriteIdentifier, Sprite> spriteResolver) {
        Optional<RegistryKey<Enchantment>> maybeKey = entry.getKey();
        if(maybeKey.isEmpty()) return new SpriteIdentifier(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("minecraft:unknown", index, grayscale, large));
        RegistryKey<Enchantment> key = maybeKey.get();
        SpriteIdentifier spriteId = new SpriteIdentifier(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId(key.getValue().toString(), index, grayscale, large));
        Sprite sprite = spriteResolver.apply(spriteId);
        if(sprite == spriteResolver.apply(defaultIcon)) return new SpriteIdentifier(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("minecraft:unknown", index, grayscale, large));
        return spriteId;
    }

    static Identifier getEnchantmentIconId(String id, int index, boolean grayscale, boolean large) {
        String[] splits = id.split(":");
        StringBuilder builder = new StringBuilder(splits[0]);
        builder.append(':');
        if(grayscale) {
            if(large) builder.append("generated/grayscale/large/");
            else if(index == 0) builder.append("generated/grayscale/small/");
            else builder.append("generated/grayscale/");
        } else {
            if(large) builder.append("large/");
            else if(index == 0) builder.append("small/");
            else builder.append("generated/");
        }
        builder.append(splits[1]);
        if(index != 0) builder.append("/").append(index);
        return Identifier.of(builder.toString());
    }

    static boolean markAsUnavailable(AbstractEnchantmentUIElement element, int hoveringIndex, RegistryEntry<Enchantment> enchant) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int index = hoveringIndex / 3;
        if(selectedEnchantments.hasSelection(index)) return false;
        if(ModUtils.isEnchantmentConflicting(selectedEnchantments, enchantmentOptions, index, enchant))
            return true;
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == hoveringIndex) return false;
        Optional<RegistryEntry<Enchantment>> hoveringEnchantment = getHoveredEnchantment(element);
        if(hoveringEnchantment.isEmpty()) return false;
        RegistryEntry<Enchantment> hoveringEnchant = hoveringEnchantment.get();
        return !Enchantment.canBeCombined(enchant, hoveringEnchant);
    }

    static Optional<RegistryEntry<Enchantment>> getHoveredEnchantment(AbstractEnchantmentUIElement element) {
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == -1) return Optional.empty();
        int absHovering = Math.abs(hovering);
        int index = absHovering / 3;
        if(enchantmentOptions.isLocked(index)) return Optional.empty();
        int optionIndex = absHovering % 3;
        return enchantmentOptions.getOrThrow(index).getOptional(optionIndex);
    }
}
