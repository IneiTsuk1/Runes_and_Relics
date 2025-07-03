package net.IneiTsuki.regen.client.screen.widgets;

import net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * A custom slot implementation for the output slot of the Spell Inscriber.
 * Prevents item insertion and triggers crafting logic when items are taken.
 */
public class OutputSlot extends Slot {

    private final PlayerEntity player;
    private final SpellInscriberBlockEntity blockEntity;

    /**
     * Creates a new OutputSlot instance.
     *
     * @param inventory   The inventory this slot belongs to.
     * @param index       The slot index in the inventory.
     * @param x           The x position on the screen.
     * @param y           The y position on the screen.
     * @param player      The player interacting with the slot.
     * @param blockEntity The SpellInscriber block entity backing this slot.
     */
    public OutputSlot(Inventory inventory, int index, int x, int y, PlayerEntity player, SpellInscriberBlockEntity blockEntity) {
        super(inventory, index, x, y);
        this.player = player;
        this.blockEntity = blockEntity;

        // Uncomment the following if you want to log a warning when blockEntity is null (usually client side)
        // if (blockEntity == null) {
        //     System.err.println("Warning: OutputSlot constructed with null blockEntity (likely on client side).");
        // }
    }

    /**
     * Prevents insertion into the output slot.
     *
     * @param stack The stack to be inserted.
     * @return false always, to disallow insertion.
     */
    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    /**
     * Allows the player to take items from this slot.
     *
     * @param playerEntity The player attempting to take items.
     * @return true always, to allow taking items.
     */
    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true;
    }

    /**
     * Called when the player takes an item from the slot.
     * Triggers the crafting process in the SpellInscriberBlockEntity.
     *
     * @param player The player taking the item.
     * @param stack  The item stack taken.
     */
    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        super.onTakeItem(player, stack);

        if (blockEntity != null) {
            var world = blockEntity.getWorld();
            if (world != null && !world.isClient) {
                blockEntity.craftItem();
            }
        }
    }
}
