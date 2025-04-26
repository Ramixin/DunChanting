package net.ramixin.dunchanting.mixins.attribution;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.ramixin.dunchanting.AttributionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin {

    @SuppressWarnings("deprecation")
    @Inject(method = "readProperties", at = @At("TAIL"))
    private static <T> void loadAttributionData(Dynamic<T> dynamic, LevelInfo info, LevelProperties.SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        NbtCompound root = dynamic.get("DungeonEnchants").flatMap(NbtCompound.CODEC::parse).result().orElse(new NbtCompound());
        AttributionManager.load(root);
    }

    @Inject(method = "updateProperties", at = @At("TAIL"))
    private void saveAttributionData(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci) {
        NbtCompound root = new NbtCompound();
        AttributionManager.save(root);
        levelNbt.put("DungeonEnchants", root);
    }

}
