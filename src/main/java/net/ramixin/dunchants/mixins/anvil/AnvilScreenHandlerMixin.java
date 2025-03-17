package net.ramixin.dunchants.mixins.anvil;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.ramixin.dunchants.DungeonEnchants;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.util.ClickableHandler;
import net.ramixin.dunchants.util.ModUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler implements ClickableHandler {

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Shadow public abstract void updateResult();

    @Shadow @Final private Property levelCost;
    @Unique
    private int replacingEnchantment = -1;

    @Unique
    private int replacementEnchantment = -1;

    @WrapOperation(method = "getForgingSlotsManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;input(IIILjava/util/function/Predicate;)Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;", ordinal = 0))
    private static ForgingSlotsManager.Builder moveFirstInputSlot(ForgingSlotsManager.Builder instance, int slotId, int x, int y, Predicate<ItemStack> mayPlace, Operation<ForgingSlotsManager.Builder> original) {
        return original.call(instance, slotId, x + 9, 6, mayPlace);
    }

    @WrapOperation(method = "getForgingSlotsManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;input(IIILjava/util/function/Predicate;)Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;", ordinal = 1))
    private static ForgingSlotsManager.Builder moveSecondInputSlot(ForgingSlotsManager.Builder instance, int slotId, int x, int y, Predicate<ItemStack> mayPlace, Operation<ForgingSlotsManager.Builder> original) {
        return original.call(instance, slotId, x - 10, 6, mayPlace);
    }

    @WrapOperation(method = "getForgingSlotsManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;output(III)Lnet/minecraft/screen/slot/ForgingSlotsManager$Builder;"))
    private static ForgingSlotsManager.Builder moveOutputSlot(ForgingSlotsManager.Builder instance, int slotId, int x, int y, Operation<ForgingSlotsManager.Builder> original) {
        return original.call(instance, slotId, x - 11, 6);
    }

    @WrapOperation(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperienceLevels(I)V"))
    private void enforceNoXpLossOnUse(PlayerEntity instance, int levels, Operation<Void> original) {
        // prevent xp removal
    }

    @Override
    public boolean dungeonEnchants$onClick(PlayerEntity player, int button) {
        DungeonEnchants.LOGGER.info("clicked: {}", button);
        int replacing = button & 0xFF;
        int replacement = button >> 16 & 0xFF;
        DungeonEnchants.LOGGER.info("replacement: {}", replacement);
        this.replacementEnchantment = replacement;
        DungeonEnchants.LOGGER.info("replacing: {}", replacing);
        this.replacingEnchantment = replacing;
        updateResult();
        return true;
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void modifyOutput(CallbackInfo ci) {
        ItemStack primary = this.input.getStack(0);
        ItemStack secondary = this.input.getStack(1);
        if(EnchantmentHelper.canHaveEnchantments(primary) && secondary.isOf(Items.ENCHANTED_BOOK)) {
            if(this.replacingEnchantment == -1 || this.replacementEnchantment == -1) {
                this.output.setStack(0, ItemStack.EMPTY);
                ci.cancel();
                return;
            }
            ItemStack copy = primary.copy();
            EnchantmentOptions options = copy.get(ModItemComponents.ENCHANTMENT_OPTIONS);
            if(options == null) {
                this.output.setStack(0, ItemStack.EMPTY);
                ci.cancel();
                return;
            }
            List<RegistryEntry<Enchantment>> orderedEnchantments = ModUtils.getOrderedEnchantments(secondary, player.getWorld().getRegistryManager());
            RegistryEntry<Enchantment> replacement = orderedEnchantments.get(replacementEnchantment / 3);
            EnchantmentOptions newOptions = options.modify(replacement.getIdAsString(), replacingEnchantment / 3, replacingEnchantment % 3);
            copy.set(ModItemComponents.ENCHANTMENT_OPTIONS, newOptions);
            this.output.setStack(0, copy);
            levelCost.set(0);
            DungeonEnchants.LOGGER.info("can take: {}", this.output.canPlayerUse(player));
            this.sendContentUpdates();
            ci.cancel();
        } else {
            this.replacingEnchantment = -1;
            this.replacementEnchantment = -1;
        }
    }

    @ModifyReturnValue(method = "canTakeOutput", at = @At("RETURN"))
    private boolean forceCanTakeOutput(boolean original) {
        return true;
    }
}
