package net.ramixin.dunchants.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.util.ModUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.ramixin.dunchants.client.ModClientUtil.*;
import static net.ramixin.dunchants.client.ModTextures.*;

public class EnchantmentUIElement {

    private final ItemStack stack;

    private SelectedEnchantments selectedEnchantments;

    private EnchantmentOptions enchantmentOptions;

    private final int[] animationProgresses = new int[]{0,0,0,0,0,0,0,0,0};

    private long previousMillisAtTick = System.currentTimeMillis();

    private int activeHoverOption = -1;

    public EnchantmentUIElement(ItemStack stack) {
        this.stack = stack;
        this.selectedEnchantments = stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        this.enchantmentOptions = stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, null);
    }


    public void render(DrawContext context, int relX, int relY) {
        if(!canRender()) return;
        MutableBoolean turnPointsReduced = new MutableBoolean();
        List<Runnable> delayedRenderCallbacks = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            renderEnchantmentSlot(context, i, relX, relY, turnPointsReduced, delayedRenderCallbacks::add);
        }
        delayedRenderCallbacks.forEach(Runnable::run);
    }

    private void renderEnchantmentSlot(DrawContext context, int index, int relX, int relY, MutableBoolean turnPointsRed, Consumer<Runnable> delayedRenderCallback) {
        if(enchantmentOptions.isLocked(index)) {
            context.drawGuiTexture(RenderLayer::getGuiTextured, LockedEnchantmentSlot, relX - 1 + 57 * index, relY + 19, 64, 64);
            return;
        }

        if(!selectedEnchantments.hasSelection(index)) {
            renderEnchantmentSlotOptions(context, index, relX, relY, turnPointsRed, delayedRenderCallback);
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

        if(activeHoverOption != 3 * index + enchantIndex) return;
        turnPointsRed.setValue(!canAfford(enchantmentEntry, stack) && (MinecraftClient.getInstance().player != null && !MinecraftClient.getInstance().player.isInCreativeMode()));

//        MutableInt startingHeight = new MutableInt(enchantLevel < 3 ? -13 : 0);
//        drawCurrentLevelTooltip(enchantmentEntry, startingHeight, (lines, relativeX, relativeY, mutInt) -> context.drawTooltip(textRenderer, lines, mouseX + relativeX, mouseY + relativeY + mutInt.getAndAdd(lines.size() * 11 + 7)));
//        startingHeight.add(25);
//        drawNextLevelTooltip(enchantmentEntry, startingHeight, false, (lines, relativeX, relativeY, mutInt) -> context.drawTooltip(textRenderer, lines, mouseX + relativeX, mouseY + relativeY + mutInt.getAndAdd(lines.size() * 11 + 7)));
    }

    private void renderEnchantmentSlotOptions(DrawContext context, int index, int relX, int relY, MutableBoolean turnPointsRed, Consumer<Runnable> delayedRenderCallback) {
        EnchantmentOption option = enchantmentOptions.get(index);
        context.drawGuiTexture(RenderLayer::getGuiTextured, optionsRomanNumerals[index], relX + 23 + 57 * index, relY + 29, 16, 16);
        for(int l = 0; l < 3; l++) {
            renderEnchantmentSlotOption(context, option, index, l, relX, relY, delayedRenderCallback);

            if(activeHoverOption != 3 * index + l) continue;
            Identifier enchantId = Identifier.of(option.get(l));
            RegistryEntry<Enchantment> enchantmentEntry = idToEntry(enchantId);
            if(enchantmentEntry == null) continue;

            turnPointsRed.setValue(!canAfford(enchantmentEntry, stack) && (MinecraftClient.getInstance().player != null && !MinecraftClient.getInstance().player.isInCreativeMode()));
            //drawNextLevelTooltip(enchantmentEntry, new MutableInt(), true, (lines, relativeX, relativeY, mutInt) -> context.drawTooltip(textRenderer, lines, mouseX + relativeX, mouseY + relativeY + mutInt.getAndAdd(lines.size() * 11 + 7)));
        }
    }

    private void renderEnchantmentSlotOption(DrawContext context, EnchantmentOption option, int index, int optionIndex, int relX, int relY, Consumer<Runnable> delayedRenderCallback) {
        int animationIndex = 3 * index + optionIndex;
        int x = (int) (relX + (-21 * Math.pow(optionIndex , 2) + 49 * optionIndex - 15)) + 57 * index;
        int y = (optionIndex == 2 ? 34 : 19) + relY;
        if(animationProgresses[animationIndex] > 0) {
            delayedRenderCallback.accept(() -> {
                context.drawGuiTexture(RenderLayer::getGuiTextured, selectionAnimationTextures[animationProgresses[animationIndex] / 20], x, y, 64, 64);
                SpriteIdentifier spriteId = getEnchantmentIcon(option.get(optionIndex), animationProgresses[animationIndex] / 20, missingIcon);
                context.drawSpriteStretched(RenderLayer::getGuiTextured, spriteId.getSprite(), x, y, 64, 64);
            });
            return;
        }

        if(option.isLocked(optionIndex)) {
            context.drawGuiTexture(RenderLayer::getGuiTextured, lockedEnchantmentOption, x, y, 64, 64);
            return;
        }
        context.drawGuiTexture(RenderLayer::getGuiTextured, enchantmentOptionBackdrop, x, y, 64, 64);
        String enchant = option.get(optionIndex);
        SpriteIdentifier spriteId = getEnchantmentIcon(enchant, 0, missingIcon);
        context.drawSpriteStretched(RenderLayer::getGuiTextured, spriteId.getSprite(), x, y, 64, 64);
    }

//    private void drawCurrentLevelTooltip(RegistryEntry<Enchantment> enchantment, MutableInt height, QuadConsumer<List<Text>, Integer, Integer, MutableInt> renderCallback) {
//        int enchantLevel = EnchantmentHelper.getLevel(enchantment, stack);
//        boolean powerful = enchantment.isIn(ModTags.POWERFUL_ENCHANTMENT);
//        Text enchantmentName = Enchantment.getName(enchantment, enchantLevel).copy().formatted(powerful ? Formatting.LIGHT_PURPLE : Formatting.WHITE);
//        renderCallback.accept(List.of(enchantmentName), 0, 0, height);
//
//        renderDescriptionText(enchantment.getKey().orElseThrow(), height, renderCallback);
//
//        renderEffectText(enchantment, renderCallback, enchantLevel, height, false);
//    }
//
//    private void drawNextLevelTooltip(RegistryEntry<Enchantment> enchantment, MutableInt height, boolean showDescription, QuadConsumer<List<Text>, Integer, Integer, MutableInt> renderCallback) {
//        int enchantLevel = EnchantmentHelper.getLevel(enchantment, stack) + 1;
//        if(enchantLevel > 3) return;
//        boolean powerful = enchantment.isIn(ModTags.POWERFUL_ENCHANTMENT);
//        Text enchantmentName;
//        if(powerful) enchantmentName = Enchantment.getName(enchantment, enchantLevel).copy().formatted(Formatting.DARK_PURPLE);
//        else enchantmentName = Enchantment.getName(enchantment, enchantLevel).copy().formatted(Formatting.WHITE);
//
//        renderCallback.accept(List.of(enchantmentName), 0, 0, height);
//
//        if(showDescription) renderDescriptionText(enchantment.getKey().orElseThrow(), height, renderCallback);
//
//        renderEffectText(enchantment, renderCallback, enchantLevel, height, true);
//
//        String costTranslationKey = getCostTranslationKey(enchantLevel, powerful);
//        String costText = Language.getInstance().get(costTranslationKey);
//        Formatting color = canAfford(enchantment) ? Formatting.GREEN : Formatting.RED;
//        renderCallback.accept(List.of(Text.literal(costText).formatted(color)), 0, 0, height);
//    }
//
//    private void renderDescriptionText(RegistryKey<Enchantment> enchantmentKey, MutableInt height, QuadConsumer<List<Text>, Integer, Integer, MutableInt> renderCallback) {
//        String descriptionTranslationKey = getDescriptionTranslationKey(enchantmentKey, -1);
//        String description = Language.getInstance().get(descriptionTranslationKey, "A mysterious enchantment...");
//        List<Text> wrappedDescription = ModUtils.textWrapString(description, 20);
//        renderCallback.accept(wrappedDescription, 0, 1, height);
//    }
//
//    private void renderEffectText(RegistryEntry<Enchantment> enchantment, QuadConsumer<List<Text>, Integer, Integer, MutableInt> renderCallback, int enchantLevel, MutableInt height, boolean darken) {
//        String effectTranslationKey = getDescriptionTranslationKey(enchantment.getKey().orElseThrow(), enchantLevel);
//        if(!Language.getInstance().hasTranslation(effectTranslationKey)) return;
//        String effect = Language.getInstance().get(effectTranslationKey);
//        List<Text> wrappedEffect = ModUtils.textWrapString(effect, 20, darken ? Formatting.LIGHT_PURPLE : Formatting.WHITE);
//        renderCallback.accept(wrappedEffect, 0, 1, height);
//    }

    public void updateMousePosition(double x, double y, int relX, int relY) {
        for(int i = 0; i < 3; i++)
            if(selectedEnchantments.hasSelection(i)) {
                int slotX = relX - 1 + 57 * i;
                int slotY = relY + 19;
                if(Math.abs(x - slotX - 32) + Math.abs(y - slotY - 32) <= 24) {
                    activeHoverOption = 3 * i + selectedEnchantments.get(i);
                    return;
                }
            } else for(int l = 0; l < 3; l++) {
                int slotX = (int) (relX + (-21 * Math.pow(l, 2) + 49 * l - 15)) + 57 * i;
                int slotY = (l == 2 ? 34 : 19) + relY;
                if(Math.abs(x - slotX - 32) + Math.abs(y - slotY - 32) <= 12) {
                    activeHoverOption = 3 * i + l;
                    return;
                }
            }
        activeHoverOption = -1;
    }

    public int getActiveHoverOption() {
        return activeHoverOption;
    }

    private boolean canRender() {
        return enchantmentOptions != null && !ModUtils.hasInvalidOptions(stack, MinecraftClient.getInstance().world);
    }

    public boolean isInvalid(ItemStack stack) {
        return this.stack != stack;
    }

    public void tick(ItemStack stack) {
        long currentMillis = System.currentTimeMillis();
        long millisDiff = currentMillis - previousMillisAtTick;
        previousMillisAtTick = currentMillis;
        this.selectedEnchantments = stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        this.enchantmentOptions = stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, null);

        for(int i = 0; i < animationProgresses.length; i++)
            if(i == activeHoverOption) animationProgresses[i] = (int) Math.min(animationProgresses[i] + millisDiff, 240);
            else animationProgresses[i] = (int) Math.max(animationProgresses[i] - millisDiff, 0);
    }

}
