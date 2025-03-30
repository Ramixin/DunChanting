package net.ramixin.dunchants.client.util;

import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.client.DungeonEnchantsClient;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.dunchants.util.ModUtils;

import java.util.Optional;

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

    static RegistryEntry<Enchantment> idToEntry(Identifier id) {
        Registry<Enchantment> enchantmentRegistry = DungeonEnchantsClient.getEnchantmentRegistry();
        Optional<RegistryEntry.Reference<Enchantment>> maybeEnchant = enchantmentRegistry.getEntry(id);
        if(maybeEnchant.isEmpty()) return null;
        RegistryEntry.Reference<Enchantment> enchantmentReference = maybeEnchant.get();
        return enchantmentRegistry.getEntry(enchantmentReference.value());
    }

    static SpriteIdentifier getEnchantmentIcon(String id, int index, SpriteIdentifier defaultIcon, boolean grayscale, boolean large) {
        Identifier enchantId = Identifier.of(id);
        Optional<RegistryEntry.Reference<Enchantment>> enchantment = DungeonEnchantsClient.getEnchantmentRegistry().getEntry(enchantId);
        if(enchantment.isEmpty()) return new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("unknown", index, grayscale, large));
        SpriteIdentifier spriteId = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId(enchantId.getPath(), index, grayscale, large));
        if(spriteId.getSprite() == defaultIcon.getSprite()) return new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("unknown", index, grayscale, large));
        return spriteId;
    }

    static Identifier getEnchantmentIconId(String id, int index, boolean grayscale, boolean large) {
        StringBuilder builder = new StringBuilder();
        if(grayscale) {
            if(large) builder.append("generated/grayscale/large/");
            else if(index == 0) builder.append("generated/grayscale/small/");
            else builder.append("generated/grayscale/");
        } else {
            if(large) builder.append("large/");
            else if(index == 0) builder.append("small/");
            else builder.append("generated/");
        }
        builder.append(id);
        if(index != 0) builder.append("/").append(index);
        return Identifier.of(builder.toString());
    }

    static boolean markAsUnavailable(AbstractEnchantmentUIElement element, int hoveringIndex, String enchant) {
        Registry<Enchantment> registry = DungeonEnchantsClient.getEnchantmentRegistry();
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int index = hoveringIndex / 3;
        if(selectedEnchantments.hasSelection(index)) return false;
        RegistryEntry<Enchantment> enchantValue = registry.getEntry(registry.get(Identifier.of(enchant)));
        if(ModUtils.doSelectionsForbid(enchant, selectedEnchantments, enchantmentOptions, index, registry, enchantValue))
            return true;
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == hoveringIndex) return false;
        Optional<String> hoveringEnchantment = getHoveredEnchantment(element);
        if(hoveringEnchantment.isEmpty()) return false;
        String hoveringEnchant = hoveringEnchantment.get();
        RegistryEntry<Enchantment> hoveringValue = registry.getEntry(registry.get(Identifier.of(hoveringEnchant)));
        return !Enchantment.canBeCombined(enchantValue, hoveringValue);
    }

    static Optional<String> getHoveredEnchantment(AbstractEnchantmentUIElement element) {
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == -1) return Optional.empty();
        int index = hovering / 3;
        if(enchantmentOptions.isLocked(index)) return Optional.empty();
        int optionIndex = hovering % 3;
        return enchantmentOptions.get(index).getOptional(optionIndex);
    }
}
