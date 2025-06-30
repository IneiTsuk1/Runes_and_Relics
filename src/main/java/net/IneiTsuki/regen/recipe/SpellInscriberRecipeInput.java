package net.IneiTsuki.regen.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.collection.DefaultedList;

/**
 * Represents the input inventory for a Spell Inscriber recipe.
 *
 * <p>This class wraps a list of {@link ItemStack}s that correspond
 * to the input slots used when matching and crafting the recipe.
 */
public class SpellInscriberRecipeInput implements RecipeInput {

    /** List of input item stacks for the recipe. */
    private final DefaultedList<ItemStack> inputs;

    /**
     * Creates a new input wrapper around the provided item stacks.
     *
     * @param inputs the list of item stacks representing the input slots
     */
    public SpellInscriberRecipeInput(DefaultedList<ItemStack> inputs) {
        this.inputs = inputs;
    }

    /**
     * Returns the item stack in the specified slot.
     *
     * @param slot the slot index to get the item from
     * @return the {@link ItemStack} in the slot, or {@link ItemStack#EMPTY} if the slot is out of range
     */
    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot < inputs.size() ? inputs.get(slot) : ItemStack.EMPTY;
    }

    /**
     * Returns the number of slots in this input.
     *
     * @return the total number of input slots
     */
    @Override
    public int getSize() {
        return inputs.size();
    }
}
