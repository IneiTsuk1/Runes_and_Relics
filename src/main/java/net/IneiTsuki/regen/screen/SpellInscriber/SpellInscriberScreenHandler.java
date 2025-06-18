package net.IneiTsuki.regen.screen.SpellInscriber;

import net.IneiTsuki.regen.block.entity.ImplementedInventory;
import net.IneiTsuki.regen.screen.ModScreenHandlers;
import net.IneiTsuki.regen.screen.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SpellInscriberScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    public SpellInscriberScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ImplementedInventory.ofSize(11));
    }

    public SpellInscriberScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.SPELL_INSCRIBER, syncId);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);


        // block slots
        this.addSlot(new Slot(inventory, 0, 49, 20)); // x, y position in GUI
        this.addSlot(new Slot(inventory, 1, 72, 35));
        this.addSlot(new Slot(inventory, 2, 83, 54));
        this.addSlot(new Slot(inventory, 3, 72, 73));
        this.addSlot(new Slot(inventory, 4, 49, 88));
        this.addSlot(new Slot(inventory, 5, 26, 73));
        this.addSlot(new Slot(inventory, 6, 15, 54));
        this.addSlot(new Slot(inventory, 7, 26, 35));
        this.addSlot(new OutputSlot(inventory, 8, 49, 54)); // output slot

        this.addSlot(new Slot(inventory, 9, 127, 25));
        this.addSlot(new Slot(inventory, 10, 127, 83));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }



    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 11 + col * 19, 132 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 10 + col * 19, 188));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            int invSize = inventory.size();
            int playerStart = invSize;
            int totalSlots = this.slots.size();

            if (index < invSize) {
                // Move from custom inventory to player inventory
                if (!this.insertItem(originalStack, playerStart, totalSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to custom input slots (0-8)
                if (!this.insertItem(originalStack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
