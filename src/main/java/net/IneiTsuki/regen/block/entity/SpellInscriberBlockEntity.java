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

public class SpellInscriberBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private static final int INPUT_SLOTS = 8;
    private static final int OUTPUT_SLOT = 8;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(11, ItemStack.EMPTY);

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

    public void updateOutputSlot() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipe = getMatchingRecipe();

        if (recipe.isEmpty()) {
            // No matching recipe, clear output
            if (!inventory.get(OUTPUT_SLOT).isEmpty()) {
                inventory.set(OUTPUT_SLOT, ItemStack.EMPTY);
                markDirty();
            }
            return;
        }

        ItemStack output = recipe.get().value().output();

        // Check if output slot can accept this output
        if (canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output)) {
            inventory.set(OUTPUT_SLOT, output.copy());
            markDirty();
        } else {
            // Can't fit output, clear output slot
            if (!inventory.get(OUTPUT_SLOT).isEmpty()) {
                inventory.set(OUTPUT_SLOT, ItemStack.EMPTY);
                markDirty();
            }
        }
    }

    public void craftItem() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipeOpt = getMatchingRecipe();
        if (recipeOpt.isEmpty()) return;

        // Consume one of each input
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    inventory.set(i, ItemStack.EMPTY);
                }
            }
        }

        // Place crafted result into output slot
        ItemStack result = recipeOpt.get().value().output().copy();
        setStack(OUTPUT_SLOT, result);
        markDirty();
    }

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

    private boolean hasValidRecipe() {
        Optional<RecipeEntry<SpellInscriberRecipe>> recipeOpt = getMatchingRecipe();
        if (recipeOpt.isEmpty()) return false;

        ItemStack output = recipeOpt.get().value().output();
        return canInsertItemIntoOutputSlot(output) && canInsertAmountIntoOutputSlot(output.getCount());
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        ItemStack current = getStack(OUTPUT_SLOT);
        return current.isEmpty() || current.getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int amount) {
        ItemStack current = getStack(OUTPUT_SLOT);
        int max = current.isEmpty() ? 64 : current.getMaxCount();
        return current.getCount() + amount <= max;
    }

    // --- NBT Serialization ---

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

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    // Optional tick method
    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) return;

        updateOutputSlot();
        // Don't consume inputs here!
    }



}
