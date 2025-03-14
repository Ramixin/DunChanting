package net.ramixin.dunchants.client.enchantmentui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.ramixin.dunchants.client.util.ModClientUtil;
import net.ramixin.dunchants.client.util.TooltipRenderer;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.util.ModTags;
import net.ramixin.util.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class EnchantmentTableHoverManager extends AbstractUIHoverManager {

    public int activeHoverOption = -1;

    public boolean canAffordHoverOption = true;

    @Override
    public Optional<Integer> setPointsToCustomColor() {
        if(!canAffordHoverOption) return Optional.of(0xFFAA0000);
        return Optional.empty();
    }

    @Override
    public int getActiveHoverOption() {
        return activeHoverOption;
    }

    @Override
    public void update(EnchantmentUIElement element, ItemStack stack, double mouseX, double mouseY, int relX, int relY) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        for(int i = 0; i < 3; i++)
            if(selectedEnchantments.hasSelection(i)) {
                int slotX = relX - 1 + 57 * i;
                int slotY = relY + 19;
                if(Math.abs(mouseX - slotX - 32) + Math.abs(mouseY - slotY - 32) <= 24) {
                    activeHoverOption = 3 * i + selectedEnchantments.get(i);
                    return;
                }
            } else for(int l = 0; l < 3; l++) {
                int slotX = (int) (relX + (-21 * Math.pow(l, 2) + 49 * l - 15)) + 57 * i;
                int slotY = (l == 2 ? 34 : 19) + relY;
                if(Math.abs(mouseX - slotX - 32) + Math.abs(mouseY - slotY - 32) <= 12) {
                    activeHoverOption = 3 * i + l;
                    return;
                }
            }
        activeHoverOption = -1;
    }

    @Override
    public void render(EnchantmentUIElement element, ItemStack stack, DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
        canAffordHoverOption = true;
        if(activeHoverOption == -1) return;
        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.isLocked(index)) return;
        EnchantmentOption option = options.get(index);
        if(option.isLocked(optionIndex)) return;
        String enchant = option.get(optionIndex);
        Identifier enchantId = Identifier.of(enchant);
        RegistryKey<Enchantment> enchantmentKey = RegistryKey.of(RegistryKeys.ENCHANTMENT, enchantId);
        int enchantLevel = ModClientUtil.getEnchantmentLevel(enchantmentKey, stack);
        RegistryEntry<Enchantment> entry = ModClientUtil.idToEntry(enchantId);
        if(entry == null) return;
        canAffordHoverOption = ModClientUtil.canAfford(entry, stack);
        boolean powerful = entry.isIn(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtil.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(entry, powerful, renderer);
            return;
        }
        if(element.getSelectedEnchantments().hasSelection(index)) renderInfoTooltip(entry, powerful, enchantLevel, renderer, true, false, false, false);
        if(enchantLevel >= 3) return;
        renderer.resetHeight();
        boolean isFirstLevel = enchantLevel == 0;
        renderInfoTooltip(entry, powerful, enchantLevel + 1, renderer, isFirstLevel, true, true, true);
    }

    private void renderUnavailableTooltip(RegistryEntry<Enchantment> enchantment, boolean powerful, TooltipRenderer renderer) {
        Function<Integer, Integer> xOffsetCalculator = width -> -30 - width;
        renderNameText(enchantment, powerful, 1, renderer, false, xOffsetCalculator);

        String message = Language.getInstance().get("container.enchant.already_exists");
        List<String> wrappedMessage = ModUtils.textWrapString(message, 20);
        List<Text> finalMessage = new ArrayList<>();
        int width = ModUtils.convertStringListToText(wrappedMessage, finalMessage, renderer::getTextWidth, Formatting.RED);
        renderer.render(finalMessage, -30 - width, 1);
    }

    private void renderInfoTooltip(RegistryEntry<Enchantment> enchantment, boolean powerful, int level, TooltipRenderer renderer, boolean showDescription, boolean showCost, boolean darkenText, boolean rightAlign) {
        Function<Integer, Integer> xOffsetCalculator = (width) -> {
            if(!rightAlign) return 0;
            else return -30 - width;
        };

        renderNameText(enchantment, powerful, level, renderer, darkenText, xOffsetCalculator);

        if(showCost) {
            String costTranslationKey = ModClientUtil.getCostTranslationKey(level, powerful);
            String costText = Language.getInstance().get(costTranslationKey);
            int costWidth = renderer.getTextWidth(costText);
            Formatting color = canAffordHoverOption ? Formatting.GREEN : Formatting.RED;
            renderer.render(List.of(Text.literal(costText).formatted(color)), xOffsetCalculator.apply(costWidth), 1);
        }

        if(showDescription)
            renderDescriptionText(enchantment.getKey().orElseThrow(), renderer, rightAlign);

        renderEffectText(enchantment, renderer, level, darkenText, rightAlign);
    }

    private void renderNameText(RegistryEntry<Enchantment> enchantment, boolean powerful, int level, TooltipRenderer renderer, boolean darkenText, Function<Integer, Integer> offsetCalculator) {
        Formatting nameColor;
        if(powerful)
            nameColor = darkenText ? Formatting.DARK_PURPLE : Formatting.LIGHT_PURPLE;
        else
            nameColor = darkenText ? Formatting.DARK_AQUA : Formatting.AQUA;
        Text enchantmentName = Enchantment.getName(enchantment, level).copy().formatted(nameColor);
        int nameWidth = renderer.getTextWidth(enchantmentName);
        renderer.render(List.of(enchantmentName), offsetCalculator.apply(nameWidth), 1);
    }

    private void renderDescriptionText(RegistryKey<Enchantment> enchantmentKey, TooltipRenderer renderer, boolean rightAlign) {
        String descriptionTranslationKey = ModClientUtil.getDescriptionTranslationKey(enchantmentKey, -1);
        Language language = Language.getInstance();
        String description = language.get(descriptionTranslationKey, language.get("container.enchant.unknown_desc"));
        List<String> rawText = ModUtils.textWrapString(description, 20);
        List<Text> text = new ArrayList<>();
        int width = ModUtils.convertStringListToText(rawText, text, renderer::getTextWidth, Formatting.GRAY);
        renderer.render(text, rightAlign ? -width - 30 : 0, 1);
    }

    private void renderEffectText(RegistryEntry<Enchantment> enchantment, TooltipRenderer renderer, int enchantLevel, boolean darken, boolean rightAlign) {
        String effectTranslationKey = ModClientUtil.getDescriptionTranslationKey(enchantment.getKey().orElseThrow(), enchantLevel);
        if(!Language.getInstance().hasTranslation(effectTranslationKey)) return;
        String effect = Language.getInstance().get(effectTranslationKey);
        List<String> rawText = ModUtils.textWrapString(effect, 20);
        List<Text> text = new ArrayList<>();
        int width = ModUtils.convertStringListToText(rawText, text, renderer::getTextWidth, darken ? Formatting.LIGHT_PURPLE : Formatting.WHITE);
        renderer.render(text, rightAlign ? -width - 30: 0, 1);
    }
}
