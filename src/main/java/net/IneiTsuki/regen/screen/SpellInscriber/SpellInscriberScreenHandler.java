package net.IneiTsuki.regen.screen.SpellInscriber;

import net.IneiTsuki.regen.block.entity.ImplementedInventory;
import net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity;
import net.IneiTsuki.regen.screen.ModScreenHandlers;
import net.IneiTsuki.regen.screen.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class SpellInscriberScreenHandler extends ScreenHandler {

    @Nullable
    private final SpellInscriberBlockEntity blockEntity;
    private final Inventory inventory;

    public SpellInscriberScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public SpellInscriberScreenHandler(int syncId, PlayerInventory playerInventory, @Nullable SpellInscriberBlockEntity blockEntity) {
        super(ModScreenHandlers.SPELL_INSCRIBER, syncId);

        this.blockEntity = blockEntity;
        this.inventory = blockEntity != null
                ? blockEntity
                : ImplementedInventory.of(DefaultedList.ofSize(11, ItemStack.EMPTY));

        if (blockEntity != null) {
            blockEntity.onOpen(playerInventory.player);
        }

        setupCustomSlots(playerInventory.player);
        setupPlayerInventory(playerInventory);
        setupPlayerHotbar(playerInventory);

        if (blockEntity != null) {
            blockEntity.updateOutputSlot();
        }
    }

    private void setupCustomSlots(PlayerEntity player) {
        // Input slots (0–7)
        int[][] inputPositions = {
                {49, 20}, {72, 35}, {83, 54}, {72, 73},
                {49, 88}, {26, 73}, {15, 54}, {26, 35}
        };
        for (int i = 0; i < inputPositions.length; i++) {
            this.addSlot(new Slot(inventory, i, inputPositions[i][0], inputPositions[i][1]));
        }

        // Output slot (8)
        this.addSlot(new OutputSlot(inventory, 8, 49, 54, player, blockEntity));

        // Optional extra slots (9–10)
        this.addSlot(new Slot(inventory, 9, 127, 25));
        this.addSlot(new Slot(inventory, 10, 127, 83));
    }

    private void setupPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                int x = 11 + col * 19;
                int y = 132 + row * 18;
                this.addSlot(new Slot(playerInventory, index, x, y));
            }
        }
    }

    private void setupPlayerHotbar(PlayerInventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 10 + col * 19, 188));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (blockEntity != null) {
            blockEntity.updateOutputSlot();
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getStack();
        ItemStack newStack = originalStack.copy();

        int customInvSize = inventory.size();
        int totalSlots = this.slots.size();

        if (index < customInvSize) {
            // Move from custom to player inventory
            if (!this.insertItem(originalStack, customInvSize, totalSlots, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Move from player to custom input (slots 0–7)
            if (!this.insertItem(originalStack, 0, 8, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
