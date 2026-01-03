package net.ramixin.dunchanting.client.util;

import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;
import net.ramixin.dunchanting.Dunchanting;
import net.ramixin.dunchanting.client.DunchantingClient;

public interface ModTextures {

    Identifier[] selectionAnimationTextures = new Identifier[]{
            Dunchanting.id("container/enchanting_table/selection_animation2"),
            Dunchanting.id("container/enchanting_table/selection_animation3"),
            Dunchanting.id("container/enchanting_table/selection_animation4"),
            Dunchanting.id("container/enchanting_table/selection_animation5"),
            Dunchanting.id("container/enchanting_table/selection_animation6"),
            Dunchanting.id("container/enchanting_table/selection_animation7"),
            Dunchanting.id("container/enchanting_table/selection_animation8"),
            Dunchanting.id("container/enchanting_table/selection_animation9"),
            Dunchanting.id("container/enchanting_table/selection_animation10"),
            Dunchanting.id("container/enchanting_table/selection_animation11"),
            Dunchanting.id("container/enchanting_table/selection_animation12"),
            Dunchanting.id("container/enchanting_table/selection_animation13"),
            Dunchanting.id("container/enchanting_table/selection_animation14")
    };
    Identifier[] selectedEnchantmentBackdrops = new Identifier[]{
            Dunchanting.id("container/enchanting_table/enchanted_0"),
            Dunchanting.id("container/enchanting_table/enchanted_1"),
            Dunchanting.id("container/enchanting_table/enchanted_2")

    };
    Identifier[] optionsRomanNumerals = new Identifier[]{
            Dunchanting.id("container/enchanting_table/one_roman_numeral"),
            Dunchanting.id("container/enchanting_table/two_roman_numeral"),
            Dunchanting.id("container/enchanting_table/three_roman_numeral")
    };
    Identifier lockedEnchantmentOption = Dunchanting.id("container/enchanting_table/locked_small");
    Identifier LockedEnchantmentSlot = Dunchanting.id("container/enchanting_table/locked");
    Identifier enchantmentOptionBackdrop = Dunchanting.id("container/enchanting_table/selection_small");
    Material missingIcon = new Material(DunchantingClient.ENCHANTMENT_ICONS_ATLAS_TEXTURE, Identifier.withDefaultNamespace("missingno"));

    Identifier anvilTextFieldBackdrop = Dunchanting.id("container/anvil/text_backdrop");

}
