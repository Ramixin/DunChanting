package net.ramixin.dunchants.client.enchantmentui.anvil;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchants.client.util.ModClientUtils;
import net.ramixin.dunchants.client.util.TooltipRenderer;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.dunchants.util.ModTags;
import net.ramixin.dunchants.util.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.ramixin.dunchants.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchants.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

public class EnchantedBookHoverManager extends AbstractUIHoverManager {

    private int activeHoverOption = -1;

    @Override
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
        if(activeHoverOption == -1 || !(element instanceof EnchantedBookElement enchantedBookElement)) return;
        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.isLocked(index)) return;
        EnchantmentOption option = options.get(index);
        if(option.isLocked(optionIndex)) return;
        String enchant = option.get(optionIndex);
        Identifier enchantId = Identifier.of(enchant);
        RegistryEntry<Enchantment> entry = ModClientUtils.idToEntry(enchantId);
        if(entry == null) return;
        int enchantLevel = ModUtils.getEnchantmentLevel(entry, stack);
        boolean powerful = entry.isIn(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(entry, powerful, renderer);
            return;
        }
        renderInfoTooltip(entry, powerful, enchantLevel, renderer, true, false, false, false, false, false, false);
        renderer.resetHeight();

        ItemStack primary = enchantedBookElement.getEnchantableStack();
        if(primary.canBeEnchantedWith(entry, EnchantingContext.ACCEPTABLE)) {
            String clickText = Language.getInstance().get("container.anvil.transfer");
            List<String> rawClickText = ModUtils.textWrapString(clickText, 20);
            List<Text> finalClickText = new ArrayList<>();
            int clickWidth = ModUtils.convertStringListToText(rawClickText, finalClickText, renderer::getTextWidth, Formatting.BLUE);
            renderer.render(finalClickText, -30 - clickWidth, 0);
        } else {
            String clickText = Language.getInstance().get("container.anvil.unsupported");
            List<String> rawClickText = ModUtils.textWrapString(clickText, 20);
            List<Text> finalClickText = new ArrayList<>();
            int clickWidth = ModUtils.convertStringListToText(rawClickText, finalClickText, renderer::getTextWidth, Formatting.RED);
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

    public boolean supports(AbstractEnchantmentUIElement element, ItemStack primary, int hoverIndex) {
        if(hoverIndex == -1) return false;
        int optionIndex = hoverIndex % 3;
        int index = hoverIndex / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.isLocked(index)) return false;
        EnchantmentOption option = options.get(index);
        if(option.isLocked(optionIndex)) return false;
        String enchant = option.get(optionIndex);
        Identifier enchantId = Identifier.of(enchant);
        RegistryEntry<Enchantment> entry = ModClientUtils.idToEntry(enchantId);
        if(entry == null) return false;
        return primary.canBeEnchantedWith(entry, EnchantingContext.ACCEPTABLE);
    }
}
