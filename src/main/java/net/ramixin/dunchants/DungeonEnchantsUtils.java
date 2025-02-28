package net.ramixin.dunchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import org.apache.commons.lang3.function.TriFunction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public interface DungeonEnchantsUtils {

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

    static EnchantmentOptions getOptions(ItemStack stack) {
        return stack.getOrDefault(ModItemComponents.ENCHANTMENT_OPTIONS, EnchantmentOptions.DEFAULT);
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
                DungeonEnchantsUtils.optionalOfEmpty(iter.next(), evalFunc),
                DungeonEnchantsUtils.optionalOfEmpty(iter.next(), evalFunc),
                DungeonEnchantsUtils.optionalOfEmpty(iter.next(), evalFunc)
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
        Optional<RegistryEntryList.Named<Enchantment>> optional = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.IN_ENCHANTING_TABLE);
        if(optional.isEmpty()) return;
        List<RegistryEntry<Enchantment>> enchantList = new ArrayList<>(optional.get().stream().filter(entry -> entry.value().isSupportedItem(stack)).toList());
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

        for(int i = 0; i < 3; i++) {
            if(i >= enchantmentSlotCount) break;
            options[i] = generateEnchantmentOption(world.getRandom(), playerLevel, i, discourageList, totalWeight.get(), enchantList);
        }
        stack.set(ModItemComponents.ENCHANTMENT_OPTIONS, new EnchantmentOptions(Optional.of(options[0]), Optional.ofNullable(options[1]), Optional.ofNullable(options[2])));
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
            discourageList.add(enchant);
            enchants[i] = enchant;
        }
        return new EnchantmentOption(Optional.ofNullable(enchants[0]), Optional.ofNullable(enchants[1]), Optional.ofNullable(enchants[2]));
    }

    private static String getRolledEnchantment(List<RegistryEntry<Enchantment>> enchantments, int rolledWeight, String[] enchants, Set<String> immutableDiscourage) {
        HashSet<String> discourageSet = new HashSet<>(immutableDiscourage);
        int index = 0;
        while(true) {
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
            DungeonEnchants.LOGGER.error("Failed to enchant item: No enchantment options for {}", stack);
            return;
        }
        Optional<EnchantmentOption> enchantment = options.getOptional(option);
        if(enchantment.isEmpty()) {
            DungeonEnchants.LOGGER.error("Failed to enchant item: No enchantment option with index {} for {}", option, stack);
            return;
        }
        Optional<String> enchant = enchantment.get().getOptional(choice);
        if(enchant.isEmpty()) {
            DungeonEnchants.LOGGER.error("Failed to enchant item: No enchantment in option {} with index {} for {}", option, choice, stack);
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
}
