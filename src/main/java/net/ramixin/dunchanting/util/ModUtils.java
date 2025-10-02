package net.ramixin.dunchanting.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.enchantments.LeveledEnchantmentEffect;
import net.ramixin.dunchanting.items.components.*;
import net.ramixin.dunchanting.payloads.EnchantmentPointsUpdateS2CPayload;
import org.apache.commons.lang3.function.TriFunction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ModUtils {

    static boolean getLeveledEnchantmentEffectValue(ComponentType<LeveledEnchantmentEffect> type, World world, ItemStack stack) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries())
            if(EnchantmentDuck.get(entry.getKey().value()).dungeonEnchants$getLeveledEffectResult(type, world, entry.getIntValue())) return true;
        return false;
    }

    static float oddsOfThirdEnchantmentSlot(int level, int optionIndex) {
        int index = optionIndex * 5;
        if(level > 10 + index) return 1;
        return (float) Math.sin(((level - index) * Math.PI) / 20d);
    }

    static <R> Optional<R> optionalOfEmpty(R val, Function<R, Boolean> eval) {
        if(eval.apply(val)) return Optional.empty();
        return Optional.of(val);
    }

    static boolean hasInvalidOptions(ItemStack stack, World world) {
        EnchantmentOptions options = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(options == null) return true;
        else return options.isInvalid(stack, world);
    }

    static void updateOptionsIfInvalid(ItemStack stack, World world, int playerLevel) {
        if(hasInvalidOptions(stack, world)) generateEnchantmentOptions(stack, world, playerLevel);
    }

    static SelectedEnchantments getSelectedEnchantments(ItemStack stack) {
        SelectedEnchantments selectedEnchantments = stack.get(ModItemComponents.SELECTED_ENCHANTMENTS);
        if(selectedEnchantments == null) return SelectedEnchantments.DEFAULT;
        else return selectedEnchantments;
    }

    static <T, R> T decodeGenericTripleOptionalList(Iterator<R> iter, TriFunction<Optional<R>, Optional<R>, Optional<R>, T> constructor, Function<R, Boolean> evalFunc) {
        return constructor.apply(
                ModUtils.optionalOfEmpty(iter.next(), evalFunc),
                ModUtils.optionalOfEmpty(iter.next(), evalFunc),
                ModUtils.optionalOfEmpty(iter.next(), evalFunc)
        );
    }

    static <R> List<R> encodeGenericTripleOptionalList(Function<Integer, Optional<R>> getter, R defaultVal) {
        return List.of(
                getter.apply(0).orElse(defaultVal),
                getter.apply(1).orElse(defaultVal),
                getter.apply(2).orElse(defaultVal)
        );
    }

    static void generateEnchantmentOptions(ItemStack stack, World world, int playerLevel) {
        List<RegistryEntry<Enchantment>> enchantList = getPossibleEnchantments(world.getRegistryManager(), stack);
        if(enchantList.size() <= 1) return;

        AtomicInteger totalWeight = new AtomicInteger();
        enchantList.forEach(entry -> totalWeight.addAndGet(entry.value().getWeight()));
        Collections.shuffle(enchantList);

        double roll = world.getRandom().nextDouble();
        double odds = (Math.cos(Math.PI * (playerLevel % 5) / 5) - 1) / -2d;
        int enchantmentSlotCount;
        if(playerLevel <= 5) enchantmentSlotCount = 1;
        else if(playerLevel <= 10) enchantmentSlotCount = roll <= odds ? 2 : 1;
        else if(playerLevel <= 15) enchantmentSlotCount = roll <= odds ? 3 : 2;
        else enchantmentSlotCount = 3;

        EnchantmentSlot[] options = new EnchantmentSlot[3];
        Set<RegistryEntry<Enchantment>> discourageList = new HashSet<>();

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        Iterator<RegistryEntry<Enchantment>> enchantmentsIterator = enchantments.getEnchantments().iterator();

        int[] usedEnchantments = new int[]{-1,-1,-1};
        boolean hasEnchantments = false;
        for(int i = 0; i < 3; i++) {
            if(i >= enchantmentSlotCount) break;
            EnchantmentSlot option = generateEnchantmentOption(world.getRandom(), playerLevel, i, discourageList, totalWeight.get(), enchantList);
            if(option == null) continue;
            if(enchantmentsIterator.hasNext()) {
                RegistryEntry<Enchantment> enchant = enchantmentsIterator.next();
                label: {
                    for(int n = 0; n < 3; n++)
                        if(option.getOptional(n).map(enchant::equals).orElse(false)) {
                            usedEnchantments[i] = n;
                            hasEnchantments = true;
                            break label;
                        }
                    usedEnchantments[i] = 0;
                    option = option.withEnchantment(enchant, 0);
                    hasEnchantments = true;
                    discourageList.add(enchant);
                }
            }
            options[i] = option;
        }
        if(hasEnchantments)
            stack.set(ModItemComponents.SELECTED_ENCHANTMENTS,
                    new SelectedEnchantments(
                            usedEnchantments[0] != -1 ? usedEnchantments[0] : null,
                            usedEnchantments[1] != -1 ? usedEnchantments[1] : null,
                            usedEnchantments[1] != -1 ? usedEnchantments[2] : null
                    )
            );
        if(enchantmentsIterator.hasNext()) {
            ItemEnchantmentsComponent.Builder newEnchantsBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            while(enchantmentsIterator.hasNext()) newEnchantsBuilder.remove(val -> val == enchantmentsIterator.next());
            stack.set(DataComponentTypes.ENCHANTMENTS, newEnchantsBuilder.build());
        }

        stack.set(ModItemComponents.ENCHANTMENT_OPTIONS, new EnchantmentOptions(options[0], options[1], options[2]));
    }

    static List<RegistryEntry<Enchantment>> getPossibleEnchantments(DynamicRegistryManager manager, ItemStack stack) {
        List<RegistryEntry<Enchantment>> enchantList = new ArrayList<>();
        for(RegistryEntry<Enchantment> entry : manager
                /*? >=1.21.2 {*/
                .getOrThrow(RegistryKeys.ENCHANTMENT)
                //?} else
                /*.get(RegistryKeys.ENCHANTMENT)*/
                .iterateEntries(EnchantmentTags.IN_ENCHANTING_TABLE))
            if(entry.value().isSupportedItem(stack)) enchantList.add(entry);
        return enchantList;
    }

    private static EnchantmentSlot generateEnchantmentOption(Random random, int playerLevel, int optionIndex, Set<RegistryEntry<Enchantment>> discourageList, int totalWeight, List<RegistryEntry<Enchantment>> enchantList) {
        float enchantRoll = random.nextFloat();
        int optionCount;
        if(enchantRoll < oddsOfThirdEnchantmentSlot(playerLevel, optionIndex)) optionCount = 3;
        else optionCount = 2;
        @SuppressWarnings("unchecked") RegistryEntry<Enchantment>[] enchants = new RegistryEntry[3];
        for(int i = 0; i < optionCount; i++) {
            int rolledWeight = random.nextBetween(1, totalWeight);
            RegistryEntry<Enchantment> entry = getRolledEnchantment(enchantList, rolledWeight, enchants, discourageList);
            if(entry == null) {
                if(i != 2) return null;
            } else {
                discourageList.add(entry);
                enchants[i] = entry;
            }
        }
        return new EnchantmentSlot(enchants[0], enchants[1], enchants[2]);
    }

    static EnchantmentOptions rerollOption(World world, ItemStack stack, EnchantmentOptions options, int playerLevel, int optionIndex) {
        List<RegistryEntry<Enchantment>> enchantList = getPossibleEnchantments(world.getRegistryManager(), stack);
        if(enchantList.size() <= 1) return null;
        AtomicInteger totalWeight = new AtomicInteger();
        enchantList.forEach(entry -> totalWeight.addAndGet(entry.value().getWeight()));
        Collections.shuffle(enchantList);
        Set<RegistryEntry<Enchantment>> discourageList = new HashSet<>();
        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 3; j++)
                if(!options.isLocked(i) && !options.getOrThrow(i).isLocked(j))
                    discourageList.add(options.getOrThrow(i).getOrThrow(j));
        EnchantmentSlot option = generateEnchantmentOption(world.getRandom(), playerLevel, optionIndex, discourageList, totalWeight.get(), enchantList);
        if(option == null) return null;
        return options.withSlot(optionIndex, option);
    }

    static void updateAttributions(ItemStack stack, int id, int cost, PlayerEntity player) {
        if(!stack.contains(ModItemComponents.ATTRIBUTIONS)) stack.set(ModItemComponents.ATTRIBUTIONS, Attributions.createNew());
        Attributions attributions = stack.get(ModItemComponents.ATTRIBUTIONS);
        if(id == 3)
            //noinspection DataFlowIssue
            attributions.addPersistentAttribute(player.getUuid(), cost);
        else
            //noinspection DataFlowIssue
            attributions.addAttribute(id, player.getUuid(), cost);

        stack.set(ModItemComponents.ATTRIBUTIONS, attributions);
    }

    private static RegistryEntry<Enchantment> getRolledEnchantment(List<RegistryEntry<Enchantment>> enchantments, int rolledWeight, RegistryEntry<Enchantment>[] enchants, Set<RegistryEntry<Enchantment>> immutableDiscourage) {
        HashSet<RegistryEntry<Enchantment>> discourageSet = new HashSet<>(immutableDiscourage);
        int index = 0;
        int roll = 0;
        while(true) {
            roll++;
            if(roll == 1000) {
                return null;
            }
            Enchantment enchantment = enchantments.get(index).value();
            RegistryEntry<Enchantment> entry = enchantments.get(index++);
            rolledWeight -= enchantment.getWeight();
            if(index >= enchantments.size()) index = 0;
            if(rolledWeight > 0) continue;
            if(!entry.equals(enchants[0]) && !entry.equals(enchants[1])) {
                if(discourageSet.contains(entry)) {
                    discourageSet.remove(entry);
                    continue;
                }
                return entry;
            }
        }
    }

    static void enchantEnchantmentOption(ItemStack stack, int option, int choice) {
        SelectedEnchantments selectedEnchantments = getSelectedEnchantments(stack);
        EnchantmentOptions options = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(options == null) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment options for {}", stack);
            return;
        }
        Optional<EnchantmentSlot> enchantment = options.getOptional(option);
        if(enchantment.isEmpty()) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment option with index {} for {}", option, stack);
            return;
        }
        Optional<RegistryEntry<Enchantment>> enchant = enchantment.get().getOptional(choice);
        if(enchant.isEmpty()) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment in option {} with index {} for {}", option, choice, stack);
            return;
        }
        SelectedEnchantments newSelection = selectedEnchantments.with(option, choice);
        stack.set(ModItemComponents.SELECTED_ENCHANTMENTS, newSelection);
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
            if(calcScore(sb.length(), word.length(), tolerance) < calcScore(sb.length(), 0, tolerance)) {
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

    static int convertStringListToText(List<String> stringList, List<Text> appendableList, Function<String, Integer> widthCallback, Formatting... formatting) {
        int longest = 0;
        for(String s : stringList) {
            int length = widthCallback.apply(s);
            if(length > longest) longest = length;
            Text text = applyFormatting(s, formatting);
            appendableList.add(text);
        }
        return longest;
    }

    private static Text applyFormatting(String text, Formatting... formatting) {
        MutableText mutableText = Text.literal(text);
        for(Formatting format : formatting) mutableText.formatted(format);
        return mutableText;
    }

    private static int calcScore(int fixed, int length, int tolerance) {
        return Math.abs(fixed + length - tolerance);
    }

    static int getEnchantingCost(RegistryEntry<Enchantment> entry, ItemStack stack) {
        int level = EnchantmentHelper.getLevel(entry, stack) + 1;
        boolean powerful = entry.isIn(ModTags.POWERFUL_ENCHANTMENT);
        return powerful ? 1 + level : level;
    }

    static int getEnchantmentLevel(RegistryEntry<Enchantment> entry, ItemStack stack) {
        if(stack.isOf(Items.ENCHANTED_BOOK)) return stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(entry);
        else return EnchantmentHelper.getLevel(entry, stack);
    }

    static boolean canAfford(RegistryEntry<Enchantment> entry, ItemStack stack, PlayerEntity player) {
        int required = getEnchantingCost(entry, stack);
        PlayerEntityDuck duck = PlayerEntityDuck.get(player);
        if (duck.dungeonEnchants$getEnchantmentPoints() >= required) return true;
        return player.isCreative();
    }

    static void applyPointsAndSend(PlayerEntity player, Consumer<Integer> callback, int val) {
        PlayerEntityDuck duck = PlayerEntityDuck.get(player);
        callback.accept(val);
        if(player instanceof ServerPlayerEntity serverPlayer)
            ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(duck.dungeonEnchants$getEnchantmentPoints()));
    }

    static boolean markAsUnavailable(ItemStack stack, int hoveringIndex, RegistryEntry<Enchantment> enchant) {
        SelectedEnchantments selectedEnchantments = stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        EnchantmentOptions enchantmentOptions = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(enchantmentOptions == null) return false;
        int index = hoveringIndex / 3;
        return isEnchantmentConflicting(selectedEnchantments, enchantmentOptions, index, enchant);
    }

    static boolean isEnchantmentConflicting(SelectedEnchantments selectedEnchantments, EnchantmentOptions enchantmentOptions, int index, RegistryEntry<Enchantment> enchant) {
        for(int i = 0; i < 3; i++)
            if(selectedEnchantments.hasSelection(i)) {
                RegistryEntry<Enchantment> otherEnchant = enchantmentOptions.getOrThrow(i).getOrThrow(selectedEnchantments.get(i));
                if (i == index) return false;
                if(otherEnchant.equals(enchant)) return true;
                if(!Enchantment.canBeCombined(enchant, otherEnchant)) return true;
            }
        return false;
    }

    static int getAttributionOnItem(UUID uuid, ItemStack stack, int slotId) {
        Attributions attributions = stack.get(ModItemComponents.ATTRIBUTIONS);
        if(attributions == null) return 0;
        List<AttributionEntry> entries = attributions.get(slotId);
        int count = 0;
        for(AttributionEntry entry : entries)
            if(entry.playerUUID().equals(uuid)) count += entry.points();
        return count;
    }

    static List<RegistryEntry<Enchantment>> getOrderedEnchantments(ItemStack stack) {
        ItemEnchantmentsComponent storedEnchantments = stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        ObjectIterator<Object2IntMap.Entry<RegistryEntry<Enchantment>>> enchantmentsIterator = ItemEnchantmentsComponentDuck.get(storedEnchantments).dungeonEnchants$getEnchantments().object2IntEntrySet().iterator();
        List<RegistryEntry<Enchantment>> orderedList = new ArrayList<>();
        enchantmentsIterator.forEachRemaining(v -> orderedList.add(v.getKey()));
        return orderedList;
    }

}
