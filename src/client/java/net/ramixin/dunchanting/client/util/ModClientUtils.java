package net.ramixin.dunchanting.client.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.ramixin.dunchanting.client.DunchantingClient;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModUtil;

import java.util.Optional;
import java.util.function.Function;

public interface ModClientUtils {

    static String getDescriptionTranslationKey(ResourceKey<Enchantment> key, int levelVariant) {
        Identifier id = key.identifier();
        String levelAddition = levelVariant > 0 ? "."+levelVariant : "";
        return String.format("enchantment.%s.%s.desc%s", id.getNamespace(), id.getPath(), levelAddition);
    }

    static String getCostTranslationKey(int levelVariant, boolean powerful) {
        String powerAddition = powerful ? "powerful" : "common";
        return String.format("container.enchant.%s.%s", powerAddition, levelVariant);
    }

    static Material getEnchantmentIcon(Holder<Enchantment> entry, int index, Material defaultIcon, boolean grayscale, boolean large, Function<Material, TextureAtlasSprite> spriteResolver) {
        Optional<ResourceKey<Enchantment>> maybeKey = entry.unwrapKey();
        if(maybeKey.isEmpty()) return new Material(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("minecraft:unknown", index, grayscale, large));
        ResourceKey<Enchantment> key = maybeKey.get();
        Material spriteId = new Material(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId(key.identifier().toString(), index, grayscale, large));
        TextureAtlasSprite sprite = spriteResolver.apply(spriteId);
        if(sprite == spriteResolver.apply(defaultIcon)) return new Material(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("minecraft:unknown", index, grayscale, large));
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
        return Identifier.parse(builder.toString());
    }

    static boolean markAsUnavailable(AbstractEnchantmentUIElement element, int hoveringIndex, Holder<Enchantment> enchant) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int index = hoveringIndex / 3;
        if(selectedEnchantments.hasSelection(index)) return false;
        if(ModUtil.isEnchantmentConflicting(selectedEnchantments, enchantmentOptions, index, enchant))
            return true;
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == hoveringIndex) return false;
        Optional<Holder<Enchantment>> hoveringEnchantment = getHoveredEnchantment(element);
        if(hoveringEnchantment.isEmpty()) return false;
        Holder<Enchantment> hoveringEnchant = hoveringEnchantment.get();
        return !Enchantment.areCompatible(enchant, hoveringEnchant);
    }

    static Optional<Holder<Enchantment>> getHoveredEnchantment(AbstractEnchantmentUIElement element) {
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == -1) return Optional.empty();
        int absHovering = Math.abs(hovering);
        int index = absHovering / 3;
        if(enchantmentOptions.hasEmptySlot(index)) return Optional.empty();
        int optionIndex = absHovering % 3;
        return enchantmentOptions.getOrThrow(index).getOptional(optionIndex);
    }
}
