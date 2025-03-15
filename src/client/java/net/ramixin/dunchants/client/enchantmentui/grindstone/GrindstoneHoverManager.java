package net.ramixin.dunchants.client.enchantmentui.grindstone;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.client.enchantmentui.AbstractUIHoverManager;
import net.ramixin.dunchants.client.util.ModClientUtils;
import net.ramixin.dunchants.client.util.TooltipRenderer;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.util.ModTags;
import net.ramixin.util.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.ramixin.dunchants.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchants.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

public class GrindstoneHoverManager extends AbstractUIHoverManager {

    private final UUID playerUUID;

    private int activeHoverOption = -1;

    private boolean changePointColor = false;


    public GrindstoneHoverManager(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
        changePointColor = false;
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
        boolean powerful = entry.isIn(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(entry, powerful, renderer);
            return;
        }
        renderInfoTooltip(entry, powerful, enchantLevel, renderer, true, false, false, false, false);
        renderer.resetHeight();

        String clickText = "Click to disenchant";
        List<String> rawClickText = ModUtils.textWrapString(clickText, 20);
        List<Text> finalClickText = new ArrayList<>();
        int clickWidth = ModUtils.convertStringListToText(rawClickText, finalClickText, renderer::getTextWidth, Formatting.GREEN);
        renderer.render(finalClickText, -30 - clickWidth, 0);

        int attribution = ModUtils.getAttributionOnItem(playerUUID, stack, index);
        if(attribution <= 0) return;
        changePointColor = true;
        String attributionText = String.format("+%s Enchantment Points", attribution);
        List<String> rawAttributionText = ModUtils.textWrapString(attributionText, 20);
        List<Text> finalAttributionText = new ArrayList<>();
        int attributionWidth = ModUtils.convertStringListToText(rawAttributionText, finalAttributionText, renderer::getTextWidth, Formatting.LIGHT_PURPLE);
        renderer.render(finalAttributionText, -30 - attributionWidth, 1);
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
        return changePointColor ? Optional.of(0x007700) : Optional.empty();
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
