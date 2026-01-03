package net.ramixin.dunchanting.client.enchantmentui.etable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.client.util.ModClientUtils;
import net.ramixin.dunchanting.client.util.TooltipRenderer;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.EnchantmentOptionsUtil;
import net.ramixin.dunchanting.util.ModTags;
import net.ramixin.dunchanting.util.ModUtil;
import net.ramixin.dunchanting.util.PlayerDuck;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

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
    public void cancelActiveHover() {
        activeHoverOption = -1;
    }

    @Override
    public void update(AbstractEnchantmentUIElement element, ItemStack stack, double mouseX, double mouseY, int relX, int relY) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        EnchantmentOptions options = element.getEnchantmentOptions();
        for(int i = 0; i < 3; i++)
            if(selectedEnchantments.hasSelection(i) || (options != null && options.hasEmptySlot(i))) {
                int slotX = relX - 1 + 57 * i;
                int slotY = relY + 19;
                if(Math.abs(mouseX - slotX - 32) + Math.abs(mouseY - slotY - 32) <= 24) {
                    if(options != null && options.hasEmptySlot(i))
                        activeHoverOption = -3 * i;
                    else activeHoverOption = 3 * i + selectedEnchantments.get(i);
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
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, GuiGraphics context, Font textRenderer, int mouseX, int mouseY, int relX, int relY) {
        canAffordHoverOption = true;
        if(activeHoverOption == -1) return;

        if(activeHoverOption < 0) {
            TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);

            EnchantmentOptions lockedOptions = EnchantmentOptionsUtil.getLocked(stack);
            if(lockedOptions.hasEmptySlot(activeHoverOption / -3)) {
                Language language = Language.getInstance();
                String translatedText = language.getOrDefault("container.enchant.perm_locked");
                List<String> wrappedText = ModUtil.textWrapString(translatedText, 20);
                List<Component> text = new ArrayList<>();
                ModUtil.convertStringListToText(wrappedText, text, renderer::getTextWidth, ChatFormatting.RED);
                renderer.render(text, 0, 0);
            } else {
                renderer.render(List.of(Component.translatable("container.enchant.locked").withStyle(ChatFormatting.RED)), 0, 0);
                renderer.resetHeight();

                String unlock = Language.getInstance().getOrDefault("container.enchant.unlock");
                int unlockWidth = renderer.getTextWidth(unlock);
                renderer.render(List.of(Component.literal(unlock)), -30 - unlockWidth, 1);


                PlayerDuck duck = PlayerDuck.get(Minecraft.getInstance().player);
                if(duck == null) return;
                boolean canAfford = duck.dungeonEnchants$getEnchantmentPoints() >= 1;
                String cost = Language.getInstance().getOrDefault("container.enchant.common.1");
                int costWidth = renderer.getTextWidth(cost);
                renderer.render(List.of(Component.literal(cost).withStyle(canAfford ? ChatFormatting.GREEN : ChatFormatting.RED)), -30 - costWidth, 1);
            }



            return;
        }

        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.hasEmptySlot(index)) return;
        EnchantmentSlot option = options.getOrThrow(index);
        if(option.isLocked(optionIndex)) return;
        Holder<Enchantment> enchant = option.getOrThrow(optionIndex);
        int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(enchant, stack);
        canAffordHoverOption = ModUtil.canAfford(enchant, stack, Minecraft.getInstance().player) || enchantLevel >= 3;
        boolean powerful = enchant.is(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(enchant, powerful, renderer);
            return;
        }
        if(element.getSelectedEnchantments().hasSelection(index))
            renderInfoTooltip(enchant, powerful, enchantLevel, renderer, true, false, false, false, canAffordHoverOption, true, true);
        if(enchantLevel >= enchant.value().getMaxLevel()) return;
        renderer.resetHeight();
        boolean isFirstLevel = enchantLevel == 0;
        renderInfoTooltip(enchant, powerful, enchantLevel + 1, renderer, isFirstLevel, true, true, true, canAffordHoverOption, true, true);
    }

}
