package net.ramixin.dunchanting.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.ramixin.dunchanting.items.components.*;

import java.util.*;

public interface EnchantmentOptionsUtil {

    static void prepareComponents(ItemStack stack, Level level, int playerLevel) {
        if(stack.isEmpty()) return;
        EnchantmentOptions unlocked = EnchantmentOptionsUtil.getUnlocked(stack);
        if(unlocked == EnchantmentOptions.DEFAULT) {
            ModUtil.generateComponents(stack, level, playerLevel);
            return;
        }
        EnchantmentOptions locked = EnchantmentOptionsUtil.getLocked(stack);
        if(locked == EnchantmentOptions.DEFAULT) {
            stack.set(ModDataComponents.LOCKED_ENCHANTMENT_OPTIONS, EnchantmentOptions.DEFAULT);
        }
    }

    static EnchantmentOptions getUnlocked(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS, EnchantmentOptions.DEFAULT);
    }

    static EnchantmentOptions getLocked(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.LOCKED_ENCHANTMENT_OPTIONS, EnchantmentOptions.DEFAULT);
    }

    static EnchantmentOptions generateLocked(ItemStack stack, Level world, int playerLevel) {
        Gilded gilded = stack.get(ModDataComponents.GILDED);
        List<Holder<Enchantment>> possibleEnchants = new ArrayList<>();
        for(Holder<Enchantment> entry : world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getTagOrEmpty(EnchantmentTags.IN_ENCHANTING_TABLE)) {
            if(gilded != null && gilded.enchantmentEntry().value().equals(entry.value())) continue;
            if(entry.value().isSupportedItem(stack)) possibleEnchants.add(entry);
        }
        Collections.shuffle(possibleEnchants);

        if(possibleEnchants.isEmpty()) return EnchantmentOptions.DEFAULT;
        if(possibleEnchants.size() == 1) {
            Holder<Enchantment> enchant = possibleEnchants.getFirst();
            return new EnchantmentOptions(new EnchantmentSlot(enchant, null, null), null, null);
        }
        if(possibleEnchants.size() == 2) {
            Holder<Enchantment> enchant1 = possibleEnchants.getFirst();
            Holder<Enchantment> enchant2 = possibleEnchants.get(1);
            return new EnchantmentOptions(new EnchantmentSlot(enchant1, enchant2, null), new EnchantmentSlot(enchant1, enchant2, null), null);
        }

        float thirdEnchantRoll = world.getRandom().nextFloat();
        boolean[] hasThirdEnchant = {
                thirdEnchantRoll <= ModMath.OddsOfThirdEnchant(playerLevel, 0),
                thirdEnchantRoll <= ModMath.OddsOfThirdEnchant(playerLevel, 1),
                thirdEnchantRoll <= ModMath.OddsOfThirdEnchant(playerLevel, 2),
        };

        ItemEnchantments currentEnchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        Iterator<Holder<Enchantment>> currentEnchantsSet;
        if(gilded == null) currentEnchantsSet = currentEnchants.keySet().iterator();
        else currentEnchantsSet = currentEnchants.keySet().stream().filter(entry -> !entry.value().equals(gilded.enchantmentEntry().value())).iterator();

        List<Holder<Enchantment>> initialEnchants = new ArrayList<>();
        List<Holder<Enchantment>> possibleInitialEnchants = new ArrayList<>(possibleEnchants);
        int possibleInitialEnchantsWeight = ModMath.totalEnchantmentWeights(possibleInitialEnchants);
        for(int i = 0; i < 3; i++) {

            if(currentEnchantsSet.hasNext()) {
                Holder<Enchantment> entry = currentEnchantsSet.next();

                initialEnchants.add(entry);
                if(possibleInitialEnchants.remove(entry)) {
                    possibleInitialEnchantsWeight -= entry.value().getWeight();
                }
                continue;

            }

            int rolledWeight = world.getRandom().nextInt(1, possibleInitialEnchantsWeight);
            Holder<Enchantment> entry = getEnchantmentAtWeight(possibleInitialEnchants, rolledWeight, Set.of());
            initialEnchants.add(entry);
            possibleInitialEnchantsWeight -= entry.value().getWeight();
            possibleInitialEnchants.remove(entry);
        }

        Set<Holder<Enchantment>> discourageSet = new HashSet<>(initialEnchants);

        List<Holder<Enchantment>> secondEnchants = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            List<Holder<Enchantment>> possibleSecondEnchants = new ArrayList<>(possibleEnchants);
            Collections.shuffle(possibleSecondEnchants);
            int possibleSecondEnchantsWeight = ModMath.totalEnchantmentWeights(possibleSecondEnchants);

            possibleSecondEnchants.remove(initialEnchants.get(i));
            int rolledWeight = world.getRandom().nextInt(1, possibleSecondEnchantsWeight);
            Holder<Enchantment> entry = getEnchantmentAtWeight(possibleSecondEnchants, rolledWeight, discourageSet);
            discourageSet.add(entry);
            secondEnchants.add(entry);
        }

        List<Holder<Enchantment>> thirdEnchants = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            if(!hasThirdEnchant[i]) {
                thirdEnchants.add(null);
                continue;
            }

            List<Holder<Enchantment>> possibleThirdEnchants = new ArrayList<>(possibleEnchants);
            Collections.shuffle(possibleThirdEnchants);
            int possibleThirdEnchantsWeight = ModMath.totalEnchantmentWeights(possibleThirdEnchants);

            possibleThirdEnchants.remove(initialEnchants.get(i));
            possibleThirdEnchants.remove(secondEnchants.get(i));
            int rolledWeight = world.getRandom().nextInt(1, possibleThirdEnchantsWeight);
            Holder<Enchantment> entry = getEnchantmentAtWeight(possibleThirdEnchants, rolledWeight, discourageSet);
            discourageSet.add(entry);
            thirdEnchants.add(entry);
        }

        return new EnchantmentOptions(
                new EnchantmentSlot(initialEnchants.getFirst(), secondEnchants.getFirst(), thirdEnchants.getFirst()),
                new EnchantmentSlot(initialEnchants.get(1), secondEnchants.get(1), thirdEnchants.get(1)),
                new EnchantmentSlot(initialEnchants.get(2), secondEnchants.get(2), thirdEnchants.get(2))
        );
    }

    static EnchantmentOptions generateUnlocked(ItemStack stack, Level world, int playerLevel) {
        EnchantmentOptions lockedOptions = getLocked(stack);
        SelectedEnchantments selectedEnchants = SelectedEnchantmentsUtil.get(stack);

        float odds = ModMath.OddsOfAnotherSlot(playerLevel);
        float slotRoll = world.getRandom().nextFloat();
        int slotCount;
        if(playerLevel <= 5) slotCount = 1;
        else if(playerLevel <= 10) slotCount = slotRoll <= odds ? 2 : 1;
        else if(playerLevel <= 15) slotCount = slotRoll <= odds ? 3 : 2;
        else slotCount = 3;
        EnchantmentSlot[] slots = new EnchantmentSlot[3];
        for(int i = 0; i < 3; i++) {
            if(lockedOptions.hasEmptySlot(i))
                continue;
            if(selectedEnchants.hasSelection(i) || i <= slotCount - 1) {
                slots[i] = lockedOptions.getOrThrow(i);
            }
        }
        return new EnchantmentOptions(slots[0], slots[1], slots[2]);
    }

    static void updateLockedAfterUpdatingUnlocked(ItemStack stack) {
        EnchantmentOptions lockedOptions = getLocked(stack);
        EnchantmentOptions unlockedOptions = getUnlocked(stack);
        EnchantmentOptions newLockedOptions = lockedOptions.withClearedSlots(
                unlockedOptions.hasEmptySlot(0),
                unlockedOptions.hasEmptySlot(1),
                unlockedOptions.hasEmptySlot(2)
        );
        stack.set(ModDataComponents.LOCKED_ENCHANTMENT_OPTIONS, newLockedOptions);
    }

    private static Holder<Enchantment> getEnchantmentAtWeight(List<Holder<Enchantment>> enchantments, int weight, Set<Holder<Enchantment>> discourageSet) {
        int currentWeight = weight;
        Set<Holder<Enchantment>> seenDiscourages = new HashSet<>();
        for(int index = 0; true; index++) {
            index %= enchantments.size();
            Holder<Enchantment> entry = enchantments.get(index);
            if(discourageSet.contains(entry) && !seenDiscourages.contains(entry)) {
                seenDiscourages.add(entry);
                continue;
            }
            int entryWeight = Math.max(1, entry.value().getWeight());
            if(entryWeight > currentWeight)
                return entry;
            currentWeight -= entryWeight;
        }
    }

    static void unlockOption(ItemStack stack, int index) {
        EnchantmentOptions locked = getLocked(stack);
        EnchantmentOptions unlocked = getUnlocked(stack);
        Optional<EnchantmentSlot> slot = locked.getOptional(index);
        if(slot.isEmpty()) return;
        EnchantmentOptions newUnlocked = unlocked.withSlot(index, slot.get());
        stack.set(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS, newUnlocked);
        updateLockedAfterUpdatingUnlocked(stack);
    }
}
