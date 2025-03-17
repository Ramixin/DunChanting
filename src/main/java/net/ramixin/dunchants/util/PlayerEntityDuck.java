package net.ramixin.dunchants.util;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerEntityDuck {

    int dungeonEnchants$getEnchantmentPoints();

    void dungeonEnchants$setEnchantmentPoints(int points);

    void dungeonEnchants$changeEnchantmentPoints(int delta);

    static PlayerEntityDuck get(PlayerEntity e) {
        return (PlayerEntityDuck) e;
    }


}
