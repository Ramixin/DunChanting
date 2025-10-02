package net.ramixin.dunchanting.client.enchantmentui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.ramixin.dunchanting.client.enchantmentui.ModUIUtils.MILLIS_IN_ANIMATION;
import static net.ramixin.dunchanting.client.util.ModClientUtils.getEnchantmentIcon;
import static net.ramixin.dunchanting.client.util.ModTextures.*;

public abstract class AbstractEnchantmentUIElement {

    private final ItemStack stack;

    private SelectedEnchantments selectedEnchantments;

    private EnchantmentOptions enchantmentOptions;

    private final AbstractUIHoverManager hoverManager;

    private final int[] animationProgresses = new int[9];

    private final boolean[] renderGrayscale = new boolean[9];

    private long previousMillisAtTick = System.currentTimeMillis();

    private final int[] cachedRelatives = new int[2];


    public AbstractEnchantmentUIElement(ItemStack stack, AbstractUIHoverManager hoverManager, int relX, int relY) {
        this.stack = stack;
        this.hoverManager = hoverManager;
        this.enchantmentOptions = generateOptions(stack);
        this.selectedEnchantments = generateSelection(stack);
        this.cachedRelatives[0] = relX;
        this.cachedRelatives[1] = relY;
    }


    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int relX, int relY) {
        if(!canRender()) return;

        long currentMillis = System.currentTimeMillis();
        long millisDiff = currentMillis - previousMillisAtTick;
        previousMillisAtTick = currentMillis;
        if(isAnimated())
            for(int i = 0; i < animationProgresses.length; i++)
                if(i == hoverManager.getActiveHoverOption()) animationProgresses[i] = (int) Math.min(animationProgresses[i] + millisDiff, MILLIS_IN_ANIMATION);
                else animationProgresses[i] = (int) Math.max(animationProgresses[i] - millisDiff, 0);

        List<Runnable> delayedRenderCallbacks = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            renderEnchantmentSlot(context, i, relX, relY, delayedRenderCallbacks::add);
        }
        delayedRenderCallbacks.forEach(Runnable::run);

        hoverManager.render(this, stack, context, textRenderer, mouseX, mouseY, relX, relY);
    }

    private void renderEnchantmentSlot(DrawContext context, int index, int relX, int relY, Consumer<Runnable> delayedRenderCallback) {
        if(enchantmentOptions.isLocked(index)) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, LockedEnchantmentSlot, relX - 1 + 57 * index, relY + 19, 64, 64);
            return;
        }

        if(!selectedEnchantments.hasSelection(index)) {
            renderEnchantmentSlotOptions(context, index, relX, relY, delayedRenderCallback);
            return;
        }

        int enchantIndex = selectedEnchantments.get(index);
        RegistryEntry<Enchantment> enchant = enchantmentOptions.getOrThrow(index).getOrThrow(enchantIndex);
        if(enchant == null) return;
        int enchantLevel = ModUtils.getEnchantmentLevel(enchant, stack);

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, selectedEnchantmentBackdrops[enchantLevel-1], relX - 1 + 57 * index, relY + 19, 64, 64);

        SpriteIdentifier largeEnchant = getEnchantmentIcon(enchant, 0, missingIcon, renderGrayscale[index * 3 + enchantIndex], true, context::getSprite);
        Sprite sprite = context.getSprite(largeEnchant);
        context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprite, relX - 1 + 57 * index, relY + 19, 64, 64);

    }

    private void renderEnchantmentSlotOptions(DrawContext context, int index, int relX, int relY, Consumer<Runnable> delayedRenderCallback) {
        EnchantmentSlot option = enchantmentOptions.getOrThrow(index);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, optionsRomanNumerals[index], relX + 23 + 57 * index, relY + 29, 16, 16);
        for(int l = 0; l < 3; l++)
            renderEnchantmentSlotOption(context, option, index, l, relX, relY, delayedRenderCallback);
    }

    private void renderEnchantmentSlotOption(DrawContext context, EnchantmentSlot option, int index, int optionIndex, int relX, int relY, Consumer<Runnable> delayedRenderCallback) {
        int animationIndex = 3 * index + optionIndex;
        int x = (relX + (-21 * optionIndex * optionIndex + 49 * optionIndex - 15)) + 57 * index;
        int y = (optionIndex == 2 ? 34 : 19) + relY;

        if(option.isLocked(optionIndex)) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lockedEnchantmentOption, x, y, 64, 64);
            return;
        }

        RegistryEntry<Enchantment> enchant = option.getOrThrow(optionIndex);
        boolean grayscale = renderGrayscale[3 * index + optionIndex];
        if(animationProgresses[animationIndex] > 0) {
            delayedRenderCallback.accept(() -> {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, selectionAnimationTextures[animationProgresses[animationIndex] / (MILLIS_IN_ANIMATION / 12)], x, y, 64, 64);
                SpriteIdentifier spriteId = getEnchantmentIcon(enchant, animationProgresses[animationIndex] / (MILLIS_IN_ANIMATION / 12), missingIcon, grayscale, false, context::getSprite);
                Sprite sprite = context.getSprite(spriteId);
                context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprite, x, y, 64, 64);
            });
            return;
        }

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, enchantmentOptionBackdrop, x, y, 64, 64);

        SpriteIdentifier spriteId = getEnchantmentIcon(enchant, 0, missingIcon, grayscale, false, context::getSprite);
        Sprite sprite = context.getSprite(spriteId);
        //GpuTextureView gpuTextureView = MinecraftClient.getInstance().getTextureManager().getTexture(spriteId.getTextureId()).getGlTextureView();
        context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprite, x, y, 64, 64);

    }

    public void updateMousePosition(double x, double y) {
        hoverManager.update(this, stack, x, y, cachedRelatives[0], cachedRelatives[1]);
    }

    public int getActiveHoverOption() {
        return hoverManager.getActiveHoverOption();
    }

    protected boolean canRender() {
        return enchantmentOptions != null && !ModUtils.hasInvalidOptions(stack, MinecraftClient.getInstance().world);
    }

    public boolean isInvalid(ItemStack stack) {
        return this.stack != stack;
    }

    public void tick(ItemStack stack) {
        if(updatesComponents()) {
            this.selectedEnchantments = stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
            this.enchantmentOptions = stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, null);
        }

        if(enchantmentOptions == null) return;
        for(int i = 0; i < 3; i++) {
            if(enchantmentOptions.isLocked(i)) continue;
            EnchantmentSlot option = enchantmentOptions.getOrThrow(i);
            for(int j = 0; j < 3; j++) {
                if(option.isLocked(j)) continue;
                int hoverIndex = i * 3 + j;
                renderGrayscale[hoverIndex] = renderGrayscale(hoverIndex, option.getOrThrow(j));
            }
        }

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

    public abstract boolean renderGrayscale(int hoverIndex, RegistryEntry<Enchantment> enchant);

    public abstract AbstractEnchantmentUIElement createCopy(ItemStack stack);

    public int[] getCachedRelatives() {
        return cachedRelatives;
    }

    public abstract boolean isAnimated();

    protected EnchantmentOptions generateOptions(ItemStack stack) {
        return stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, null);
    }

    protected SelectedEnchantments generateSelection(ItemStack stack) {
        return stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
    }

    public ItemStack getStack() {
        return stack;
    }

    protected abstract boolean updatesComponents();
}