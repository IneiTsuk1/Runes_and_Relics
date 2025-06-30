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
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * BlockEntity for the Spell Inscriber block.
 * Handles inventory, crafting logic, recipe matching, GUI interaction, and syncing with the client.
 */
public class SpellInscriberBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private static final int INPUT_SLOTS = 8;
    private static final int OUTPUT_SLOT = 8;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(11, ItemStack.EMPTY);

    /**
     * Constructs the SpellInscriber block entity.
     *
     * @param pos   The position of the block.
     * @param state The block state.
     */
    public SpellInscriberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_INSCRIBER, pos, state);
    }

    /**
     * Returns the backing item list used for inventory operations.
     */
    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    /**
     * Returns the display name shown on the block's GUI.
     */
    @Override
    public Text getDisplayName() {
        return Text.literal("Spell Inscriber");
    }

    /**
     * Creates the screen handler for the Spell Inscriber GUI.
     */
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SpellInscriberScreenHandler(syncId, playerInventory, this);
    }

    /**
     * Updates the output slot based on the current input configuration.
     * If a matching recipe is found and the output can fit, places the output stack.
     * Otherwise, clears the output.
     */
    public void updateOutputSlot() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipe = getMatchingRecipe();

        if (recipe.isEmpty()) {
            if (!inventory.get(OUTPUT_SLOT).isEmpty()) {
                inventory.set(OUTPUT_SLOT, ItemStack.EMPTY);
                markDirty();
            }
            return;
        }

        ItemStack output = recipe.get().value().output();

        if (canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output)) {
            inventory.set(OUTPUT_SLOT, output.copy());
            markDirty();
        } else if (!inventory.get(OUTPUT_SLOT).isEmpty()) {
            inventory.set(OUTPUT_SLOT, ItemStack.EMPTY);
            markDirty();
        }
    }

    /**
     * Crafts the current item and consumes the input items.
     * Assumes a valid recipe is already present.
     */
    public void craftItem() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipeOpt = getMatchingRecipe();
        if (recipeOpt.isEmpty()) return;

        // Consume one item from each input slot
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    inventory.set(i, ItemStack.EMPTY);
                }
            }
        }

        // Output the crafted item
        ItemStack result = recipeOpt.get().value().output().copy();
        setStack(OUTPUT_SLOT, result);
        markDirty();
    }

    /**
     * Checks for a matching recipe based on the current input configuration.
     *
     * @return An optional recipe match, or empty if no match is found.
     */
    private Optional<RecipeEntry<SpellInscriberRecipe>> getMatchingRecipe() {
        DefaultedList<ItemStack> inputs = DefaultedList.ofSize(INPUT_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            inputs.set(i, inventory.get(i));
        }

        World world = getWorld();
        if (world == null) return Optional.empty();

        return world.getRecipeManager().getFirstMatch(
                ModRecipes.SPELL_INSCRIBER_RECIPE_TYPE,
                new SpellInscriberRecipeInput(inputs),
                world
        );
    }

    /**
     * Determines whether the current inventory matches a valid recipe that can fit in the output.
     */
    private boolean hasValidRecipe() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipeOpt = getMatchingRecipe();
        if (recipeOpt.isEmpty()) return false;

        ItemStack output = recipeOpt.get().value().output();
        return canInsertItemIntoOutputSlot(output) && canInsertAmountIntoOutputSlot(output.getCount());
    }

    /**
     * Checks if the output slot can accept an item of the given type.
     */
    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        ItemStack current = getStack(OUTPUT_SLOT);
        return current.isEmpty() || current.getItem() == output.getItem();
    }

    /**
     * Checks if the output slot can hold the specified amount.
     */
    private boolean canInsertAmountIntoOutputSlot(int amount) {
        ItemStack current = getStack(OUTPUT_SLOT);
        int max = current.isEmpty() ? 64 : current.getMaxCount();
        return current.getCount() + amount <= max;
    }

    // --- NBT Serialization ---

    /**
     * Writes this block entity's data to NBT.
     */
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
    }

    /**
     * Reads this block entity's data from NBT.
     */
    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
    }

    /**
     * Returns the NBT data used for chunk syncing.
     */
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    /**
     * Returns a packet that synchronizes this block entity with the client.
     */
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /**
     * Called every tick to update the block entity.
     * Only runs on the server side.
     */
    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) return;

        updateOutputSlot();
        // Do not consume items here; crafting is handled elsewhere.
    }
}
