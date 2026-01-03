package net.ramixin.dunchanting.client.enchantmentui.grindstone;

import net.minecraft.ChatFormatting;
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
import net.ramixin.dunchanting.util.ModTags;
import net.ramixin.dunchanting.util.ModUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderInfoTooltip;
import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.renderUnavailableTooltip;

public class GrindstoneHoverManager extends AbstractUIHoverManager {

    private final UUID playerUUID;

    private int activeHoverOption = -1;

    private boolean changePointColor = false;


    public GrindstoneHoverManager(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public void render(AbstractEnchantmentUIElement element, ItemStack stack, GuiGraphics context, Font textRenderer, int mouseX, int mouseY, int relX, int relY) {
        changePointColor = false;
        if(activeHoverOption == -1) return;
        //noinspection DuplicatedCode
        int optionIndex = activeHoverOption % 3;
        int index = activeHoverOption / 3;
        EnchantmentOptions options = element.getEnchantmentOptions();
        if(options.hasEmptySlot(index)) return;
        EnchantmentSlot option = options.getOrThrow(index);
        if(option.isLocked(optionIndex)) return;
        Holder<Enchantment> enchant = option.getOrThrow(optionIndex);
        int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(enchant, stack);
        boolean powerful = enchant.is(ModTags.POWERFUL_ENCHANTMENT);
        TooltipRenderer renderer = new TooltipRenderer(context, textRenderer, mouseX, mouseY);
        if(ModClientUtils.markAsUnavailable(element, activeHoverOption, enchant)) {
            renderUnavailableTooltip(enchant, powerful, renderer);
            return;
        }
        renderInfoTooltip(enchant, powerful, enchantLevel, renderer, true, false, false, false, false, true, true);
        renderer.resetHeight();

        String clickText = Language.getInstance().getOrDefault("container.grindstone.disenchant");
        List<String> rawClickText = ModUtil.textWrapString(clickText, 20);
        List<Component> finalClickText = new ArrayList<>();
        int clickWidth = ModUtil.convertStringListToText(rawClickText, finalClickText, renderer::getTextWidth, ChatFormatting.GREEN);
        renderer.render(finalClickText, -30 - clickWidth, 0);

        int attribution = ModUtil.getAttributionOnItem(playerUUID, stack, index);
        if(attribution <= 0) return;
        changePointColor = true;
        String attributionText = String.format(Language.getInstance().getOrDefault("container.grindstone.attribution"), attribution, attribution == 1 ? "" : "s");
        List<String> rawAttributionText = ModUtil.textWrapString(attributionText, 20);
        List<Component> finalAttributionText = new ArrayList<>();
        int attributionWidth = ModUtil.convertStringListToText(rawAttributionText, finalAttributionText, renderer::getTextWidth, ChatFormatting.LIGHT_PURPLE);
        renderer.render(finalAttributionText, -30 - attributionWidth, 1);
    }

    @Override
    public void update(AbstractEnchantmentUIElement element, ItemStack stack, double mouseX, double mouseY, int relX, int relY) {
        SelectedEnchantments selectedEnchantments = element.getSelectedEnchantments();
        for(int i = 0; i < 3; i++) {
            if(!selectedEnchantments.hasSelection(i)) continue;
            int slotX = relX - 1 + 57 * i;
            int slotY = relY + 19 + 10;
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
