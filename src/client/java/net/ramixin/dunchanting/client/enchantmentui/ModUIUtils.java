package net.ramixin.dunchanting.client.enchantmentui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.ramixin.dunchanting.client.util.ModClientUtils;
import net.ramixin.dunchanting.client.util.TooltipRenderer;
import net.ramixin.dunchanting.util.ModUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface ModUIUtils {

    int MILLIS_IN_ANIMATION = 144;

    static void renderUnavailableTooltip(Holder<Enchantment> enchantment, boolean powerful, TooltipRenderer renderer) {
        Function<Integer, Integer> xOffsetCalculator = width -> -30 - width;
        renderNameText(enchantment, powerful, 1, renderer, false, xOffsetCalculator, true);

        String message = Language.getInstance().getOrDefault("container.enchant.already_exists");
        List<String> wrappedMessage = ModUtil.textWrapString(message, 20);
        List<Component> finalMessage = new ArrayList<>();
        int width = ModUtil.convertStringListToText(wrappedMessage, finalMessage, renderer::getTextWidth, ChatFormatting.RED);
        renderer.render(finalMessage, -30 - width, 1);
    }

    static void renderInfoTooltip(Holder<Enchantment> enchantment, boolean powerful, int level, TooltipRenderer renderer, boolean showDescription, boolean showCost, boolean darkenText, boolean rightAlign, boolean canAffordHoverOption, boolean showEffect, boolean showNameLevel) {
        Function<Integer, Integer> xOffsetCalculator = (width) -> {
            if(!rightAlign) return 0;
            else return -30 - width;
        };

        renderNameText(enchantment, powerful, level, renderer, darkenText, xOffsetCalculator, showNameLevel);

        if(showCost) {
            String costTranslationKey = ModClientUtils.getCostTranslationKey(level, powerful);
            String costText = Language.getInstance().getOrDefault(costTranslationKey);
            int costWidth = renderer.getTextWidth(costText);
            ChatFormatting color = canAffordHoverOption ? ChatFormatting.GREEN : ChatFormatting.RED;
            renderer.render(List.of(Component.literal(costText).withStyle(color)), xOffsetCalculator.apply(costWidth), 1);
        }

        if(showDescription)
            renderDescriptionText(enchantment.unwrapKey().orElseThrow(), renderer, rightAlign);

        if(showEffect)
            renderEffectText(enchantment, renderer, level, darkenText, rightAlign);
    }

    static void renderNameText(Holder<Enchantment> enchantment, boolean powerful, int level, TooltipRenderer renderer, boolean darkenText, Function<Integer, Integer> offsetCalculator, boolean showLevel) {
        Component tempText;
        if(showLevel) tempText = Enchantment.getFullname(enchantment, level);
        else tempText = enchantment.value().description();
        Component enchantmentName;
        if(darkenText) enchantmentName =  tempText.copy().withStyle(!powerful ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA);
        else enchantmentName = tempText;
        int nameWidth = renderer.getTextWidth(enchantmentName);
        renderer.render(List.of(enchantmentName), offsetCalculator.apply(nameWidth), 1);
    }

    static void renderDescriptionText(ResourceKey<Enchantment> enchantmentKey, TooltipRenderer renderer, boolean rightAlign) {
        String descriptionTranslationKey = ModClientUtils.getDescriptionTranslationKey(enchantmentKey, -1);
        Language language = Language.getInstance();
        String description = language.getOrDefault(descriptionTranslationKey, language.getOrDefault("container.enchant.unknown_desc"));
        List<String> rawText = ModUtil.textWrapString(description, 20);
        List<Component> text = new ArrayList<>();
        int width = ModUtil.convertStringListToText(rawText, text, renderer::getTextWidth, ChatFormatting.GRAY);
        renderer.render(text, rightAlign ? -width - 30 : 0, 1);
    }

    static void renderEffectText(Holder<Enchantment> enchantment, TooltipRenderer renderer, int enchantLevel, boolean darken, boolean rightAlign) {
        String effectTranslationKey = ModClientUtils.getDescriptionTranslationKey(enchantment.unwrapKey().orElseThrow(), enchantLevel);
        if(!Language.getInstance().has(effectTranslationKey)) return;
        String effect = Language.getInstance().getOrDefault(effectTranslationKey);
        List<String> rawText = ModUtil.textWrapString(effect, 20);
        List<Component> text = new ArrayList<>();
        int width = ModUtil.convertStringListToText(rawText, text, renderer::getTextWidth, darken ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.WHITE);
        renderer.render(text, rightAlign ? -width - 30: 0, 1);
    }

}
