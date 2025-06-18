package net.IneiTsuki.regen.recipe;

import net.IneiTsuki.regen.Regen;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {
    public static final RecipeSerializer<SpellInscriberRecipe> SPELL_INSCRIBER_RECIPE_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER, Identifier.of(Regen.MOD_ID, "spell_inscriber"),
            new SpellInscriberRecipe.Serializer());

    public static final RecipeType<SpellInscriberRecipe> SPELL_INSCRIBER_RECIPE_TYPE = Registry.register(
            Registries.RECIPE_TYPE, Identifier.of(Regen.MOD_ID, "spell_inscriber"), new RecipeType<SpellInscriberRecipe>() {
                @Override
                public String toString() {
                    return "spell_inscriber";
                }});

    public static void registerRecipes() {
        Regen.LOGGER.info("Registering Custom Recipes for " + Regen.MOD_ID);
    }
}
