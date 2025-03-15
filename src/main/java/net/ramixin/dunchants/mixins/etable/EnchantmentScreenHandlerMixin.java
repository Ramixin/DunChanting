package net.ramixin.dunchants.mixins.etable;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.Attributions;
import net.ramixin.dunchants.items.components.EnchantmentOption;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.util.ModUtils;
import net.ramixin.util.PlayerEntityDuck;
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
            if(!stack.isEmpty()) ModUtils.updateOptionsIfInvalid(stack, world, playerLevel);
        });

    }

    @Inject(method = "onButtonClick", at = @At("HEAD"), cancellable = true)
    private void enchantItemOnButtonClick(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        ItemStack stack = this.inventory.getStack(0);
        EnchantmentOptions options = stack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(options == null) return;
        if(options.isLocked(id / 3)) return;
        EnchantmentOption option = options.get(id / 3);
        if(option.isLocked(id % 3)) return;
        String enchant = option.get(id % 3);
        RegistryEntry<Enchantment> entry = ModUtils.idToEntry(Identifier.of(enchant), player.getWorld());
        int enchantLevel = EnchantmentHelper.getLevel(entry, stack);
        if(enchantLevel >= 3) return;
        ItemStack lapisStack = this.inventory.getStack(1);
        int lapisLevel = lapisStack.isEmpty() ? 0 : lapisStack.getCount();
        if(lapisLevel < enchantLevel + 1 && !player.isCreative()) return;
        boolean canAfford = ModUtils.canAfford(entry, stack, player);
        if(!canAfford) return;
        boolean markedAsUnavailable = ModUtils.markAsUnavailable(stack, id, enchant);
        if(markedAsUnavailable) return;
        int cost = ModUtils.getEnchantingCost(entry, stack);
        lapisStack.decrement(enchantLevel + 1);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());
        builder.set(entry, enchantLevel+1);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        ModUtils.enchantEnchantmentOption(stack, id/3, id % 3);
        if(!player.isCreative()) {
            if(!stack.contains(ModItemComponents.ATTRIBUTIONS)) stack.set(ModItemComponents.ATTRIBUTIONS, Attributions.createNew());
            Attributions attributions = stack.get(ModItemComponents.ATTRIBUTIONS);
            //noinspection DataFlowIssue
            attributions.addAttribute(id / 3, player.getUuid(), cost);
            stack.set(ModItemComponents.ATTRIBUTIONS, attributions);
            if(player instanceof ServerPlayerEntity serverPlayer) {
                PlayerEntityDuck duck = (PlayerEntityDuck) serverPlayer;
                duck.dungeonEnchants$changeEnchantmentPoints(-cost);
                ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(duck.dungeonEnchants$getEnchantmentPoints()));
            }
        }
        this.inventory.markDirty();
        this.seed.set(player.getEnchantingTableSeed());
        this.onContentChanged(this.inventory);
        player.getWorld().playSound(null, BlockPos.ofFloored(player.getPos()), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, player.getWorld().getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
