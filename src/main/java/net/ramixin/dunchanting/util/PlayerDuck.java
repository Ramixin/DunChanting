package net.ramixin.dunchanting.util;

import net.minecraft.world.entity.player.Player;

public interface PlayerDuck {

    int dungeonEnchants$getEnchantmentPoints();

    void dungeonEnchants$setEnchantmentPoints(int points);

    void dungeonEnchants$changeEnchantmentPoints(int delta);

    static PlayerDuck get(Player e) {
        return (PlayerDuck) e;
    }


}
