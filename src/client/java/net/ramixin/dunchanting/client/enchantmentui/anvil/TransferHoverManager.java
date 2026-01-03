package net.ramixin.dunchanting.client.enchantmentui.anvil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.client.util.ModClientUtils;
import net.ramixin.dunchanting.client.util.TooltipRenderer;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModTags;
import net.ramixin.dunchanting.util.ModUtil;

import java.util.List;
import java.util.Optional;

import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

public class TransferHoverManager extends AbstractUIHoverManager {

    private int activeHoverOption = -1;

    @Override
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, GuiGraphics context, Font textRenderer, int mouseX, int mouseY, int relX, int relY) {
        if(activeHoverOption == -1 || !(element instanceof TransferElement transferElement)) return;
        //noinspection DuplicatedCode
        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.hasEmptySlot(index)) return;
        EnchantmentSlot option = options.getOrThrow(index);
        if(option.isLocked(optionIndex)) return;
        Holder<Enchantment> enchant = option.getOrThrow(optionIndex);
        if(enchant == null) return;
        int enchantLevel = ModUtil.getEnchantmentLevel(enchant, stack);
        boolean powerful = enchant.is(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(enchant, powerful, renderer);
            return;
        }
        renderer.render(List.of(Component.translatable("container.anvil.replacing").withStyle(ChatFormatting.RED)), 0, 0);
        renderInfoTooltip(enchant, powerful, enchantLevel, renderer, true, false, false, false, false, false, false);
        renderer.resetHeight();

        Holder<Enchantment> transferSelection = transferElement.getTransferSelection();
        boolean transferPowerful = transferSelection.is(ModTags.POWERFUL_ENCHANTMENT);
        renderer.render(List.of(Component.translatable("container.anvil.transferring").withStyle(ChatFormatting.GREEN)), - 30 - 68, 0);
        renderInfoTooltip(transferSelection, transferPowerful, 1, renderer, true, false, false, true, false, false, false);
    }

    @Override
    public void update(AbstractEnchantmentUIElement element, ItemStack stack, double mouseX, double mouseY, int relX, int relY) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        for(int i = 0; i < 3; i++) {
            if(selectedEnchantments.hasSelection(i)) continue;
            for (int l = 0; l < 3; l++) {
                int slotX = (int) (relX + (-21 * Math.pow(l, 2) + 49 * l - 15)) + 57 * i;
                int slotY = (l == 2 ? 34 : 19) + relY + 12;
                if(Math.abs(mouseX - slotX - 32) + Math.abs(mouseY - slotY - 32) <= 12) {
                    activeHoverOption = 3 * i + l;
                    return;
                }
            }
        }
        activeHoverOption = -1;
    }

    @Override
    public Optional<Integer> setPointsToCustomColor() {
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
}
