package net.ramgames.dunchants.mixins;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.ramgames.dunchants.DungeonEnchantsUtils;
import net.ramgames.dunchants.ModItemComponents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.*;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Shadow @Final private Inventory inventory;

    @Shadow @Final private ScreenHandlerContext context;

    @Shadow public abstract void onContentChanged(Inventory inventory);

    @Shadow @Final private Property seed;

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

    @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;<init>(Lnet/minecraft/inventory/Inventory;III)V"), index = 3)
    private int moveAllSlotsDown(int y) {
        return y+8;
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
    private void modifiedOnContentChange(Inventory inventory, CallbackInfo ci) {
        ci.cancel();
        if(inventory != this.inventory) return;
        ItemStack stack = inventory.getStack(0);
        this.context.run((world, pos) -> {
            if(DungeonEnchantsUtils.containsImproperComponent(stack, world))
                inventory.getStack(0).set(ModItemComponents.ENCHANTMENT_OPTIONS, DungeonEnchantsUtils.generateComponent(stack, world));
        });

    }

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "onButtonClick", at = @At("HEAD"), cancellable = true)
    private void enchantItemOnButtonClick(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        ItemStack stack = this.inventory.getStack(0);
        if(stack.get(ModItemComponents.ENCHANTMENT_OPTIONS) == null) return;
        List<List<String>> enchantmentOptions = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(enchantmentOptions.get(id/3).getFirst().equals("locked")) return;
        String enchantId;
        if(enchantmentOptions.get(id/3).size() == 1) enchantId = enchantmentOptions.get(id/3).getFirst();
        else enchantId = enchantmentOptions.get(id/3).get(id % 3);
        if(enchantId.equals("locked")) return;
        Optional<RegistryEntry.Reference<Enchantment>> optionalEnchantment = player.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Identifier.of(enchantId));
        if(optionalEnchantment.isEmpty()) return;
        RegistryEntry.Reference<Enchantment> enchantment = optionalEnchantment.get();
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());
        builder.set(enchantment, builder.getLevel(enchantment)+1);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        stack.set(ModItemComponents.ENCHANTMENT_OPTIONS, DungeonEnchantsUtils.enchantEnchantmentOption(id/3, id%3, stack.get(ModItemComponents.ENCHANTMENT_OPTIONS)));
        this.inventory.markDirty();
        this.seed.set(player.getEnchantmentTableSeed());
        this.onContentChanged(this.inventory);
        player.getWorld().playSound(null, BlockPos.ofFloored(player.getPos()), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, player.getWorld().getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
