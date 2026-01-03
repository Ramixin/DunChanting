package net.ramixin.dunchanting.menus;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.ramixin.dunchanting.AttributionManager;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.ModDataComponents;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.EnchantmentOptionsUtil;
import org.jspecify.annotations.NonNull;

public class ModGrindstoneMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess context;
    private final Container inventory;
    private final int playerLevel;

    public ModGrindstoneMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ModGrindstoneMenu(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModMenus.MOD_GRINDSTONE_HANDLER_TYPE, syncId);
        this.context = context;
        this.inventory = new SimpleContainer(1);
        this.addSlot(new Slot(inventory, 0, 80, 6));
        this.addStandardInventorySlots(playerInventory, 8, 100);
        this.playerLevel = playerInventory.player.experienceLevel;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotId) {
        ItemStack itemStack = getSlot(slotId).getItem();
        if(slotId == 0) {
            if (!this.moveItemStackTo(itemStack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(itemStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return stillValid(this.context, player, Blocks.GRINDSTONE);
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        this.context.execute((world, pos) -> this.clearContainer(player, inventory));
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int id) {
        ItemStack itemStack = getSlot(0).getItem();
        SelectedEnchantments selected = itemStack.getOrDefault(ModDataComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        EnchantmentOptions options = EnchantmentOptionsUtil.getUnlocked(itemStack);
        if(options == EnchantmentOptions.DEFAULT) return false;
        if(!selected.hasSelection(id)) return false;

        if(player.level() instanceof ServerLevel serverWorld) {
            AttributionManager.redistribute(itemStack, serverWorld, id);
            this.context.execute((world, pos) -> world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS));
        }
        int optionId = selected.get(id);
        Holder<Enchantment> enchant = options.getOrThrow(id).getOrThrow(optionId);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(itemStack));
        builder.removeIf(entry -> entry.equals(enchant));
        itemStack.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
        SelectedEnchantments disenchanted = selected.disenchantOption(id);
        itemStack.set(ModDataComponents.SELECTED_ENCHANTMENTS, disenchanted);
        this.inventory.setChanged();

        return true;
    }

    @Override
    public void slotsChanged(@NonNull Container inventory) {
        super.slotsChanged(inventory);
        ItemStack stack = inventory.getItem(0);
        this.context.execute((world, pos) -> {
            if(!stack.isEmpty()) EnchantmentOptionsUtil.prepareComponents(stack, world, playerLevel);
        });
    }
}
