package net.ramixin.dunchanting.mixins.etable;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.ramixin.dunchanting.items.ModItemComponents;
import net.ramixin.dunchanting.items.components.EnchantmentOption;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.util.ModUtils;
import net.ramixin.dunchanting.util.PlayerEntityDuck;
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
        if(playerInventory.player.isCreative()) playerLevel = 30;
        else playerLevel = playerInventory.player.experienceLevel;
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

    @ModifyArgs(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "INVOKE", target =
            //? >=1.21.2 {
            "Lnet/minecraft/screen/EnchantmentScreenHandler;addPlayerSlots(Lnet/minecraft/inventory/Inventory;II)V"
            //?} else
            /*"Lnet/minecraft/screen/slot/Slot;<init>(Lnet/minecraft/inventory/Inventory;III)V"*/
    ))
    private void movePlayerSlots(Args args) {
        int argId;
        //? <=1.21.1 {
        /*argId = 3;
        *///?} else
        argId = 2;

        args.set(argId, (int) args.get(argId) + 8);
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

        if(id == -1) return;
        if(id < 0) {
            if(!options.isLocked(-id / 3)) return;
            PlayerEntityDuck duck = PlayerEntityDuck.get(player);
            int points = duck.dungeonEnchants$getEnchantmentPoints();
            if(points < 1) return;
            EnchantmentOptions newOptions = ModUtils.rerollOption(player.getWorld(), stack, options, playerLevel, -id/3);
            if(newOptions == null) return;
            stack.set(ModItemComponents.ENCHANTMENT_OPTIONS, newOptions);
            if(!player.isCreative()) {
                ModUtils.updateAttributions(stack, 3, 1, player);
                ModUtils.applyPointsAndSend(player, PlayerEntityDuck.get(player)::dungeonEnchants$changeEnchantmentPoints, -1);
            }

            this.onContentChanged(this.inventory);
            player.getWorld().playSound(null, BlockPos.ofFloored(player.getPos()), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.BLOCKS, 1.0F, player.getWorld().getRandom().nextFloat() * 0.1F + 0.9F);
            return;
        }

        if(options.isLocked(id / 3)) return;
        EnchantmentOption option = options.get(id / 3);
        if(option.isLocked(id % 3)) return;
        String enchant = option.get(id % 3);
        RegistryEntry<Enchantment> entry = ModUtils.idToEntry(Identifier.of(enchant), player.getWorld());
        if(entry == null) return;
        if(entry.value() == null) return;
        int enchantLevel = EnchantmentHelper.getLevel(entry, stack);
        if(enchantLevel >= entry.value().getMaxLevel()) return;
        ItemStack lapisStack = this.inventory.getStack(1);
        int lapisLevel = lapisStack.isEmpty() ? 0 : lapisStack.getCount();
        if(lapisLevel < enchantLevel + 1 && !player.isCreative()) return;
        boolean canAfford = ModUtils.canAfford(entry, stack, player);
        if(!canAfford) return;
        boolean markedAsUnavailable = ModUtils.markAsUnavailable(stack, id, enchant, player.getWorld().getRegistryManager()
                /*? >=1.21.2 {*/
                .getOrThrow(RegistryKeys.ENCHANTMENT));
                //?} else
                /*.get(RegistryKeys.ENCHANTMENT));*/
        if(markedAsUnavailable) return;
        int cost = ModUtils.getEnchantingCost(entry, stack);
        lapisStack.decrement(enchantLevel + 1);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());
        builder.set(entry, enchantLevel+1);
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        ModUtils.enchantEnchantmentOption(stack, id/3, id % 3);
        if(!player.isCreative()) {
            ModUtils.updateAttributions(stack, id / 3, cost, player);
            ModUtils.applyPointsAndSend(player, PlayerEntityDuck.get(player)::dungeonEnchants$changeEnchantmentPoints, -cost);
        }
        this.inventory.markDirty();
        this.seed.set(
                player.
                    //? >= 1.21.2 {
                    getEnchantingTableSeed()
                    //?} else
                    /*getEnchantmentTableSeed()*/
        );
        this.onContentChanged(this.inventory);
        player.getWorld().playSound(null, BlockPos.ofFloored(player.getPos()), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, player.getWorld().getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
