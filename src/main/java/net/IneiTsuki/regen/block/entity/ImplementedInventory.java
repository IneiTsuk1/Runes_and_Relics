package net.IneiTsuki.regen.block.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * A simple {@link SidedInventory} implementation with default logic and utility factory methods.
 *
 * <p>This interface can be used for creating inventories inside {@link net.minecraft.block.entity.BlockEntity}s
 * or other containers without having to implement all inventory logic manually.
 *
 * <p>Backed by a {@link DefaultedList} of {@link ItemStack}s and supporting sided access.
 *
 * <p>Use {@link Inventories#writeNbt(NbtCompound, DefaultedList, RegistryWrapper.WrapperLookup)}
 * and {@link Inventories#readNbt(NbtCompound, DefaultedList, RegistryWrapper.WrapperLookup)}
 * to serialize/deserialize inventory contents.
 *
 * <p>License: <a href="https://creativecommons.org/publicdomain/zero/1.0/">CC0</a>
 */
public interface ImplementedInventory extends SidedInventory {

    /**
     * Returns the backing item list.
     * This method must always return the same instance.
     *
     * @return The inventory item list.
     */
    DefaultedList<ItemStack> getItems();

    /**
     * Creates an {@link ImplementedInventory} backed by an existing item list.
     *
     * @param items The item list backing the inventory.
     * @return A new {@link ImplementedInventory}.
     */
    static ImplementedInventory of(DefaultedList<ItemStack> items) {
        return () -> items;
    }

    /**
     * Creates a new {@link ImplementedInventory} of the given size filled with {@link ItemStack#EMPTY}.
     *
     * @param size The number of inventory slots.
     * @return A new {@link ImplementedInventory} of the given size.
     */
    static ImplementedInventory ofSize(int size) {
        return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    // --------------------------
    // SidedInventory Defaults
    // --------------------------

    /**
     * Returns an array of all available slot indices for a given side.
     * By default, this includes all slots.
     *
     * @param side The direction of interaction.
     * @return All slot indices.
     */
    @Override
    default int[] getAvailableSlots(Direction side) {
        int[] slots = new int[getItems().size()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    /**
     * Determines whether a stack can be inserted into the given slot from the specified direction.
     * By default, always returns true.
     *
     * @param slot  The target slot.
     * @param stack The item stack to insert.
     * @param side  The direction of insertion.
     * @return {@code true} if the item can be inserted.
     */
    @Override
    default boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    /**
     * Determines whether a stack can be extracted from the given slot from the specified direction.
     * By default, always returns true.
     *
     * @param slot  The slot to extract from.
     * @param stack The item stack in the slot.
     * @param side  The direction of extraction.
     * @return {@code true} if the item can be extracted.
     */
    @Override
    default boolean canExtract(int slot, ItemStack stack, Direction side) {
        return true;
    }

    // --------------------------
    // Inventory Defaults
    // --------------------------

    /**
     * Returns the number of slots in this inventory.
     *
     * @return Inventory size.
     */
    @Override
    default int size() {
        return getItems().size();
    }

    /**
     * Returns {@code true} if all slots are empty.
     *
     * @return Whether the inventory is empty.
     */
    @Override
    default boolean isEmpty() {
        for (ItemStack stack : getItems()) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    /**
     * Gets the stack in the given slot.
     *
     * @param slot The slot index.
     * @return The item stack in the slot.
     */
    @Override
    default ItemStack getStack(int slot) {
        return getItems().get(slot);
    }

    /**
     * Removes up to a specified number of items from a slot.
     *
     * @param slot  The slot index.
     * @param count The number of items to remove.
     * @return The removed item stack.
     */
    @Override
    default ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    /**
     * Removes the entire stack from a slot.
     *
     * @param slot The slot index.
     * @return The removed item stack.
     */
    @Override
    default ItemStack removeStack(int slot) {
        return Inventories.removeStack(getItems(), slot);
    }

    /**
     * Sets the item stack in a slot.
     * Clamps the stack size to the max stack size if necessary.
     *
     * @param slot  The slot index.
     * @param stack The stack to set.
     */
    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }

    /**
     * Clears all item stacks from the inventory.
     */
    @Override
    default void clear() {
        getItems().clear();
    }

    /**
     * Called when the inventory is modified.
     * Override in your block entity to persist changes.
     */
    @Override
    default void markDirty() {
        // Optional: override to mark block entity dirty
    }

    /**
     * Determines whether the given player can use this inventory.
     * By default, always returns true.
     *
     * @param player The player attempting to use the inventory.
     * @return {@code true} if the player can use the inventory.
     */
    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }
}
