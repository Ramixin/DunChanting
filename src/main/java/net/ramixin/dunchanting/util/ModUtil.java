package net.ramixin.dunchanting.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.enchantments.LeveledEnchantmentEffect;
import net.ramixin.dunchanting.items.components.*;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.ramixin.dunchanting.util.ModMath.textWrapScore;

public interface ModUtil {

    static void generateComponents(ItemStack stack, Level level, int playerLevel) {
        EnchantmentOptions lockedOptions = EnchantmentOptionsUtil.generateLocked(stack, level, playerLevel);
        stack.set(ModDataComponents.LOCKED_ENCHANTMENT_OPTIONS, lockedOptions);
        EnchantmentOptions unlockedOptions = EnchantmentOptionsUtil.generateUnlocked(stack, level, playerLevel);
        stack.set(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS, unlockedOptions);
        EnchantmentOptionsUtil.updateLockedAfterUpdatingUnlocked(stack);
        SelectedEnchantments selected = SelectedEnchantmentsUtil.generate(stack);
        stack.set(ModDataComponents.SELECTED_ENCHANTMENTS, selected);
    }

    static boolean getLeveledEnchantmentEffectValue(DataComponentType<LeveledEnchantmentEffect> type, Level world, ItemStack stack) {
        ItemEnchantments itemEnchantmentsComponent = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for(Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantmentsComponent.entrySet())
            if(EnchantmentDuck.get(entry.getKey().value()).dungeonEnchants$getLeveledEffectResult(type, world, entry.getIntValue())) return true;
        return false;
    }

    static SelectedEnchantments getSelectedEnchantments(ItemStack stack) {
        SelectedEnchantments selectedEnchantments = stack.get(ModDataComponents.SELECTED_ENCHANTMENTS);
        if(selectedEnchantments == null) return SelectedEnchantments.DEFAULT;
        else return selectedEnchantments;
    }


    static void updateAttributions(ItemStack stack, int id, int cost, Player player) {
        if(!stack.has(ModDataComponents.ATTRIBUTIONS)) stack.set(ModDataComponents.ATTRIBUTIONS, new Attributions());
        Attributions attributions = stack.get(ModDataComponents.ATTRIBUTIONS);
        if(id == 3)
            //noinspection DataFlowIssue
            attributions.addPersistentAttribute(player.getUUID(), cost);
        else
            //noinspection DataFlowIssue
            attributions.addAttribute(id, player.getUUID(), cost);

        stack.set(ModDataComponents.ATTRIBUTIONS, attributions);
    }

    static void enchantEnchantmentOption(ItemStack stack, int option, int choice) {
        SelectedEnchantments selectedEnchantments = getSelectedEnchantments(stack);
        EnchantmentOptions options = stack.get(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS);
        if(options == null) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment options for {}", stack);
            return;
        }
        Optional<EnchantmentSlot> enchantment = options.getOptional(option);
        if(enchantment.isEmpty()) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment option with index {} for {}", option, stack);
            return;
        }
        Optional<Holder<Enchantment>> enchant = enchantment.get().getOptional(choice);
        if(enchant.isEmpty()) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment in option {} with index {} for {}", option, choice, stack);
            return;
        }
        SelectedEnchantments newSelection = selectedEnchantments.with(option, choice);
        stack.set(ModDataComponents.SELECTED_ENCHANTMENTS, newSelection);
    }

    static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage bufImg) return bufImg;
        BufferedImage bufImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bufImage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bufImage;
    }

    static int manhattanDistance(int x, int y, int x1, int y1) {
        return Math.abs(x - x1) + Math.abs(y - y1);
    }

    static List<String> textWrapString(String text, int tolerance) {
        String[] splitWords = text.split(" ");
        StringBuilder sb = new StringBuilder();
        List<String> result = new ArrayList<>();
        for(String word : splitWords) {
            if(textWrapScore(sb.length(), word.length(), tolerance) < textWrapScore(sb.length(), 0, tolerance)) {
                if(!sb.isEmpty()) sb.append(' ');
                sb.append(word);
            } else {
                result.add(sb.toString());
                sb = new StringBuilder();
                sb.append(word);
            }
        }
        result.add(sb.toString());
        return result;
    }

    static int convertStringListToText(List<String> stringList, List<Component> appendableList, Function<String, Integer> widthCallback, ChatFormatting... formatting) {
        int longest = 0;
        for(String s : stringList) {
            int length = widthCallback.apply(s);
            if(length > longest) longest = length;
            Component text = applyFormatting(s, formatting);
            appendableList.add(text);
        }
        return longest;
    }

    private static Component applyFormatting(String text, ChatFormatting... formatting) {
        MutableComponent mutableText = Component.literal(text);
        for(ChatFormatting format : formatting) mutableText.withStyle(format);
        return mutableText;
    }

    static int getEnchantingCost(Holder<Enchantment> entry, ItemStack stack) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(entry, stack) + 1;
        boolean powerful = entry.is(ModTags.POWERFUL_ENCHANTMENT);
        return powerful ? 1 + level : level;
    }

    static int getEnchantmentLevel(Holder<Enchantment> entry, ItemStack stack) {
        if(stack.is(Items.ENCHANTED_BOOK)) return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(entry);
        else return Math.min(EnchantmentHelper.getItemEnchantmentLevel(entry, stack), 3);
    }

    static boolean canAfford(Holder<Enchantment> entry, ItemStack stack, Player player) {
        int required = getEnchantingCost(entry, stack);
        PlayerDuck duck = PlayerDuck.get(player);
        if (duck.dungeonEnchants$getEnchantmentPoints() >= required) return true;
        return player.isCreative();
    }

    static void applyPointsAndSend(Player player, Consumer<Integer> callback, int val) {
        PlayerDuck duck = PlayerDuck.get(player);
        callback.accept(val);
        if(player instanceof ServerPlayer serverPlayer)
            ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(duck.dungeonEnchants$getEnchantmentPoints()));
    }

    static boolean markAsUnavailable(ItemStack stack, int hoveringIndex, Holder<Enchantment> enchant) {
        SelectedEnchantments selectedEnchantments = stack.getOrDefault(ModDataComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        EnchantmentOptions enchantmentOptions = stack.get(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS);
        if(enchantmentOptions == null) return false;
        int index = hoveringIndex / 3;
        return isEnchantmentConflicting(selectedEnchantments, enchantmentOptions, index, enchant);
    }

    static boolean isEnchantmentConflicting(SelectedEnchantments selectedEnchantments, EnchantmentOptions enchantmentOptions, int index, Holder<Enchantment> enchant) {
        for(int i = 0; i < 3; i++)
            if(selectedEnchantments.hasSelection(i)) {
                Holder<Enchantment> otherEnchant = enchantmentOptions.getOrThrow(i).getOrThrow(selectedEnchantments.get(i));
                if (i == index) return false;
                if(otherEnchant.equals(enchant)) return true;
                if(!Enchantment.areCompatible(enchant, otherEnchant)) return true;
            }
        return false;
    }

    static int getAttributionOnItem(UUID uuid, ItemStack stack, int slotId) {
        Attributions attributions = stack.get(ModDataComponents.ATTRIBUTIONS);
        if(attributions == null) return 0;
        List<AttributionEntry> entries = attributions.get(slotId);
        int count = 0;
        for(AttributionEntry entry : entries)
            if(entry.playerUUID().equals(uuid)) count += entry.points();
        return count;
    }

    static List<Holder<Enchantment>> getOrderedEnchantments(ItemStack stack) {
        ItemEnchantments storedEnchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ObjectIterator<Object2IntMap.Entry<Holder<Enchantment>>> enchantmentsIterator = ItemEnchantmentsDuck.get(storedEnchantments).dungeonEnchants$getEnchantments().object2IntEntrySet().iterator();
        List<Holder<Enchantment>> orderedList = new ArrayList<>();
        enchantmentsIterator.forEachRemaining(v -> orderedList.add(v.getKey()));
        return orderedList;
    }

}
