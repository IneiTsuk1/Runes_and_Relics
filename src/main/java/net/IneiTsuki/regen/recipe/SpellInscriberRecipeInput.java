package net.IneiTsuki.regen.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.collection.DefaultedList;

public class SpellInscriberRecipeInput implements RecipeInput {
    private final DefaultedList<ItemStack> inputs;

    public SpellInscriberRecipeInput(DefaultedList<ItemStack> inputs) {
        this.inputs = inputs;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot < inputs.size() ? inputs.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public int getSize() {
        return inputs.size();
    }
}