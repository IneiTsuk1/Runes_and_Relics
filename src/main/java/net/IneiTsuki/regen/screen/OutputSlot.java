package net.IneiTsuki.regen.screen;

import net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class OutputSlot extends Slot {

    private final PlayerEntity player;
    private final SpellInscriberBlockEntity blockEntity;

    public OutputSlot(Inventory inventory, int index, int x, int y, PlayerEntity player, SpellInscriberBlockEntity blockEntity) {
        super(inventory, index, x, y);
        this.player = player;
        this.blockEntity = blockEntity;

       // if (blockEntity == null) {
          //  System.err.println("Warning: OutputSlot constructed with null blockEntity (likely on client side).");
       // }

    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false; // prevent insertion
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true; // allow taking items
    }

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
