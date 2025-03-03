package net.ramixin.dunchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.ramixin.mixson.atp.annotations.events.MixsonEvent;
import net.ramixin.mixson.inline.EventContext;
import net.ramixin.mixson.inline.Mixson;

public class ModMixson {

    static void onInitialize() {
        modifyValueEffect("mending", "minecraft:repair_with_xp", 1, 0.5);
        modifyValueEffect("sharpness", "minecraft:damage", 1, 1);
        modifyValueEffect("protection", "minecraft:damage_protection", 2, 1);
        modifyValueEffect("fire_protection", "minecraft:damage_protection", 3, 2.5);
        //modifyAttributeEffect("fire_protection", "minecraft:generic.burning_time", -0.2, -0.2);
        modifyValueEffect("feather_falling", "minecraft:damage_protection", 4, 4);
        modifyValueEffect("blast_protection", "minecraft:damage_protection", 3, 2.5);
        //modifyAttributeEffect("blast_protection", "minecraft:generic.explosion_knockback_resistance", 0.2, 0.2);
        modifyValueEffect("projectile_protection", "minecraft:damage_protection", 3, 2.5);
        //modifyAttributeEffect("aqua_affinity", "minecraft:player.submerged_mining_speed", 2, 1);
        modifyValueEffect("smite", "minecraft:damage", 4.5, 4);
        modifyValueEffect("bane_of_arthropods", "minecraft:damage", 4.5, 4);
        modifyValueEffect("knockback", "minecraft:knockback", 1, 0.5);
        //modifyAttributeEffect("efficiency", "minecraft:player.mining_efficiency", 2, 1.5);
        modifyValueEffect("power", "minecraft:damage", 1.5, 0.5);
        modifyValueEffect("punch", "minecraft:knockback", 1, 0.5);
        modifyValueEffect("luck_of_the_sea", "minecraft:fishing_luck_bonus", 0.5, 0.25);
        modifyValueEffect("impaling", "minecraft:damage", 4.5, 4);
        modifyValueEffect("piercing", "minecraft:projectile_piercing", 2, 1);
        modifyValueEffect("density", "minecraft:smash_damage_per_fallen_block", 1.5, 0.5);
        modifyValueEffect("breach", "minecraft:armor_effectiveness", -0.2, -0.2);
    }

    @MixsonEvent("enchantment/*")
    private static void setAllEnchantmentsToThreeLevels(EventContext<JsonElement> context) {
        context.getFile().getAsJsonObject().addProperty("max_level", 3);
    }

    /*
    FROST_WALKER - what on earth is this file
    BINDING_CURSE - how do I do this one...
    BANE_OF_ARTHROPODS - slowness effect
    FIRE_ASPECT - needs custom treatment
    SILK_TOUCH - yeah, I'm at a lose
    FLAME - custom service
    INFINITY - special man
    CHANNELING - tricky tricky
    MULTISHOT - hmmmm
    VANISHING_CURSE - yep, special
    */

    private static void modifyValueEffect(String enchantment, String effectName, double base, double perLevel) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                "enchantment/"+enchantment,
                "modify_"+enchantment,
                context -> {
                    JsonArray effect = getEffect(effectName, context);
                    JsonElement removedEntry = effect.remove(0);
                    if(!(removedEntry instanceof JsonObject objectEntry)) return;
                    JsonObject newEntry = buildAddEffect(base, perLevel);
                    if(objectEntry.has("requirements")) newEntry.add("requirements", objectEntry.get("requirements"));
                    effect.add(newEntry);
                }
        );
    }

    private static void modifyAttributeEffect(String enchantment, String attribute, double base, double perLevel) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                "enchantment/"+enchantment,
                "modify_"+enchantment,
                context -> {
                    JsonArray effect = getEffect(attribute, context);
                    JsonElement removedEntry = effect.remove(0);
                    if(!(removedEntry instanceof JsonObject objectEntry)) return;
                    objectEntry.addProperty("attribute", attribute);
                    objectEntry.add("amount", buildLinearValue(base, perLevel));
                    effect.add(objectEntry);
                }
        );
    }

    private static JsonArray getEffect(String id, EventContext<JsonElement> context) {
        JsonObject object = context.getFile().getAsJsonObject();
        JsonObject effects = object.getAsJsonObject("effects");
        return effects.getAsJsonArray(id);
    }

    private static JsonObject buildAddEffect(double base, double perLevel) {
        JsonObject shell = new JsonObject();
        JsonObject effect = new JsonObject();
        effect.addProperty("type", "minecraft:add");
        JsonObject value = buildLinearValue(base, perLevel);
        effect.add("value", value);
        shell.add("effect", effect);
        return shell;
    }

    private static JsonObject buildLinearValue(double base, double perLevel) {
        JsonObject linearValue = new JsonObject();
        linearValue.addProperty("type", "minecraft:linear");
        linearValue.addProperty("base", base);
        linearValue.addProperty("per_level_above_first", perLevel);
        return linearValue;
    }

}
