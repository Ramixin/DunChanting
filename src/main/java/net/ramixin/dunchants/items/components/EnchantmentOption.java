package net.ramixin.dunchants.items.components;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record EnchantmentOption(Optional<String> first, Optional<String> second, Optional<String> third) {

    public static final EnchantmentOption DEFAULT = new EnchantmentOption(Optional.empty(), Optional.empty(), Optional.empty());

    public EnchantmentOption(@Nullable String first, @Nullable String second, @Nullable String third) {
        this(Optional.ofNullable(first), Optional.ofNullable(second), Optional.ofNullable(third));
    }

    public Optional<String> getOptional(int index) {
        if(index < 0 || index > 2) throw new IndexOutOfBoundsException("unexpected index: " + index);
        return switch (index) {
            case 0 -> first;
            case 1 -> second;
            default -> third;
        };
    }

    public boolean isLocked(int index) {
        return getOptional(index).isEmpty();
    }

    public String get(int index) {
        return getOptional(index).orElseThrow();
    }

    public byte unlockedCount() {
        if(first.isEmpty()) return 0;
        else if(second.isEmpty()) return 1;
        else if(third.isEmpty()) return 2;
        else return 3;
    }

    public EnchantmentOption modify(String id, int optionId) {
        return new EnchantmentOption(
                optionId == 0 ? Optional.of(id) : first,
                optionId == 1 ? Optional.of(id) : second,
                optionId == 2 ? Optional.of(id) : third
        );
    }

}
