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

public record SpellInscriberRecipe(DefaultedList<Ingredient> ingredients, ItemStack output) implements Recipe<SpellInscriberRecipeInput> {

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

    @Override
    public ItemStack craft(SpellInscriberRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SPELL_INSCRIBER_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SPELL_INSCRIBER_RECIPE_TYPE;
    }

    public static class Serializer implements RecipeSerializer<SpellInscriberRecipe> {

        public static final MapCodec<SpellInscriberRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients")
                        .forGetter(recipe -> recipe.ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(SpellInscriberRecipe::output)
        ).apply(instance, (ingredients, result) -> {
            DefaultedList<Ingredient> list = DefaultedList.ofSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                list.set(i, ingredients.get(i));
            }
            return new SpellInscriberRecipe(list, result);
        }));

        public static final PacketCodec<RegistryByteBuf, SpellInscriberRecipe> STREAM_CODEC =
                PacketCodec.tuple(
                        PacketCodecs.collection(ArrayList::new, Ingredient.PACKET_CODEC),
                        Recipe::getIngredients,

                        ItemStack.PACKET_CODEC,
                        SpellInscriberRecipe::output,

                        (ingredients, result) -> {
                            DefaultedList<Ingredient> ingredientsList = DefaultedList.copyOf(Ingredient.EMPTY, ingredients.toArray(new Ingredient[0]));
                            return new SpellInscriberRecipe(ingredientsList, result);
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
