package net.ramixin.dunchants.client.util;

import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.DungeonEnchants;
import net.ramixin.dunchants.client.DungeonEnchantsClient;

public interface ModTextures {

    Identifier[] selectionAnimationTextures = new Identifier[]{
            DungeonEnchants.id("container/enchanting_table/selection_animation2"),
            DungeonEnchants.id("container/enchanting_table/selection_animation3"),
            DungeonEnchants.id("container/enchanting_table/selection_animation4"),
            DungeonEnchants.id("container/enchanting_table/selection_animation5"),
            DungeonEnchants.id("container/enchanting_table/selection_animation6"),
            DungeonEnchants.id("container/enchanting_table/selection_animation7"),
            DungeonEnchants.id("container/enchanting_table/selection_animation8"),
            DungeonEnchants.id("container/enchanting_table/selection_animation9"),
            DungeonEnchants.id("container/enchanting_table/selection_animation10"),
            DungeonEnchants.id("container/enchanting_table/selection_animation11"),
            DungeonEnchants.id("container/enchanting_table/selection_animation12"),
            DungeonEnchants.id("container/enchanting_table/selection_animation13"),
            DungeonEnchants.id("container/enchanting_table/selection_animation14")
    };
    Identifier[] selectedEnchantmentBackdrops = new Identifier[]{
            DungeonEnchants.id("container/enchanting_table/enchanted_0"),
            DungeonEnchants.id("container/enchanting_table/enchanted_1"),
            DungeonEnchants.id("container/enchanting_table/enchanted_2")

    };
    Identifier[] optionsRomanNumerals = new Identifier[]{
            DungeonEnchants.id("container/enchanting_table/one_roman_numeral"),
            DungeonEnchants.id("container/enchanting_table/two_roman_numeral"),
            DungeonEnchants.id("container/enchanting_table/three_roman_numeral")
    };
    Identifier lockedEnchantmentOption = DungeonEnchants.id("container/enchanting_table/locked_small");
    Identifier LockedEnchantmentSlot = DungeonEnchants.id("container/enchanting_table/locked");
    Identifier enchantmentOptionBackdrop = DungeonEnchants.id("container/enchanting_table/selection_small");
    SpriteIdentifier missingIcon = new SpriteIdentifier(DungeonEnchantsClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, Identifier.ofVanilla("missingno"));

    Identifier anvilTextFieldBackdrop = DungeonEnchants.id("container/anvil/text_backdrop");

}
