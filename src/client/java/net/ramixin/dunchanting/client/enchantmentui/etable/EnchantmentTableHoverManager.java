package net.ramixin.dunchanting.client.enchantmentui.etable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import net.ramixin.dunchanting.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchanting.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchanting.client.util.ModClientUtils;
import net.ramixin.dunchanting.client.util.TooltipRenderer;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModTags;
import net.ramixin.dunchanting.util.ModUtils;
import net.ramixin.dunchanting.util.PlayerEntityDuck;

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
            if(selectedEnchantments.hasSelection(i) || (options != null && options.isLocked(i))) {
                int slotX = relX - 1 + 57 * i;
                int slotY = relY + 19;
                if(Math.abs(mouseX - slotX - 32) + Math.abs(mouseY - slotY - 32) <= 24) {
                    if(options != null && options.isLocked(i)) activeHoverOption = -3 * i;
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
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
        canAffordHoverOption = true;
        if(activeHoverOption == -1) return;

        if(activeHoverOption < 0) {
            TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
            renderer.render(List.of(Text.translatable("container.enchant.locked").formatted(Formatting.RED)), 0, 0);
            renderer.resetHeight();
            String unlock = Language.getInstance().get("container.enchant.unlock");
            int unlockWidth = renderer.getTextWidth(unlock);
            renderer.render(List.of(Text.literal(unlock)), -30 - unlockWidth, 1);

            PlayerEntityDuck duck = PlayerEntityDuck.get(MinecraftClient.getInstance().player);
            if(duck == null) return;
            boolean canAfford = duck.dungeonEnchants$getEnchantmentPoints() >= 1;
            String cost = Language.getInstance().get("container.enchant.common.1");
            int costWidth = renderer.getTextWidth(cost);
            renderer.render(List.of(Text.literal(cost).formatted(canAfford ? Formatting.GREEN : Formatting.RED)), -30 - costWidth, 1);

            return;
        }

        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.isLocked(index)) return;
        EnchantmentSlot option = options.getOrThrow(index);
        if(option.isLocked(optionIndex)) return;
        RegistryEntry<Enchantment> enchant = option.getOrThrow(optionIndex);
        int enchantLevel = EnchantmentHelper.getLevel(enchant, stack);
        if(enchant == null) return;
        canAffordHoverOption = ModUtils.canAfford(enchant, stack, MinecraftClient.getInstance().player) || enchantLevel >= 3;
        boolean powerful = enchant.isIn(ModTags.POWERFUL_ENCHANTMENT);
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
