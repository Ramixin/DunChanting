package net.ramgames.dunchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public interface DungeonEnchantsUtils {

    List<List<String>> DEFAULT_COMPONENT = List.of(List.of("locked"), List.of("locked"), List.of("locked"));

    static boolean containsImproperComponent(ItemStack stack, World world) {
        List<List<String>> comp = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(comp == null)
            return true;
        if(comp.size() != 3)
            return true;
        Registry<Enchantment> enchantmentRegistry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        Optional<RegistryEntryList.Named<Enchantment>> optional = enchantmentRegistry.getEntryList(EnchantmentTags.IN_ENCHANTING_TABLE);
        if(optional.isEmpty())
            return true;
        List<RegistryEntry<Enchantment>> enchantList = new ArrayList<>(optional.get().stream().filter(entry -> entry.value().isSupportedItem(stack)).toList());
        if(enchantList.size() <= 1)
            return true;
        for(List<String> enchantOption : comp) {
            if(enchantOption.size() == 1) {
                if(enchantOption.getFirst().equals("locked")) continue;
                if(enchantmentIsInvalid(enchantOption.getFirst(), enchantmentRegistry, enchantList))
                    return true;
                else continue;
            }
            if(enchantOption.size() != 3)
                return true;
            for(String enchant : enchantOption) {
                if(enchant.equals("locked")) continue;
                if(enchantmentIsInvalid(enchant, enchantmentRegistry, enchantList))
                    return true;
            }
        }
        return false;
    }

    private static boolean enchantmentIsInvalid(String id, Registry<Enchantment> registry, List<RegistryEntry<Enchantment>> validList) {
        Optional<RegistryEntry.Reference<Enchantment>> entry = registry.getEntry(Identifier.of(id));
        return entry.filter(validList::contains).isEmpty();
    }

    static List<List<String>> generateComponent(ItemStack stack, World world) {
        Optional<RegistryEntryList.Named<Enchantment>> optional = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntryList(EnchantmentTags.IN_ENCHANTING_TABLE);
        if(optional.isEmpty()) return DEFAULT_COMPONENT;
        List<RegistryEntry<Enchantment>> enchantList = new ArrayList<>(optional.get().stream().filter(entry -> entry.value().isSupportedItem(stack)).toList());
        if(enchantList.size() <= 1) return DEFAULT_COMPONENT;

        AtomicInteger totalWeight = new AtomicInteger();
        enchantList.forEach(entry -> totalWeight.addAndGet(entry.value().getWeight()));
        Collections.shuffle(new ArrayList<>(enchantList));
        int enchantability = stack.getItem().getEnchantability();
        int roll = world.getRandom().nextBetween(1 , enchantability);
        int enchantmentSlotCount;
        if(roll <= 5) enchantmentSlotCount = 1;
        else if(roll <= 10) enchantmentSlotCount = 2;
        else enchantmentSlotCount = 3;
        List<List<String>> result = new ArrayList<>();
        Set<String> discourageList = new HashSet<>();
        for(int i = 0; i < 3; i++) {
            if(i+1 > enchantmentSlotCount) {
                result.add(List.of("locked"));
                continue;
            }
            int enchantRoll = world.getRandom().nextBetween(1, 10);
            int enchantOptionCount;
            if(enchantRoll <= 6) enchantOptionCount = 2;
            else enchantOptionCount = 3;
            enchantOptionCount = Math.min(enchantList.size(), enchantOptionCount);
            List<String> enchants = new ArrayList<>();
            for(int l = 0; l < 3; l++) {
                if(l+1 > enchantOptionCount) {
                    enchants.add("locked");
                    continue;
                }
                int rolledWeight = world.getRandom().nextBetween(1, totalWeight.get());
                String enchant = getRolledEnchantment(enchantList, rolledWeight, enchants, new HashSet<>(discourageList));
                discourageList.add(enchant);
                enchants.add(enchant);
            }
            result.add(enchants);
        }
        return result;
    }

    private static String getRolledEnchantment(List<RegistryEntry<Enchantment>> enchantments, int rolledWeight, List<String> blacklist, Set<String> discourageSet) {
        int index = 0;
        while(true) {
            Enchantment enchantment = enchantments.get(index).value();
            String id = enchantments.get(index++).getIdAsString();
            rolledWeight -= enchantment.getWeight();
            if(index >= enchantments.size()) index = 0;
            if(rolledWeight <= 0 && !blacklist.contains(id)) {
                if(discourageSet.contains(id)) {
                    discourageSet.remove(id);
                    continue;
                }
                return id;
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    static boolean isOptionEnchanted(ItemStack stack, World world, int optionId, List<List<String>> cache) {
        if(cache != stack.get(ModItemComponents.ENCHANTMENT_OPTIONS)) if(containsImproperComponent(stack, world)) return false;
        List<List<String>> comp = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(optionId < 0 || optionId >= comp.size()) return false;
        if(comp.get(optionId).size() != 1) return false;
        return !comp.get(optionId).getFirst().equals("locked");
    }

    static List<List<String>> enchantEnchantmentOption(int option, int choice, List<List<String>> enchantmentOptions) {
        List<List<String>> mutableOptions = new ArrayList<>(enchantmentOptions);
        mutableOptions.set(option, List.of(mutableOptions.get(option).get(choice)));
        return List.copyOf(mutableOptions);
    }
}
