package net.ramixin.dunchants.mixins;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.ramixin.dunchants.DungeonEnchantsUtils;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler {

    @Shadow @Final private Inventory inventory;

    @Shadow @Final private ScreenHandlerContext context;

    @Shadow public abstract void onContentChanged(Inventory inventory);

    @Shadow @Final private Property seed;

    @Unique private int playerLevel;

    protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }


    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void setPlayerLevel(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        playerLevel = playerInventory.player.experienceLevel;
    }

    @ModifyArgs(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/EnchantmentScreenHandler$2;<init>(Lnet/minecraft/screen/EnchantmentScreenHandler;Lnet/minecraft/inventory/Inventory;III)V"))
    private void moveItemSlot(Args args) {
        args.set(3, 51);
        args.set(4, 14);
    }

    @ModifyArgs(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/EnchantmentScreenHandler$3;<init>(Lnet/minecraft/screen/EnchantmentScreenHandler;Lnet/minecraft/inventory/Inventory;III)V"))
    private void moveLapizSlot(Args args) {
        args.set(3, 108);
        args.set(4, 14);
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
    private void modifiedOnContentChange(Inventory inventory, CallbackInfo ci) {
        ci.cancel();
        if(inventory != this.inventory) return;
        ItemStack stack = inventory.getStack(0);
        this.context.run((world, pos) -> {
            if(!stack.isEmpty()) DungeonEnchantsUtils.updateOptionsIfInvalid(stack, world, playerLevel);
        });

    }

    @Inject(method = "onButtonClick", at = @At("HEAD"), cancellable = true)
    private void enchantItemOnButtonClick(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        ItemStack stack = this.inventory.getStack(0);
        EnchantmentOptions options = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(options == null) return;
        if(options.isLocked(id / 3)) return;
        Optional<String> enchant = options.get(id / 3).getOptional(id % 3);
        if(enchant.isEmpty()) return;
        Optional<RegistryEntry.Reference<Enchantment>> optionalEnchantment = player.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Identifier.of(enchant.get()));
        if(optionalEnchantment.isEmpty()) return;
        RegistryEntry.Reference<Enchantment> enchantment = optionalEnchantment.get();
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());
        builder.set(enchantment, builder.getLevel(enchantment)+1);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        DungeonEnchantsUtils.enchantEnchantmentOption(stack, id/3, id % 3);
        this.inventory.markDirty();
        this.seed.set(player.getEnchantingTableSeed());
        this.onContentChanged(this.inventory);
        player.getWorld().playSound(null, BlockPos.ofFloored(player.getPos()), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, player.getWorld().getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
