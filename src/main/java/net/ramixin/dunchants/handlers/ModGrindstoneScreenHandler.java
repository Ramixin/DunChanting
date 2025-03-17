package net.ramixin.dunchants.handlers;

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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.ramixin.dunchants.AttributionManager;
import net.ramixin.dunchants.items.ModItemComponents;
import net.ramixin.dunchants.items.components.EnchantmentOptions;
import net.ramixin.dunchants.items.components.SelectedEnchantments;
import net.ramixin.dunchants.util.ModUtils;

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
        this.addPlayerSlots(playerInventory, 8, 100);
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

        if(player.getWorld() instanceof ServerWorld serverWorld) {
            AttributionManager.redistribute(itemStack, serverWorld, id);
            this.context.run((world, pos) -> world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS));
        }
        int optionId = selected.get(id);
        String enchant = options.get(id).get(optionId);
        RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(enchant));
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(itemStack));
        builder.remove(entry -> entry.matchesKey(key));
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
