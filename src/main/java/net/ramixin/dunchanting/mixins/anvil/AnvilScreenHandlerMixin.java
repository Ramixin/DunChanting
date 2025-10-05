package net.ramixin.dunchanting.mixins.anvil;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import net.ramixin.dunchanting.util.ClickableHandler;
import net.ramixin.dunchanting.util.ModUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Debug(export = true)
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
        int replacing = button & 0xFF;
        this.replacementEnchantment = button >> 16 & 0xFF;
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
            List<RegistryEntry<Enchantment>> orderedEnchantments = ModUtils.getOrderedEnchantments(secondary);
            RegistryEntry<Enchantment> replacement = orderedEnchantments.get(replacementEnchantment / 3);
            EnchantmentOptions newOptions = options.withEnchantment(replacement, replacingEnchantment / 3, replacingEnchantment % 3);
            copy.set(ModItemComponents.ENCHANTMENT_OPTIONS, newOptions);
            this.output.setStack(0, copy);
            levelCost.set(0);
            this.sendContentUpdates();
            ci.cancel();
        } else {
            this.replacingEnchantment = -1;
            this.replacementEnchantment = -1;
        }
    }

    @WrapOperation(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 2))
    private void updateEnchantmentsOnEnchantedBook(Inventory instance, int i, ItemStack ignored, Operation<Void> original) {
        ItemStack stack = this.input.getStack(1);
        if(stack.getItem() != Items.ENCHANTED_BOOK) {
            original.call(instance, i, stack);
            return;
        }
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if(enchantments == null) {
            original.call(instance, i, stack);
            return;
        }
        List<RegistryEntry<Enchantment>> orderedEnchantments = ModUtils.getOrderedEnchantments(stack);
        RegistryEntry<Enchantment> enchantmentEntry = orderedEnchantments.get(replacementEnchantment / 3);
        int level = enchantments.getLevel(enchantmentEntry);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
        if(level == 1) {
            builder.remove(entry -> entry.equals(enchantmentEntry));
        }
        else builder.set(enchantmentEntry, level - 1);
        ItemEnchantmentsComponent finalEnchants = builder.build();
        stack.set(DataComponentTypes.STORED_ENCHANTMENTS, finalEnchants);
        if(finalEnchants.isEmpty()) original.call(instance, i, ItemStack.EMPTY);
    }

    @ModifyReturnValue(method = "canTakeOutput", at = @At("RETURN"))
    private boolean forceCanTakeOutput(boolean original) {
        return true;
    }
}
