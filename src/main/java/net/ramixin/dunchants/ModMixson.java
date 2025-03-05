package net.ramixin.dunchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.ramixin.mixson.atp.annotations.events.MixsonEvent;
import net.ramixin.mixson.debug.DebugMode;
import net.ramixin.mixson.inline.EventContext;
import net.ramixin.mixson.inline.Mixson;

public class ModMixson {

    static void onInitialize() {
        Mixson.setDebugMode(DebugMode.EXPORT);

        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                id -> id.getPath().startsWith("enchantment/"),
                "ChangeEnchantmentMaxLevel",
                context -> context.getFile().getAsJsonObject().addProperty("max_level", 3),
                true
        );

        modifyValueEffect("mending", "minecraft:repair_with_xp", 1, 0.5);
        modifyValueEffect("sharpness", "minecraft:damage", 1, 1);
        modifyValueEffect("protection", "minecraft:damage_protection", 2, 1);
        modifyValueEffect("fire_protection", "minecraft:damage_protection", 3, 2.5);
        modifyAttributeEffect("fire_protection", "minecraft:burning_time", -0.2, -0.2);
        modifyValueEffect("feather_falling", "minecraft:damage_protection", 4, 4);
        modifyValueEffect("blast_protection", "minecraft:damage_protection", 3, 2.5);
        modifyAttributeEffect("blast_protection", "minecraft:explosion_knockback_resistance", 0.2, 0.2);
        modifyValueEffect("projectile_protection", "minecraft:damage_protection", 3, 2.5);
        modifyAttributeEffect("aqua_affinity", "minecraft:submerged_mining_speed", 2, 1);
        modifyValueEffect("smite", "minecraft:damage", 4.5, 4);
        modifyValueEffect("bane_of_arthropods", "minecraft:damage", 4.5, 4);
        modifyValueEffect("knockback", "minecraft:knockback", 1, 0.5);
        modifyAttributeEffect("efficiency", "minecraft:mining_efficiency", 2, 1.5);
        modifyValueEffect("power", "minecraft:damage", 1.5, 0.5);
        modifyValueEffect("punch", "minecraft:knockback", 1, 0.5);
        modifyValueEffect("luck_of_the_sea", "minecraft:fishing_luck_bonus", 0.5, 0.25);
        modifyValueEffect("impaling", "minecraft:damage", 4.5, 4);
        modifyValueEffect("piercing", "minecraft:projectile_piercing", 2, 1);
        modifyValueEffect("density", "minecraft:smash_damage_per_fallen_block", 1.5, 0.5);
        modifyValueEffect("breach", "minecraft:armor_effectiveness", -0.2, -0.2);
        modifyValueEffect("fire_aspect", "minecraft:post_attack", 3, 2.5, "duration");
        modifyValueEffect("flame", "minecraft:projectile_spawned", 40, 30, "duration");
    }

    @MixsonEvent("enchantment/infinity")
    private static void modifyInfinityEnchantment(EventContext<JsonElement> context) {
        JsonArray ammoUseEffect = getEffect("minecraft:ammo_use", context);
        JsonElement effect = ammoUseEffect.get(0);
        if(!(effect instanceof JsonObject objEffect)) return;
        JsonArray terms = new JsonArray();
        JsonObject matchToolRequirement = objEffect.getAsJsonObject("requirements");
        terms.add(matchToolRequirement);
        JsonObject chanceRequirement = new JsonObject();
        JsonObject chance = new JsonObject();
        chance.addProperty("type", "minecraft:enchantment_level");
        chance.add("amount", buildLinearValue(0.333, 0.333));
        chanceRequirement.add("chance", chance);
        chanceRequirement.addProperty("condition", "minecraft:random_chance");
        terms.add(chanceRequirement);
        JsonObject requirements = new JsonObject();
        requirements.addProperty("condition", "minecraft:all_of");
        requirements.add("terms", terms);
        objEffect.add("requirements", requirements);
    }

    /*
    FROST_WALKER - what on earth is this file
    BINDING_CURSE - how do I do this one...
    BANE_OF_ARTHROPODS - slowness effect
    SILK_TOUCH - yeah, I'm at a lose
    INFINITY - special man
    CHANNELING - tricky tricky
    MULTISHOT - hmmmm
    VANISHING_CURSE - yep, special
    */

    private static void modifyValueEffect(String enchantment, String effectName, double base, double perLevel) {
        modifyValueEffect(enchantment, effectName, base, perLevel, "effect");
    }

    private static void modifyValueEffect(String enchantment, String effectName, double base, double perLevel, String componentName) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                "enchantment/"+enchantment,
                "modify_"+enchantment,
                context -> {
                    JsonArray effect = getEffect(effectName, context);
                    JsonElement removedEntry = effect.remove(0);
                    if(!(removedEntry instanceof JsonObject objectEntry)) return;
                    objectEntry.add(componentName, buildAddEffect(base, perLevel));
                    effect.add(objectEntry);
                }
        );
    }

    private static void modifyAttributeEffect(String enchantment, String attribute, double base, double perLevel) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                "enchantment/"+enchantment,
                "modify_"+enchantment,
                context -> {
                    JsonArray effect = getEffect("minecraft:attributes", context);
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
        JsonObject effect = new JsonObject();
        effect.addProperty("type", "minecraft:add");
        JsonObject value = buildLinearValue(base, perLevel);
        effect.add("value", value);
        return effect;
    }

    private static JsonObject buildLinearValue(double base, double perLevel) {
        JsonObject linearValue = new JsonObject();
        linearValue.addProperty("type", "minecraft:linear");
        linearValue.addProperty("base", base);
        linearValue.addProperty("per_level_above_first", perLevel);
        return linearValue;
    }

}
