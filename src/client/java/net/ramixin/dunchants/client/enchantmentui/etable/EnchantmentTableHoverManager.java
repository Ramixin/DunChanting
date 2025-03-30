package net.ramixin.dunchants.client.enchantmentui.etable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchants.client.util.ModClientUtils;
import net.ramixin.dunchants.client.util.TooltipRenderer;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.dunchants.util.ModTags;
import net.ramixin.dunchants.util.ModUtils;

import java.util.Optional;

import static net.ramixin.dunchants.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchants.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

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
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
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
        RegistryEntry<Enchantment> entry = ModClientUtils.idToEntry(enchantId);
        int enchantLevel = EnchantmentHelper.getLevel(entry, stack);
        if(entry == null) return;
        canAffordHoverOption = ModUtils.canAfford(entry, stack, MinecraftClient.getInstance().player) || enchantLevel >= 3;
        boolean powerful = entry.isIn(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(entry, powerful, renderer);
            return;
        }
        if(element.getSelectedEnchantments().hasSelection(index)) renderInfoTooltip(entry, powerful, enchantLevel, renderer, true, false, false, false, canAffordHoverOption, true, true);
        if(enchantLevel >= entry.value().getMaxLevel()) return;
        renderer.resetHeight();
        boolean isFirstLevel = enchantLevel == 0;
        renderInfoTooltip(entry, powerful, enchantLevel + 1, renderer, isFirstLevel, true, true, true, canAffordHoverOption, true, true);
    }

}
