package net.ramixin.dunchants.client.enchantmentui;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import net.ramixin.dunchants.client.util.ModClientUtils;
import net.ramixin.dunchants.client.util.TooltipRenderer;
import net.ramixin.dunchants.util.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface ModUIUtils {

    int MILLIS_IN_ANIMATION = 144;

    static void renderUnavailableTooltip(RegistryEntry<Enchantment> enchantment, boolean powerful, TooltipRenderer renderer) {
        Function<Integer, Integer> xOffsetCalculator = width -> -30 - width;
        renderNameText(enchantment, powerful, 1, renderer, false, xOffsetCalculator, true);

        String message = Language.getInstance().get("container.enchant.already_exists");
        List<String> wrappedMessage = ModUtils.textWrapString(message, 20);
        List<Text> finalMessage = new ArrayList<>();
        int width = ModUtils.convertStringListToText(wrappedMessage, finalMessage, renderer::getTextWidth, Formatting.RED);
        renderer.render(finalMessage, -30 - width, 1);
    }

    static void renderInfoTooltip(RegistryEntry<Enchantment> enchantment, boolean powerful, int level, TooltipRenderer renderer, boolean showDescription, boolean showCost, boolean darkenText, boolean rightAlign, boolean canAffordHoverOption, boolean showEffect, boolean showNameLevel) {
        Function<Integer, Integer> xOffsetCalculator = (width) -> {
            if(!rightAlign) return 0;
            else return -30 - width;
        };

        renderNameText(enchantment, powerful, level, renderer, darkenText, xOffsetCalculator, showNameLevel);

        if(showCost) {
            String costTranslationKey = ModClientUtils.getCostTranslationKey(level, powerful);
            String costText = Language.getInstance().get(costTranslationKey);
            int costWidth = renderer.getTextWidth(costText);
            Formatting color = canAffordHoverOption ? Formatting.GREEN : Formatting.RED;
            renderer.render(List.of(Text.literal(costText).formatted(color)), xOffsetCalculator.apply(costWidth), 1);
        }

        if(showDescription)
            renderDescriptionText(enchantment.getKey().orElseThrow(), renderer, rightAlign);

        if(showEffect)
            renderEffectText(enchantment, renderer, level, darkenText, rightAlign);
    }

    static void renderNameText(RegistryEntry<Enchantment> enchantment, boolean powerful, int level, TooltipRenderer renderer, boolean darkenText, Function<Integer, Integer> offsetCalculator, boolean showLevel) {
        Text tempText;
        if(showLevel) tempText = Enchantment.getName(enchantment, level);
        else tempText = enchantment.value().description();
        Text enchantmentName;
        if(darkenText) enchantmentName =  tempText.copy().formatted(!powerful ? Formatting.LIGHT_PURPLE : Formatting.AQUA);
        else enchantmentName = tempText;
        int nameWidth = renderer.getTextWidth(enchantmentName);
        renderer.render(List.of(enchantmentName), offsetCalculator.apply(nameWidth), 1);
    }

    static void renderDescriptionText(RegistryKey<Enchantment> enchantmentKey, TooltipRenderer renderer, boolean rightAlign) {
        String descriptionTranslationKey = ModClientUtils.getDescriptionTranslationKey(enchantmentKey, -1);
        Language language = Language.getInstance();
        String description = language.get(descriptionTranslationKey, language.get("container.enchant.unknown_desc"));
        List<String> rawText = ModUtils.textWrapString(description, 20);
        List<Text> text = new ArrayList<>();
        int width = ModUtils.convertStringListToText(rawText, text, renderer::getTextWidth, Formatting.GRAY);
        renderer.render(text, rightAlign ? -width - 30 : 0, 1);
    }

    static void renderEffectText(RegistryEntry<Enchantment> enchantment, TooltipRenderer renderer, int enchantLevel, boolean darken, boolean rightAlign) {
        String effectTranslationKey = ModClientUtils.getDescriptionTranslationKey(enchantment.getKey().orElseThrow(), enchantLevel);
        if(!Language.getInstance().hasTranslation(effectTranslationKey)) return;
        String effect = Language.getInstance().get(effectTranslationKey);
        List<String> rawText = ModUtils.textWrapString(effect, 20);
        List<Text> text = new ArrayList<>();
        int width = ModUtils.convertStringListToText(rawText, text, renderer::getTextWidth, darken ? Formatting.LIGHT_PURPLE : Formatting.WHITE);
        renderer.render(text, rightAlign ? -width - 30: 0, 1);
    }

}
