package net.IneiTsuki.regen.block.entity;

import net.IneiTsuki.regen.client.screen.SpellInscriber.SpellInscriberScreenHandler;
import net.IneiTsuki.regen.recipe.ModRecipes;
import net.IneiTsuki.regen.recipe.impl.SpellInscriberRecipe;
import net.IneiTsuki.regen.recipe.types.SpellInscriberRecipeInput;
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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * BlockEntity for the Spell Inscriber block.
 * Handles inventory, crafting logic, recipe matching, GUI interaction, and syncing with the client.
 */
public class SpellInscriberBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    public static final int INPUT_SLOTS = 8;
    public static final int OUTPUT_SLOT = 8;
    private static final int INVENTORY_SIZE = 11; // 8 inputs + 1 output

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // Cache fields for recipe & input state
    private Optional<RecipeEntry<SpellInscriberRecipe>> cachedRecipe = Optional.empty();
    private int lastInputHash = 0;

    public SpellInscriberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_INSCRIBER, pos, state);
    }

    @Override
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

    // -- Inventory Helpers --

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(inventory, slot, count);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    // -- Recipe caching and input change detection --

    /**
     * Computes a simple hash for the input slots to detect changes.
     */
    private int computeInputHash() {
        return Arrays.hashCode(inventory.subList(0, INPUT_SLOTS).stream()
                .filter(Objects::nonNull)
                .map(Object::hashCode)
                .toArray(Integer[]::new));
    }

    /**
     * Updates the cached recipe if the inputs have changed.
     */
    private void updateCachedRecipe() {
        int currentInputHash = computeInputHash();
        if (currentInputHash != lastInputHash) {
            lastInputHash = currentInputHash;
            cachedRecipe = findMatchingRecipe();
        }
    }

    /**
     * Finds the current matching recipe.
     */
    private Optional<RecipeEntry<SpellInscriberRecipe>> findMatchingRecipe() {
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
     * Updates the output slot based on cached recipe.
     */
    public void updateOutputSlot() {
        updateCachedRecipe();

        if (cachedRecipe.isEmpty()) {
            clearOutput();
            return;
        }

        ItemStack output = cachedRecipe.get().value().output();

        if (canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output)) {
            // Copy to avoid modifying cached recipe output
            inventory.set(OUTPUT_SLOT, output.copy());
            markDirty();
            notifyBlockUpdate();
        } else {
            clearOutput();
        }
    }

    private void clearOutput() {
        if (!inventory.get(OUTPUT_SLOT).isEmpty()) {
            inventory.set(OUTPUT_SLOT, ItemStack.EMPTY);
            markDirty();
            notifyBlockUpdate();
        }
    }

    // -- Crafting logic --

    /**
     * Crafts a single item (consumes inputs once, produces one output).
     */
    public boolean craftSingle() {
        updateCachedRecipe();
        if (cachedRecipe.isEmpty()) return false;

        ItemStack output = cachedRecipe.get().value().output();
        if (!canInsertAmountIntoOutputSlot(output.getCount()) || !canInsertItemIntoOutputSlot(output)) {
            return false;
        }

        // Consume inputs once
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                stack.decrement(1);
                if (stack.isEmpty()) inventory.set(i, ItemStack.EMPTY);
            }
        }

        // Add output
        addToOutput(output.copy());
        markDirty();
        notifyBlockUpdate();
        return true;
    }

    /**
     * Crafts as many times as possible (shift crafting).
     * Returns the number of items crafted.
     */
    public int craftAllPossible() {
        updateCachedRecipe();
        if (cachedRecipe.isEmpty()) return 0;

        int craftedCount = 0;
        while (craftSingle()) {
            craftedCount++;
            updateCachedRecipe();
        }
        return craftedCount;
    }

    /**
     * Adds an ItemStack to the output slot, merging if possible.
     */
    private void addToOutput(ItemStack stackToAdd) {
        ItemStack current = inventory.get(OUTPUT_SLOT);
        if (current.isEmpty()) {
            inventory.set(OUTPUT_SLOT, stackToAdd);
        } else if (current.getItem() == stackToAdd.getItem()) {
            int combined = current.getCount() + stackToAdd.getCount();
            int maxCount = current.getMaxCount();
            if (combined <= maxCount) {
                current.setCount(combined);
            } else {
                current.setCount(maxCount);
                // optionally handle overflow here (e.g., drop excess)
            }
        } else {
            // Different item in output slot, overwrite (or you can decide what to do)
            inventory.set(OUTPUT_SLOT, stackToAdd);
        }
    }

    // -- Output slot insertion checks --

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        ItemStack current = inventory.get(OUTPUT_SLOT);
        return current.isEmpty() || current.getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int amount) {
        ItemStack current = inventory.get(OUTPUT_SLOT);
        int max = current.isEmpty() ? 64 : current.getMaxCount();
        return current.getCount() + amount <= max;
    }

    // -- NBT serialization --

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        // Optional: Save cache info if needed (usually not necessary)
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        // Force cache update on load
        lastInputHash = 0;
        cachedRecipe = Optional.empty();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public void notifyBlockUpdate() {
        if (world != null) {
            BlockState state = getCachedState();
            world.updateListeners(pos, state, state, 3);
        }
    }

    /**
     * Tick method to be called from mod tick registry.
     * Updates output slot periodically on server side.
     */
    public static void tick(World world, BlockPos pos, BlockState state, SpellInscriberBlockEntity blockEntity) {
        if (world.isClient) return;
        blockEntity.updateOutputSlot();
    }
}
