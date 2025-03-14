package net.ramixin.dunchants.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.client.DungeonEnchantsClient;
import net.ramixin.dunchants.client.enchantmentui.EnchantmentUIElement;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.util.ModTags;
import net.ramixin.util.PlayerEntityDuck;

import java.util.Optional;

public interface ModClientUtil {

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

    static SpriteIdentifier getEnchantmentIcon(String id, int index, SpriteIdentifier defaultIcon, boolean grayscale) {
        Identifier enchantId = Identifier.of(id);
        Optional<RegistryEntry.Reference<Enchantment>> enchantment = DungeonEnchantsClient.getEnchantmentRegistry().getEntry(enchantId);
        if(enchantment.isEmpty()) return new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("unknown", index, grayscale));
        SpriteIdentifier spriteId = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId(enchantId.getPath(), index, grayscale));
        if(spriteId.getSprite() == defaultIcon.getSprite()) return new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, getEnchantmentIconId("unknown", index, grayscale));
        return spriteId;
    }

    static Identifier getEnchantmentIconId(String id, int index, boolean grayscale) {
        StringBuilder builder = new StringBuilder();
        if(grayscale) {
            if(index == 0) builder.append("generated/grayscale/small/");
            else builder.append("generated/grayscale/");
        } else {
            if(index == 0) builder.append("small/");
            else builder.append("generated/");
        }
        builder.append(id);
        if(index != 0) builder.append("/").append(index);
        return Identifier.of(builder.toString());
    }

    static int getEnchantmentLevel(RegistryKey<Enchantment> key, ItemStack stack) {
        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        for(RegistryEntry<Enchantment> enchantEntry : enchantments.getEnchantments())
            if(enchantEntry.matchesKey(key))
                return Math.min(3, enchantments.getLevel(enchantEntry));
        return 0;
    }

    static boolean canAfford(RegistryEntry<Enchantment> entry, ItemStack stack) {
        int level = getEnchantmentLevel(entry.getKey().orElseThrow(), stack) + 1;
        if(level > 3) return true;
        boolean powerful = entry.isIn(ModTags.POWERFUL_ENCHANTMENT);
        int required = powerful ? 1 + level : level;
        PlayerEntityDuck duck = DungeonEnchantsClient.getPlayerEntityDuck();
        if (duck.dungeonEnchants$getEnchantmentPoints() >= required) return true;
        assert MinecraftClient.getInstance().player != null;
        return MinecraftClient.getInstance().player.isCreative();
    }

    static boolean markAsUnavailable(EnchantmentUIElement element, int hoveringIndex, String enchant) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int index = hoveringIndex / 3;
        for(int i = 0; i < 3; i++)
            if(selectedEnchantments.hasSelection(i))
                if(i != index && enchantmentOptions.get(i).get(selectedEnchantments.get(i)).equals(enchant)) return true;
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == hoveringIndex) return false;
        Optional<String> hoveringEnchantment = getHoveredEnchantment(element);
        return hoveringEnchantment.map(string -> string.equals(enchant)).orElse(false);

    }
    static Optional<String> getHoveredEnchantment(EnchantmentUIElement element) {
        EnchantmentOptions enchantmentOptions = element.getEnchantmentOptions();
        int hovering = element.getHoverManager().getActiveHoverOption();
        if(hovering == -1) return Optional.empty();
        int index = hovering / 3;
        if(enchantmentOptions.isLocked(index)) return Optional.empty();
        int optionIndex = hovering % 3;
        return enchantmentOptions.get(index).getOptional(optionIndex);
    }



}
