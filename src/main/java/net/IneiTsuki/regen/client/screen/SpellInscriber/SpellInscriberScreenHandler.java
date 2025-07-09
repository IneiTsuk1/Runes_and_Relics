package net.IneiTsuki.regen.client.screen.SpellInscriber;

import net.IneiTsuki.regen.block.entity.ImplementedInventory;
import net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity;
import net.IneiTsuki.regen.client.screen.handlers.ModScreenHandlers;
import net.IneiTsuki.regen.screen.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import static net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity.INPUT_SLOTS;
import static net.IneiTsuki.regen.block.entity.SpellInscriberBlockEntity.OUTPUT_SLOT;

/**
 * ScreenHandler for the Spell Inscriber block.
 * <p>
 * Manages the container inventory slots and player inventory integration.
 * Handles item transfer, slot layout, and syncing output slot updates.
 */
public class SpellInscriberScreenHandler extends ScreenHandler {

    /** The block entity backing this screen handler, nullable on client side before block entity sync. */
    @Nullable
    private final SpellInscriberBlockEntity blockEntity;

    /** The inventory backing this screen handler, either from the block entity or a dummy inventory. */
    private final Inventory inventory;

    /**
     * Constructor for client-side only, no block entity present.
     *
     * @param syncId         sync id from client-server screen sync
     * @param playerInventory the player's inventory
     */
    public SpellInscriberScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    /**
     * Constructor for server-side with block entity reference.
     *
     * @param syncId         sync id from client-server screen sync
     * @param playerInventory the player's inventory
     * @param blockEntity    the Spell Inscriber block entity backing this container, or null on client
     */
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

    /**
     * Adds the custom block inventory slots:
     * - Input slots (0–7) arranged in a pattern.
     * - Output slot (8), uses custom OutputSlot to restrict interaction.
     * - Two optional extra slots (9–10) at fixed positions.
     *
     * @param player the player interacting with this container
     */
    private void setupCustomSlots(PlayerEntity player) {
        // Input slots (0–7) coordinates
        int[][] inputPositions = {
                {49, 20}, {72, 35}, {83, 54}, {72, 73},
                {49, 88}, {26, 73}, {15, 54}, {26, 35}
        };
        for (int i = 0; i < inputPositions.length; i++) {
            this.addSlot(new Slot(inventory, i, inputPositions[i][0], inputPositions[i][1]));
        }

        // Output slot (8) with special behavior
        this.addSlot(new OutputSlot(inventory, 8, 49, 54, player, blockEntity));

        // Optional extra slots (9–10)
        this.addSlot(new Slot(inventory, 9, 127, 25));
        this.addSlot(new Slot(inventory, 10, 127, 83));
    }

    /**
     * Adds the player's main inventory slots (3 rows of 9 columns).
     *
     * @param playerInventory the player's inventory
     */
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

    /**
     * Adds the player's hotbar slots (1 row of 9 columns).
     *
     * @param playerInventory the player's inventory
     */
    private void setupPlayerHotbar(PlayerInventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 10 + col * 19, 188));
        }
    }

    /**
     * Called when the inventory content changes.
     * Triggers an update of the output slot in the block entity if available.
     *
     * @param inventory the inventory that changed
     */
    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (blockEntity != null) {
            blockEntity.updateOutputSlot();
        }
    }

    /**
     * Handles shift-click item transfer between player inventory and the custom block inventory.
     *
     * @param player the player interacting with the container
     * @param index  the slot index clicked
     * @return the resulting item stack after the move, or empty if none
     */
    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getStack();
        ItemStack stackCopy = originalStack.copy();

        int customInvSize = inventory.size(); // e.g. 11
        int playerInvStart = customInvSize;
        int playerInvEnd = this.slots.size();

        if (index == OUTPUT_SLOT) {
            // Handle output slot shift-click - craft as many as possible
            if (blockEntity != null) {
                // Keep crafting and transferring until we can't anymore
                ItemStack transferredStack = ItemStack.EMPTY;
                int craftAttempts = 0;
                final int maxCraftAttempts = 64; // Safety limit

                while (slot.hasStack() && craftAttempts < maxCraftAttempts) {
                    craftAttempts++;
                    ItemStack currentOutput = slot.getStack().copy();

                    // Try to insert current output into player inventory
                    if (!this.insertItem(currentOutput, playerInvStart, playerInvEnd, true)) {
                        break; // Player inventory is full
                    }

                    // Successfully transferred, so consume the output and craft more
                    slot.setStack(ItemStack.EMPTY);

                    // Track what we've transferred for return value
                    if (transferredStack.isEmpty()) {
                        transferredStack = currentOutput.copy();
                    } else {
                        transferredStack.increment(currentOutput.getCount());
                    }

                    // Trigger crafting to refill output slot
                    if (blockEntity.getWorld() != null && !blockEntity.getWorld().isClient) {
                        if (!blockEntity.craftSingle()) {
                            break; // No more materials to craft
                        }
                        blockEntity.updateOutputSlot();
                    }
                }

                return transferredStack.isEmpty() ? ItemStack.EMPTY : transferredStack;
            }

            // Fallback for when blockEntity is null (shouldn't happen on server)
            if (!this.insertItem(originalStack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }

        } else if (index < customInvSize) {
            // Move from block inventory to player inventory
            if (!this.insertItem(originalStack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Move from player inventory to input slots
            if (!this.insertItem(originalStack, 0, INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return stackCopy;
    }

    /**
     * Determines whether the player can use this container.
     * Delegates to the inventory's own usage check.
     *
     * @param player the player
     * @return true if usable, false otherwise
     */
    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
