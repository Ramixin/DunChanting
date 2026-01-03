package net.ramixin.dunchanting.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.ramixin.dunchanting.Dunchanting;

public class ModFunctionTypes {

    public static final LootItemFunctionType<GildFunction> GILD = register("gild", GildFunction.CODEC);

    private static <T extends LootItemFunction> LootItemFunctionType<T> register(String string, MapCodec<T> mapCodec) {
        return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Dunchanting.id(string), new LootItemFunctionType<>(mapCodec));
    }

    public static void onInitialize() {}

}
