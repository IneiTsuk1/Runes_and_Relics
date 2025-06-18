package net.IneiTsuki.regen.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class OutputSlot extends Slot {

    public OutputSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false; // prevents players from putting items in
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true; // allow taking items
    }
}