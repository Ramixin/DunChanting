package net.ramixin.dunchanting.mixins.anvil;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.ramixin.dunchanting.AttributionManager;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.util.ClickableHandler;
import net.ramixin.dunchanting.util.EnchantmentOptionsUtil;
import net.ramixin.dunchanting.util.ModUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Debug(export = true)
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu implements ClickableHandler {


    public AnvilMenuMixin(@Nullable MenuType<?> type, int syncId, Inventory playerInventory, ContainerLevelAccess context, ItemCombinerMenuSlotDefinition forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Shadow public abstract void createResult();

    @Shadow @Final private DataSlot cost;
    @Unique
    private int replacingEnchantment = -1;

    @Unique
    private int replacementEnchantment = -1;

    @WrapOperation(method = "createInputSlotDefinitions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;withSlot(IIILjava/util/function/Predicate;)Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;", ordinal = 0))
    private static ItemCombinerMenuSlotDefinition.Builder moveFirstInputSlot(ItemCombinerMenuSlotDefinition.Builder instance, int slotId, int x, int y, Predicate<ItemStack> mayPlace, Operation<ItemCombinerMenuSlotDefinition.Builder> original) {
        return original.call(instance, slotId, x + 9, 6, mayPlace);
    }

    @WrapOperation(method = "createInputSlotDefinitions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;withSlot(IIILjava/util/function/Predicate;)Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;", ordinal = 1))
    private static ItemCombinerMenuSlotDefinition.Builder moveSecondInputSlot(ItemCombinerMenuSlotDefinition.Builder instance, int slotId, int x, int y, Predicate<ItemStack> mayPlace, Operation<ItemCombinerMenuSlotDefinition.Builder> original) {
        return original.call(instance, slotId, x - 10, 6, mayPlace);
    }

    @WrapOperation(method = "createInputSlotDefinitions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;withResultSlot(III)Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;"))
    private static ItemCombinerMenuSlotDefinition.Builder moveOutputSlot(ItemCombinerMenuSlotDefinition.Builder instance, int slotId, int x, int y, Operation<ItemCombinerMenuSlotDefinition.Builder> original) {
        return original.call(instance, slotId, x - 11, 6);
    }

    @WrapOperation(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"))
    private void enforceNoXpLossOnUse(Player instance, int levels, Operation<Void> original) {
        // prevent xp removal
    }

    @Override
    public boolean dungeonEnchants$onClick(Player player, int button) {
        int replacing = button & 0xFF;
        this.replacementEnchantment = button >> 16 & 0xFF;
        this.replacingEnchantment = replacing;
        createResult();
        return true;
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void modifyOutput(CallbackInfo ci) {
        ItemStack primary = this.inputSlots.getItem(0);
        EnchantmentOptionsUtil.prepareComponents(primary, player.level(), player.experienceLevel);
        ItemStack secondary = this.inputSlots.getItem(1);
        if(EnchantmentHelper.canStoreEnchantments(primary) && secondary.is(Items.ENCHANTED_BOOK)) {
            if(this.replacingEnchantment == -1 || this.replacementEnchantment == -1) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                ci.cancel();
                return;
            }
            ItemStack copy = primary.copy();
            EnchantmentOptions options = copy.get(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS);
            if(options == null) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                ci.cancel();
                return;
            }
            List<Holder<Enchantment>> orderedEnchantments = ModUtil.getOrderedEnchantments(secondary);
            Holder<Enchantment> replacement = orderedEnchantments.get(replacementEnchantment / 3);
            EnchantmentOptions newOptions = options.withEnchantment(replacement, replacingEnchantment / 3, replacingEnchantment % 3);
            copy.set(ModDataComponents.UNLOCKED_ENCHANTMENT_OPTIONS, newOptions);

            this.resultSlots.setItem(0, copy);
            cost.set(0);
            this.broadcastChanges();
            ci.cancel();
        } else {
            this.replacingEnchantment = -1;
            this.replacementEnchantment = -1;
        }
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void ensureEnchantmentsDoNotTransferToOutput(CallbackInfo ci) {
        ItemStack stack = this.resultSlots.getItem(0);
        ItemStack primary = this.inputSlots.getItem(0);
        stack.set(DataComponents.ENCHANTMENTS, primary.get(DataComponents.ENCHANTMENTS));
    }

    @WrapOperation(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V", ordinal = 2))
    private void updateEnchantmentsOnEnchantedBook(Container instance, int i, ItemStack ignored, Operation<Void> original) {
        if(!(player.level() instanceof ServerLevel serverLevel)) {
            original.call(instance, i, ignored);
            return;
        }
        ItemStack stack = instance.getItem(1);
        if(stack.getItem() != Items.ENCHANTED_BOOK) {
            if(stack.has(ModDataComponents.ATTRIBUTIONS))
                AttributionManager.redistribute(stack, serverLevel);
            original.call(instance, i, ignored);
            return;
        }
        ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if(enchantments == null) {
            original.call(instance, i, stack);
            return;
        }
        List<Holder<Enchantment>> orderedEnchantments = ModUtil.getOrderedEnchantments(stack);
        Holder<Enchantment> enchantmentEntry = orderedEnchantments.get(replacementEnchantment / 3);
        int level = enchantments.getLevel(enchantmentEntry);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(enchantments);
        if(level == 1) builder.removeIf(entry -> entry.equals(enchantmentEntry));
        else builder.set(enchantmentEntry, level - 1);
        ItemEnchantments finalEnchants = builder.toImmutable();
        stack.set(DataComponents.STORED_ENCHANTMENTS, finalEnchants);
        if(finalEnchants.isEmpty()) original.call(instance, i, ItemStack.EMPTY);
    }

    @ModifyReturnValue(method = "mayPickup", at = @At("RETURN"))
    private boolean forceCanTakeOutput(boolean original) {
        return true;
    }
}
