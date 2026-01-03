package net.ramixin.dunchanting.mixins.etable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.EnchantmentSlot;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.util.EnchantmentOptionsUtil;
import net.ramixin.dunchanting.util.ModUtil;
import net.ramixin.dunchanting.util.PlayerDuck;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
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

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu {

    @Shadow @Final private Container enchantSlots;

    @Shadow @Final private ContainerLevelAccess access;

    @Shadow public abstract void slotsChanged(@NonNull Container inventory);

    @Shadow @Final private DataSlot enchantmentSeed;

    @Unique private int playerLevel;

    protected EnchantmentMenuMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void setPlayerLevel(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, CallbackInfo ci) {
        if(inventory.player.isCreative()) playerLevel = 30;
        else playerLevel = inventory.player.experienceLevel;
    }

    @ModifyArgs(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/EnchantmentMenu$2;<init>(Lnet/minecraft/world/inventory/EnchantmentMenu;Lnet/minecraft/world/Container;III)V"))
    private void moveItemSlot(Args args) {
        args.set(3, 51);
        args.set(4, 14);
    }

    @ModifyArgs(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/EnchantmentMenu$3;<init>(Lnet/minecraft/world/inventory/EnchantmentMenu;Lnet/minecraft/world/Container;III)V"))
    private void moveLapizSlot(Args args) {
        args.set(3, 108);
        args.set(4, 14);
    }

    @ModifyArgs(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/EnchantmentMenu;addStandardInventorySlots(Lnet/minecraft/world/Container;II)V"))
    private void movePlayerSlots(Args args) {
        args.set(2, (int) args.get(2) + 8);
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    private void modifiedOnContentChange(Container inventory, CallbackInfo ci) {
        ci.cancel();
        if(inventory != this.enchantSlots) return;
        ItemStack stack = inventory.getItem(0);
        this.access.execute((world, pos) -> EnchantmentOptionsUtil.prepareComponents(stack, world, playerLevel));

    }

    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void enchantItemOnButtonClick(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        ItemStack stack = this.enchantSlots.getItem(0);
        EnchantmentOptionsUtil.prepareComponents(stack, player.level(), player.experienceLevel);
        EnchantmentOptions options = stack.get(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS);
        if(options == null) return;

        if(id == -1) return;
        if(id < 0) {
            if(!options.hasEmptySlot(-id / 3)) return;
            PlayerDuck duck = PlayerDuck.get(player);
            int points = duck.dungeonEnchants$getEnchantmentPoints();
            if(points < 1) return;
            EnchantmentOptionsUtil.unlockOption(stack, -id/3);
            if(!player.isCreative()) {
                ModUtil.updateAttributions(stack, 3, 1, player);
                ModUtil.applyPointsAndSend(player, PlayerDuck.get(player)::dungeonEnchants$changeEnchantmentPoints, -1);
            }

            this.slotsChanged(this.enchantSlots);
            player.level().playSound(null, BlockPos.containing(player.position()), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 1.0F, player.level().getRandom().nextFloat() * 0.1F + 0.9F);
            return;
        }

        if(options.hasEmptySlot(id / 3)) return;
        EnchantmentSlot option = options.getOrThrow(id / 3);
        if(option.isLocked(id % 3)) return;
        Holder<Enchantment> enchant = option.getOrThrow(id % 3);
        if(enchant == null) return;
        int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(enchant, stack);
        if(enchantLevel >= enchant.value().getMaxLevel()) return;
        ItemStack lapisStack = this.enchantSlots.getItem(1);
        int lapisLevel = lapisStack.isEmpty() ? 0 : lapisStack.getCount();
        if(lapisLevel < enchantLevel + 1 && !player.isCreative()) return;
        boolean canAfford = ModUtil.canAfford(enchant, stack, player);
        if(!canAfford) return;
        boolean markedAsUnavailable = ModUtil.markAsUnavailable(stack, id, enchant);
        if(markedAsUnavailable) return;
        int cost = ModUtil.getEnchantingCost(enchant, stack);
        lapisStack.shrink(enchantLevel + 1);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(stack.getEnchantments());
        builder.set(enchant, enchantLevel+1);
        stack.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
        ModUtil.enchantEnchantmentOption(stack, id/3, id % 3);
        if(!player.isCreative()) {
            ModUtil.updateAttributions(stack, id / 3, cost, player);
            ModUtil.applyPointsAndSend(player, PlayerDuck.get(player)::dungeonEnchants$changeEnchantmentPoints, -cost);
        }
        this.enchantSlots.setChanged();
        this.enchantmentSeed.set(player.getEnchantmentSeed());
        this.slotsChanged(this.enchantSlots);
        player.level().playSound(null, BlockPos.containing(player.position()), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, player.level().getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
