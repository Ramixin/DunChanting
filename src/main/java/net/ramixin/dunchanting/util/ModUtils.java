package net.ramixin.dunchanting.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.enchantments.LeveledEnchantmentEffect;
import net.ramixin.dunchanting.items.ModItemComponents;
import net.ramixin.dunchanting.items.components.*;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public interface ModUtils {

    static boolean getLeveledEnchantmentEffectValue(ComponentType<LeveledEnchantmentEffect> type, World world, ItemStack stack) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries())
            if(EnchantmentDuck.get(entry.getKey().value()).dungeonEnchants$getLeveledEffectResult(type, world, entry.getIntValue())) return true;
        return false;
    }

    static float oddsOfThirdEnchantmentOption(int level, int optionIndex) {
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

    static boolean enchantmentIsInvalid(String id, Registry<Enchantment> registry, List<RegistryEntry<Enchantment>> validList) {
        Optional<RegistryEntry.Reference<Enchantment>> entry = registry.getEntry(Identifier.of(id));
        return entry.filter(validList::contains).isEmpty();
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

        int roll = world.getRandom().nextBetween(0, playerLevel);
        int enchantmentSlotCount;
        if(roll <= 5) enchantmentSlotCount = 1;
        else if(roll <= 10) enchantmentSlotCount = 2;
        else enchantmentSlotCount = 3;

        EnchantmentOption[] options = new EnchantmentOption[3];
        Set<String> discourageList = new HashSet<>();

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        Iterator<RegistryEntry<Enchantment>> enchantmentsIterator = enchantments.getEnchantments().iterator();

        int usedEnchantments = 0;
        for(int i = 0; i < 3; i++) {
            if(enchantmentsIterator.hasNext()) {
                RegistryEntry<Enchantment> enchant = enchantmentsIterator.next();
                String id = enchant.getIdAsString();
                discourageList.add(id);
                options[i] = new EnchantmentOption(id, id, null);
                usedEnchantments++;
                continue;
            }
            if(i >= enchantmentSlotCount) break;
            options[i] = generateEnchantmentOption(world.getRandom(), playerLevel, i, discourageList, totalWeight.get(), enchantList);
        }
        if(usedEnchantments > 0)
            stack.set(ModItemComponents.SELECTED_ENCHANTMENTS,
                    new SelectedEnchantments(
                            0,
                            usedEnchantments > 1 ? 0 : null,
                            usedEnchantments > 2 ? 0 : null
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

    private static EnchantmentOption generateEnchantmentOption(Random random, int playerLevel, int optionIndex, Set<String> discourageList, int totalWeight, List<RegistryEntry<Enchantment>> enchantList) {
        float enchantRoll = random.nextFloat();
        int optionCount;
        if(enchantRoll < oddsOfThirdEnchantmentOption(playerLevel, optionIndex)) optionCount = 3;
        else optionCount = 2;
        String[] enchants = new String[3];
        for(int i = 0; i < optionCount; i++) {
            int rolledWeight = random.nextBetween(1, totalWeight);
            String enchant = getRolledEnchantment(enchantList, rolledWeight, enchants, discourageList);
            if(enchant == null) {
                if(i != 2) return new EnchantmentOption(Optional.empty(), Optional.empty(), Optional.empty());
            } else {
                discourageList.add(enchant);
                enchants[i] = enchant;
            }
        }
        return new EnchantmentOption(Optional.ofNullable(enchants[0]), Optional.ofNullable(enchants[1]), Optional.ofNullable(enchants[2]));
    }

    private static String getRolledEnchantment(List<RegistryEntry<Enchantment>> enchantments, int rolledWeight, String[] enchants, Set<String> immutableDiscourage) {
        HashSet<String> discourageSet = new HashSet<>(immutableDiscourage);
        int index = 0;
        int roll = 0;
        while(true) {
            roll++;
            if(roll == 1000) {
                return null;
            }
            Enchantment enchantment = enchantments.get(index).value();
            String id = enchantments.get(index++).getIdAsString();
            rolledWeight -= enchantment.getWeight();
            if(index >= enchantments.size()) index = 0;
            if(rolledWeight > 0) continue;
            if(!id.equals(enchants[0]) && !id.equals(enchants[1])) {
                if(discourageSet.contains(id)) {
                    discourageSet.remove(id);
                    continue;
                }
                return id;
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
        Optional<EnchantmentOption> enchantment = options.getOptional(option);
        if(enchantment.isEmpty()) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment option with index {} for {}", option, stack);
            return;
        }
        Optional<String> enchant = enchantment.get().getOptional(choice);
        if(enchant.isEmpty()) {
            Dunchanting.LOGGER.error("Failed to enchant item: No enchantment in option {} with index {} for {}", option, choice, stack);
            return;
        }
        SelectedEnchantments newSelection = selectedEnchantments.enchantOption(option, choice);
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

    static ByteArrayOutputStream bufferedImageToStream(BufferedImage image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream;
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

    static RegistryEntry<Enchantment> idToEntry(Identifier id, @NotNull World world) {
        Registry<Enchantment> enchantmentRegistry = world.getRegistryManager()
                /*? >=1.21.2 {*/
                .getOrThrow(RegistryKeys.ENCHANTMENT);
                //?} else
                /*.get(RegistryKeys.ENCHANTMENT);*/
        Optional<RegistryEntry.Reference<Enchantment>> maybeEnchant = enchantmentRegistry.getEntry(id);
        if(maybeEnchant.isEmpty()) return null;
        RegistryEntry.Reference<Enchantment> enchantmentReference = maybeEnchant.get();
        return enchantmentRegistry.getEntry(enchantmentReference.value());
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

    static boolean markAsUnavailable(ItemStack stack, int hoveringIndex, String enchant, Registry<Enchantment> registry) {
        SelectedEnchantments selectedEnchantments = stack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        EnchantmentOptions enchantmentOptions = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(enchantmentOptions == null) return false;
        int index = hoveringIndex / 3;
        RegistryEntry<Enchantment> enchantValue = registry.getEntry(registry.get(Identifier.of(enchant)));
        return doSelectionsForbid(enchant, selectedEnchantments, enchantmentOptions, index, registry, enchantValue);
    }

    static boolean doSelectionsForbid(String enchant, SelectedEnchantments selectedEnchantments, EnchantmentOptions enchantmentOptions, int index, Registry<Enchantment> registry, RegistryEntry<Enchantment> enchantValue) {
        for(int i = 0; i < 3; i++)
            if(selectedEnchantments.hasSelection(i)) {
                String otherEnchant = enchantmentOptions.get(i).get(selectedEnchantments.get(i));
                if (i == index) return false;
                if(otherEnchant.equals(enchant)) return true;
                RegistryEntry<Enchantment> otherValue = registry.getEntry(registry.get(Identifier.of(otherEnchant)));
                if(!Enchantment.canBeCombined(enchantValue, otherValue)) return true;
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

    static List<RegistryEntry<Enchantment>> getOrderedEnchantments(ItemStack stack, RegistryWrapper.WrapperLookup wrapperLookup) {
        RegistryEntryList<Enchantment> registryEntryList = ItemEnchantmentsComponent.getTooltipOrderList(wrapperLookup, RegistryKeys.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);
        ItemEnchantmentsComponent storedEnchantments = stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        List<RegistryEntry<Enchantment>> orderedList = new ArrayList<>();
        for(RegistryEntry<Enchantment> enchantment : registryEntryList)
            if(storedEnchantments.getLevel(enchantment) > 0) orderedList.add(enchantment);
        return orderedList;
    }

}
