package net.IneiTsuki.regen.block.entity;

import net.IneiTsuki.regen.recipe.ModRecipes;
import net.IneiTsuki.regen.recipe.SpellInscriberRecipe;
import net.IneiTsuki.regen.recipe.SpellInscriberRecipeInput;
import net.IneiTsuki.regen.screen.SpellInscriber.SpellInscriberScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class SpellInscriberBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(11, ItemStack.EMPTY);

    private static final int INPUT_SLOTS = 8;
    private static final int OUTPUT_SLOT = 8;

    public SpellInscriberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_INSCRIBER, pos, state);
    }

    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Spell Inscriber");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SpellInscriberScreenHandler(syncId, playerInventory, this);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (hasRecipe()) {
            craftItem();
        }
    }


    private void craftItem() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return;

        // Remove 1 item from each non-empty input slot (basic placeholder logic)
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (!inventory.get(i).isEmpty()) {
                removeStack(i, 1);
            }
        }

        // Output the crafted result
        ItemStack output = recipe.get().value().output();
        this.setStack(OUTPUT_SLOT, new ItemStack(output.getItem(),
                this.getStack(OUTPUT_SLOT).getCount() + output.getCount()));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
    }

    private boolean hasRecipe() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) {
            return false;
        }

        ItemStack output = recipe.get().value().output();
        return canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output);
    }

    private Optional<RecipeEntry<SpellInscriberRecipe>> getCurrentRecipe() {
        DefaultedList<ItemStack> inputs = DefaultedList.ofSize(INPUT_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            inputs.set(i, inventory.get(i));
        }

        return Objects.requireNonNull(this.getWorld()).getRecipeManager()
                .getFirstMatch(ModRecipes.SPELL_INSCRIBER_RECIPE_TYPE, new SpellInscriberRecipeInput(inputs), this.getWorld());
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return this.getStack(OUTPUT_SLOT).isEmpty() || this.getStack(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = this.getStack(OUTPUT_SLOT).isEmpty() ? 64 : this.getStack(OUTPUT_SLOT).getMaxCount();
        int currentCount = this.getStack(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

}
