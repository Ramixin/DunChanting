package net.ramixin.dunchanting.client.enchantmentui.anvil;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.client.util.ModClientUtils;
import net.ramixin.dunchanting.client.util.TooltipRenderer;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModTags;
import net.ramixin.dunchanting.util.ModUtils;

import java.util.List;
import java.util.Optional;

import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

public class TransferHoverManager extends AbstractUIHoverManager {

    private int activeHoverOption = -1;

    @Override
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
        if(activeHoverOption == -1 || !(element instanceof TransferElement transferElement)) return;
        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.isLocked(index)) return;
        EnchantmentSlot option = options.getOrThrow(index);
        if(option.isLocked(optionIndex)) return;
        RegistryEntry<Enchantment> enchant = option.getOrThrow(optionIndex);
        if(enchant == null) return;
        int enchantLevel = ModUtils.getEnchantmentLevel(enchant, stack);
        boolean powerful = enchant.isIn(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(enchant, powerful, renderer);
            return;
        }
        renderer.render(List.of(Text.translatable("container.anvil.replacing").formatted(Formatting.RED)), 0, 0);
        renderInfoTooltip(enchant, powerful, enchantLevel, renderer, true, false, false, false, false, false, false);
        renderer.resetHeight();

        RegistryEntry<Enchantment> transferSelection = transferElement.getTransferSelection();
        boolean transferPowerful = transferSelection.isIn(ModTags.POWERFUL_ENCHANTMENT);
        renderer.render(List.of(Text.translatable("container.anvil.transferring").formatted(Formatting.GREEN)), - 30 - 68, 0);
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
