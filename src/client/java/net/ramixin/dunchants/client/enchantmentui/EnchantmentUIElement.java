package net.ramixin.dunchants.client.enchantmentui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.client.DungeonEnchantsClient;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.util.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.ramixin.dunchants.client.util.ModClientUtil.*;
import static net.ramixin.dunchants.client.util.ModTextures.*;

public class EnchantmentUIElement {

    private final ItemStack stack;

    private SelectedEnchantments selectedEnchantments;

    private EnchantmentOptions enchantmentOptions;

    private final AbstractUIHoverManager hoverManager;

    private final int[] animationProgresses = new int[]{0,0,0,0,0,0,0,0,0};

    private long previousMillisAtTick = System.currentTimeMillis();

    private static final int millisInAnimation = 144;

    public EnchantmentUIElement(ItemStack stack, AbstractUIHoverManager hoverManager) {
        this.stack = stack;
        this.hoverManager = hoverManager;
        this.selectedEnchantments = stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        this.enchantmentOptions = stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, null);
    }

    public EnchantmentUIElement createCopy(ItemStack stack) {
        EnchantmentUIElement element = new EnchantmentUIElement(stack, hoverManager);
        System.arraycopy(animationProgresses, 0, element.animationProgresses, 0, animationProgresses.length);
        return element;
    }


    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
        if(!canRender()) return;

        long currentMillis = System.currentTimeMillis();
        long millisDiff = currentMillis - previousMillisAtTick;
        previousMillisAtTick = currentMillis;
        for(int i = 0; i < animationProgresses.length; i++)
            if(i == hoverManager.getActiveHoverOption()) animationProgresses[i] = (int) Math.min(animationProgresses[i] + millisDiff, millisInAnimation);
            else animationProgresses[i] = (int) Math.max(animationProgresses[i] - millisDiff, 0);

        List<Runnable> delayedRenderCallbacks = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            renderEnchantmentSlot(context, i, relX, relY, delayedRenderCallbacks::add);
        }
        delayedRenderCallbacks.forEach(Runnable::run);

        int points = DungeonEnchantsClient.getPlayerEntityDuck().dungeonEnchants$getEnchantmentPoints();
        String text = String.valueOf(points);
        int width = textRenderer.getWidth(text);
        int color = hoverManager.setPointsToCustomColor().orElse(0xFF9c50af);
        context.drawText(textRenderer, Text.of(text), relX + 88 - width / 2, relY + 8, color, false);

        hoverManager.render(this, stack, context, textRenderer, mouseX, mouseY, relX, relY);
    }

    private void renderEnchantmentSlot(DrawContext context, int index, int relX, int relY, Consumer<Runnable> delayedRenderCallback) {
        if(enchantmentOptions.isLocked(index)) {
            context.drawGuiTexture(RenderLayer::getGuiTextured, LockedEnchantmentSlot, relX - 1 + 57 * index, relY + 19, 64, 64);
            return;
        }

        if(!selectedEnchantments.hasSelection(index)) {
            renderEnchantmentSlotOptions(context, index, relX, relY, delayedRenderCallback);
            return;
        }

        int enchantIndex = selectedEnchantments.get(index);
        String enchant = enchantmentOptions.get(index).get(enchantIndex);
        Identifier enchantmentId = Identifier.of(enchant);
        RegistryEntry<Enchantment> enchantmentEntry = idToEntry(enchantmentId);
        if(enchantmentEntry == null) return;
        int enchantLevel = getEnchantmentLevel(enchantmentEntry.getKey().orElseThrow(), stack);

        context.drawGuiTexture(RenderLayer::getGuiTextured, selectedEnchantmentBackdrops[enchantLevel-1], relX - 1 + 57 * index, relY + 19, 64, 64);
        SpriteIdentifier spriteId = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, Identifier.of(enchant).withPrefixedPath("large/"));
        if(spriteId.getSprite() == missingIcon.getSprite()) spriteId = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, Identifier.ofVanilla("large/unknown"));
        context.drawSpriteStretched(RenderLayer::getGuiTextured, spriteId.getSprite(), relX - 1 + 57 * index, relY + 19, 64, 64);
    }

    private void renderEnchantmentSlotOptions(DrawContext context, int index, int relX, int relY, Consumer<Runnable> delayedRenderCallback) {
        EnchantmentOption option = enchantmentOptions.get(index);
        context.drawGuiTexture(RenderLayer::getGuiTextured, optionsRomanNumerals[index], relX + 23 + 57 * index, relY + 29, 16, 16);
        for(int l = 0; l < 3; l++)
            renderEnchantmentSlotOption(context, option, index, l, relX, relY, delayedRenderCallback);
    }

    private void renderEnchantmentSlotOption(DrawContext context, EnchantmentOption option, int index, int optionIndex, int relX, int relY, Consumer<Runnable> delayedRenderCallback) {
        int animationIndex = 3 * index + optionIndex;
        int x = (int) (relX + (-21 * Math.pow(optionIndex , 2) + 49 * optionIndex - 15)) + 57 * index;
        int y = (optionIndex == 2 ? 34 : 19) + relY;
        String enchant = option.get(optionIndex);
        boolean grayscale = markAsUnavailable(this, index * 3 + optionIndex, enchant);
        if(animationProgresses[animationIndex] > 0) {
            delayedRenderCallback.accept(() -> {
                context.drawGuiTexture(RenderLayer::getGuiTextured, selectionAnimationTextures[animationProgresses[animationIndex] / (millisInAnimation / 12)], x, y, 64, 64);
                SpriteIdentifier spriteId = getEnchantmentIcon(enchant, animationProgresses[animationIndex] / (millisInAnimation / 12), missingIcon, grayscale);
                context.drawSpriteStretched(RenderLayer::getGuiTextured, spriteId.getSprite(), x, y, 64, 64);
            });
            return;
        }

        if(option.isLocked(optionIndex)) {
            context.drawGuiTexture(RenderLayer::getGuiTextured, lockedEnchantmentOption, x, y, 64, 64);
            return;
        }
        context.drawGuiTexture(RenderLayer::getGuiTextured, enchantmentOptionBackdrop, x, y, 64, 64);
        SpriteIdentifier spriteId = getEnchantmentIcon(enchant, 0, missingIcon, grayscale);
        context.drawSpriteStretched(RenderLayer::getGuiTextured, spriteId.getSprite(), x, y, 64, 64);
    }

    public void updateMousePosition(double x, double y, int relX, int relY) {
        hoverManager.update(this, stack, x, y, relX, relY);
    }

    public int getActiveHoverOption() {
        return hoverManager.getActiveHoverOption();
    }

    private boolean canRender() {
        return enchantmentOptions != null && !ModUtils.hasInvalidOptions(stack, MinecraftClient.getInstance().world);
    }

    public boolean isInvalid(ItemStack stack) {
        return this.stack != stack;
    }

    public void tick(ItemStack stack) {
        this.selectedEnchantments = stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        this.enchantmentOptions = stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, null);
    }

    public SelectedEnchantments getSelectedEnchantments() {
        return selectedEnchantments;
    }

    public EnchantmentOptions getEnchantmentOptions() {
        return enchantmentOptions;
    }

    public Optional<Integer> getPointColor() {
        return hoverManager.setPointsToCustomColor();
    }

    public AbstractUIHoverManager getHoverManager() {
        return hoverManager;
    }
}
