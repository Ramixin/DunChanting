package net.ramixin.dunchanting.client.enchantmentui.anvil;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

public class EnchantedBookHoverManager extends AbstractUIHoverManager {

    private int activeHoverOption = -1;

    @Override
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, GuiGraphics context, Font textRenderer, int mouseX, int mouseY, int relX, int relY) {
        if(activeHoverOption == -1 || !(element instanceof EnchantedBookElement enchantedBookElement)) return;
        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.hasEmptySlot(index)) return;
        EnchantmentSlot option = options.getOrThrow(index);
        if(option.isLocked(optionIndex)) return;
        Holder<Enchantment> enchant = option.getOrThrow(optionIndex);
        int enchantLevel = ModUtil.getEnchantmentLevel(enchant, stack);
        boolean powerful = enchant.is(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(enchant, powerful, renderer);
            return;
        }
        renderInfoTooltip(enchant, powerful, enchantLevel, renderer, true, false, false, false, false, false, false);
        renderer.resetHeight();

        ItemStack primary = enchantedBookElement.getEnchantableStack();
        if(primary.canBeEnchantedWith(enchant, EnchantingContext.ACCEPTABLE)) {
            String clickText = Language.getInstance().getOrDefault("container.anvil.transfer");
            List<String> rawClickText = ModUtil.textWrapString(clickText, 20);
            List<Component> finalClickText = new ArrayList<>();
            int clickWidth = ModUtil.convertStringListToText(rawClickText, finalClickText, renderer::getTextWidth, ChatFormatting.BLUE);
            renderer.render(finalClickText, -30 - clickWidth, 0);
        } else {
            String clickText = Language.getInstance().getOrDefault("container.anvil.unsupported");
            List<String> rawClickText = ModUtil.textWrapString(clickText, 20);
            List<Component> finalClickText = new ArrayList<>();
            int clickWidth = ModUtil.convertStringListToText(rawClickText, finalClickText, renderer::getTextWidth, ChatFormatting.RED);
            renderer.render(finalClickText, -30 - clickWidth, 0);
        }


    }

    @Override
    public void update(AbstractEnchantmentUIElement element, ItemStack stack, double mouseX, double mouseY, int relX, int relY) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        for(int i = 0; i < 3; i++) {
            if(!selectedEnchantments.hasSelection(i)) continue;
            int slotX = relX - 1 + 57 * i;
            int slotY = relY + 19;
            if(Math.abs(mouseX - slotX - 32) + Math.abs(mouseY - slotY - 32) <= 24) {
                activeHoverOption = 3 * i + selectedEnchantments.get(i);
                return;
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
