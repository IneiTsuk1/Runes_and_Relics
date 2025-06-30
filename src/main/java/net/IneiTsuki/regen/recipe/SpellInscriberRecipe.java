package net.IneiTsuki.regen.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Represents a custom recipe for the Spell Inscriber block.
 *
 * <p>This recipe defines the ingredients and output item stack.
 * It matches an input inventory against the recipe ingredients,
 * and produces the specified output.
 *
 * @param ingredients list of input ingredients required
 * @param output      resulting item stack produced by the recipe
 */
public record SpellInscriberRecipe(DefaultedList<Ingredient> ingredients, ItemStack output)
        implements Recipe<SpellInscriberRecipeInput> {

    /**
     * Checks if the given input matches this recipe.
     *
     * @param input the input inventory wrapper
     * @param world the world context, used to check client/server side
     * @return true if input matches the ingredients, false otherwise
     */
    @Override
    public boolean matches(SpellInscriberRecipeInput input, World world) {
        if (world.isClient()) return false;

        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(input.getStackInSlot(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Crafts the output item stack from the input.
     *
     * @param input  the input inventory
     * @param lookup registry lookup context
     * @return a copy of the output item stack
     */
    @Override
    public ItemStack craft(SpellInscriberRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return output.copy();
    }

    /**
     * Indicates this recipe does not require a shaped grid.
     *
     * @param width  width of crafting area
     * @param height height of crafting area
     * @return always true as this is shapeless
     */
    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    /**
     * Gets the recipe result.
     *
     * @param lookup registry lookup context
     * @return the output item stack
     */
    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
        return output;
    }

    /**
     * Gets the serializer for this recipe type.
     *
     * @return the serializer instance
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SPELL_INSCRIBER_RECIPE_SERIALIZER;
    }

    /**
     * Gets the recipe type for this recipe.
     *
     * @return the recipe type instance
     */
    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SPELL_INSCRIBER_RECIPE_TYPE;
    }

    /**
     * Serializer class for reading/writing SpellInscriberRecipe from JSON and packets.
     */
    public static class Serializer implements RecipeSerializer<SpellInscriberRecipe> {

        /**
         * Codec for serializing/deserializing the recipe via JSON or data-driven formats.
         */
        public static final MapCodec<SpellInscriberRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients")
                                .forGetter(recipe -> recipe.ingredients),
                        ItemStack.CODEC.fieldOf("result")
                                .forGetter(SpellInscriberRecipe::output)
                ).apply(instance, (ingredients, result) -> {
                    DefaultedList<Ingredient> list = DefaultedList.ofSize(ingredients.size(), Ingredient.EMPTY);
                    for (int i = 0; i < ingredients.size(); i++) {
                        list.set(i, ingredients.get(i));
                    }
                    return new SpellInscriberRecipe(list, result);
                })
        );

        /**
         * Codec for serializing/deserializing the recipe over network packets.
         */
        public static final PacketCodec<RegistryByteBuf, SpellInscriberRecipe> STREAM_CODEC =
                PacketCodec.tuple(
                        PacketCodecs.collection(ArrayList::new, Ingredient.PACKET_CODEC),
                        Recipe::getIngredients,

                        ItemStack.PACKET_CODEC,
                        SpellInscriberRecipe::output,

                        (ingredients, result) -> {
                            DefaultedList<Ingredient> list =
                                    DefaultedList.copyOf(Ingredient.EMPTY, ingredients.toArray(new Ingredient[0]));
                            return new SpellInscriberRecipe(list, result);
                        }
                );

        @Override
        public MapCodec<SpellInscriberRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SpellInscriberRecipe> packetCodec() {
            return STREAM_CODEC;
        }
    }
}
