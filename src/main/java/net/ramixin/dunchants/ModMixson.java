package net.ramixin.dunchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.ramixin.mixson.inline.EventContext;
import net.ramixin.mixson.inline.Mixson;
import org.jetbrains.annotations.Nullable;

public class ModMixson {

    private static final JsonObject CHANCE_REQUIREMENT;
    private static final JsonObject LEVELED_CHANCE_SILK_TOUCH = buildLeveledChancePredicate("minecraft:silk_touch");

    static {

        CHANCE_REQUIREMENT = new JsonObject();
        JsonObject chance = new JsonObject();
        chance.addProperty("type", "minecraft:enchantment_level");
        chance.add("amount", buildLinearValue(0.334, 0.333));
        CHANCE_REQUIREMENT.add("chance", chance);
        CHANCE_REQUIREMENT.addProperty("condition", "minecraft:random_chance");

    }

    public static void onInitialize() {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                id -> id.getPath().startsWith("enchantment/"),
                "ChangeEnchantmentMaxLevel",
                context -> context.getFile().getAsJsonObject().addProperty("max_level", 3),
                true
        );

        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                id -> id.getPath().startsWith("loot_table/blocks/"),
                "AddLevelingToSilkTouch",
                context -> {
                    JsonArray pools = context.getFile().getAsJsonObject().getAsJsonArray("pools");
                    if(pools == null) return;
                    for (JsonElement pool : pools) {
                        JsonObject poolObject = pool.getAsJsonObject();

                        if(poolObject.has("conditions")) {
                            JsonArray conditions = poolObject.getAsJsonArray("conditions");
                            JsonObject matchToolPredicates = containsSilkTouchCondition(conditions);
                            if(matchToolPredicates != null) matchToolPredicates.add("dungeon_enchants:leveled_chance", LEVELED_CHANCE_SILK_TOUCH);
                        }

                        JsonArray entries = poolObject.getAsJsonArray("entries");
                        for(JsonElement entry : entries) {
                            JsonObject entryObject = entry.getAsJsonObject();
                            if(!entryObject.has("type")) continue;
                            if(!entryObject.get("type").getAsString().equals("minecraft:alternatives")) continue;
                            JsonArray children = entryObject.getAsJsonArray("children");
                            for (JsonElement child : children) {
                                JsonObject childObject = child.getAsJsonObject();
                                if(!childObject.has("conditions")) continue;
                                JsonArray conditions = childObject.getAsJsonArray("conditions");
                                JsonObject matchToolPredicates = containsSilkTouchCondition(conditions);
                                if(matchToolPredicates == null) continue;
                                matchToolPredicates.add("dungeon_enchants:leveled_chance", LEVELED_CHANCE_SILK_TOUCH);

                            }
                        }
                    }
                },
                true
        );

        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                "enchantment/frost_walker",
                "RebalanceFrostWalker",
                context -> {
                    JsonArray effectArray = getEffect("minecraft:location_changed", context);
                    if(!(effectArray.get(0) instanceof JsonObject obj)) return;
                    JsonObject effect = obj.getAsJsonObject("effect");
                    JsonObject radius = effect.getAsJsonObject("radius");
                    radius.add("value", buildLinearValue(2, 1));
                },
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
        modifyAttributeEffect("efficiency", "minecraft:mining_efficiency", 10, 8);
        modifyValueEffect("power", "minecraft:damage", 1.5, 0.5);
        modifyValueEffect("punch", "minecraft:knockback", 1, 0.5);
        modifyValueEffect("luck_of_the_sea", "minecraft:fishing_luck_bonus", 0.5, 0.25);
        modifyValueEffect("impaling", "minecraft:damage", 4.5, 4);
        modifyValueEffect("piercing", "minecraft:projectile_piercing", 2, 1);
        modifyValueEffect("density", "minecraft:smash_damage_per_fallen_block", 1.5, 0.5);
        modifyValueEffect("breach", "minecraft:armor_effectiveness", -0.2, -0.2);
        modifyValueEffect("fire_aspect", "minecraft:post_attack", 3, 2.5, "duration");
        modifyValueEffect("flame", "minecraft:projectile_spawned", 40, 30, "duration");
        modifyRequirements("infinity", "minecraft:ammo_use", CHANCE_REQUIREMENT);
        modifyRequirements("channeling", "minecraft:post_attack", CHANCE_REQUIREMENT);
        modifyRequirements("channeling", "minecraft:hit_block", CHANCE_REQUIREMENT);
        modifyRequirements("bane_of_arthropods", "minecraft:post_attack", CHANCE_REQUIREMENT);
        replaceUnitEffect("minecraft:prevent_equipment_drop", "dungeon_enchants:leveled_prevent_equipment_drop");
        replaceUnitEffect("minecraft:prevent_armor_change", "dungeon_enchants:leveled_prevent_armor_change");
        modifyValueEffect("multishot", "minecraft:projectile_count", 0.75, 0.75);
        modifyValueEffect("multishot", "minecraft:projectile_spread", 10, 0);
    }


    private static @Nullable JsonObject containsSilkTouchCondition(JsonArray conditions) {
        for (JsonElement condition : conditions) {
            JsonObject conditionObject = condition.getAsJsonObject();
            if(!conditionObject.get("condition").getAsString().equals("minecraft:match_tool")) continue;
            JsonObject predicate = conditionObject.getAsJsonObject("predicate");
            JsonObject predicates = predicate.getAsJsonObject("predicates");
            if(predicates == null) continue;
            if(!predicates.has("minecraft:enchantments")) continue;
            JsonArray enchantments = predicates.getAsJsonArray("minecraft:enchantments");
            for (JsonElement enchantment : enchantments) {
                JsonObject enchantmentObject = enchantment.getAsJsonObject();
                if(enchantmentObject.get("enchantments").getAsString().equals("minecraft:silk_touch")) return predicates;
            }
        }
        return null;
    }

    private static void replaceUnitEffect(String unitEffect, String newUnitEffect) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                id -> id.getPath().startsWith("enchantment/"),
                "replaceUnitEffect_" + unitEffect,
                context -> {
                    JsonObject file = context.getFile().getAsJsonObject();
                    if(!file.has("effects")) return;
                    JsonObject effects = file.getAsJsonObject("effects");
                    if(!effects.has(unitEffect)) return;
                    effects.remove(unitEffect);
                    effects.add(newUnitEffect, new JsonObject());
                },
                true
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static void modifyRequirements(String enchantment, String effectName, JsonObject requirement) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                "enchantment/"+enchantment,
                "addChance_"+enchantment,
                context -> {
                    JsonArray effect = getEffect(effectName, context);
                    JsonElement entry = effect.get(0);
                    if(!(entry instanceof JsonObject objEffect)) return;
                    addRequirement(objEffect, requirement);
                },
                false
        );
    }

    private static void addRequirement(JsonObject entry, JsonObject requirement) {
        if(!entry.has("requirements")) {
            entry.add("requirements", requirement);
            return;
        }
        JsonObject requirements = entry.getAsJsonObject("requirements");
        if(requirements.get("condition").getAsString().equals("minecraft:all_of")) {
            JsonArray terms = requirements.getAsJsonArray("terms");
            terms.add(requirement);
            return;
        }
        JsonObject allOfRequirement = new JsonObject();
        allOfRequirement.addProperty("condition", "minecraft:all_of");
        JsonArray terms = new JsonArray();
        terms.add(requirements);
        terms.add(requirement);
        allOfRequirement.add("terms", terms);
        entry.add("requirements", allOfRequirement);
    }

    private static void modifyValueEffect(String enchantment, String effectName, double base, double perLevel) {
        modifyValueEffect(enchantment, effectName, base, perLevel, "effect");
    }

    private static void modifyValueEffect(String enchantment, String effectName, double base, double perLevel, String componentName) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                "enchantment/"+enchantment,
                "modifyValue_"+enchantment,
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
                "modifyAttribute_"+enchantment,
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

    @SuppressWarnings("SameParameterValue")
    private static JsonObject buildLeveledChancePredicate(String enchantment) {
        JsonObject shell = new JsonObject();
        shell.addProperty("enchantment", enchantment);
        return shell;
    }

}
