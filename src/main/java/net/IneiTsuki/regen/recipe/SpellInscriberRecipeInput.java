package net.IneiTsuki.regen.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.collection.DefaultedList;

/**
 * Represents the input for a Spell Inscriber recipe,
 * holding a list of item stacks.
 */
public class SpellInscriberRecipeInput implements RecipeInput {
    private final DefaultedList<ItemStack> inputs;

    public SpellInscriberRecipeInput(DefaultedList<ItemStack> inputs) {
        this.inputs = inputs;
    }

    /**
     * Returns the item stack in the specified slot,
     * or ItemStack.EMPTY if the slot is out of bounds.
     */
    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot < inputs.size() ? inputs.get(slot) : ItemStack.EMPTY;
    }

    /**
     * Returns the total number of input slots.
     */
    @Override
    public int getSize() {
        return inputs.size();
    }
}
