package net.ramixin.dunchanting.handlers;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.ramixin.dunchanting.AttributionManager;
import net.ramixin.dunchanting.items.components.EnchantmentOptions;
import net.ramixin.dunchanting.items.components.ModItemComponents;
import net.ramixin.dunchanting.items.components.SelectedEnchantments;
import net.ramixin.dunchanting.util.ModUtils;

public class ModGrindstoneScreenHandler extends ScreenHandler {

    private final ScreenHandlerContext context;
    private final Inventory inventory;
    private final int playerLevel;

    public ModGrindstoneScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ModGrindstoneScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModHandlers.MOD_GRINDSTONE_HANDLER_TYPE, syncId);
        this.context = context;
        this.inventory = new SimpleInventory(1);
        this.addSlot(new Slot(inventory, 0, 80, 6));
        //? >=1.21.2 {
        this.addPlayerSlots(playerInventory, 8, 100);
        //?} else {
        /*for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 100 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 158));
        }
        *///?}
        this.playerLevel = playerInventory.player.experienceLevel;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotId) {
        ItemStack itemStack = getSlot(slotId).getStack();
        if(slotId == 0) {
            if (!this.insertItem(itemStack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(itemStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Blocks.GRINDSTONE);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, inventory));
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        ItemStack itemStack = getSlot(0).getStack();
        SelectedEnchantments selected = itemStack.getOrDefault(ModItemComponents.SELECTED_ENCHANTMENTS, SelectedEnchantments.DEFAULT);
        EnchantmentOptions options = itemStack.get(ModItemComponents.ENCHANTMENT_OPTIONS);
        if(options == null) return false;
        if(!selected.hasSelection(id)) return false;

        if(player.getEntityWorld() instanceof ServerWorld serverWorld) {
            AttributionManager.redistribute(itemStack, serverWorld, id);
            this.context.run((world, pos) -> world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS));
        }
        int optionId = selected.get(id);
        RegistryEntry<Enchantment> enchant = options.getOrThrow(id).getOrThrow(optionId);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(itemStack));
        builder.remove(entry -> entry.equals(enchant));
        itemStack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        SelectedEnchantments disenchanted = selected.disenchantOption(id);
        itemStack.set(ModItemComponents.SELECTED_ENCHANTMENTS, disenchanted);
        this.inventory.markDirty();

        return true;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        ItemStack stack = inventory.getStack(0);
        this.context.run((world, pos) -> {
            if(!stack.isEmpty()) ModUtils.updateOptionsIfInvalid(stack, world, playerLevel);
        });
    }
}
