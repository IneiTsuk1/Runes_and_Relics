package net.IneiTsuki.regen.recipe;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.recipe.impl.SpellInscriberRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers custom recipe serializers and recipe types used in the mod.
 *
 * <p>This class handles registration of the {@link SpellInscriberRecipe} serializer
 * and recipe type, so Minecraft knows how to load and recognize custom recipes.
 */
public class ModRecipes {

    /**
     * The serializer for the Spell Inscriber recipes.
     *
     * <p>Handles reading and writing the recipe JSON data for Spell Inscriber.
     */
    public static final RecipeSerializer<SpellInscriberRecipe> SPELL_INSCRIBER_RECIPE_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER,
            Identifier.of(Regen.MOD_ID, "spell_inscriber"),
            new SpellInscriberRecipe.Serializer());

    /**
     * The custom recipe type for Spell Inscriber recipes.
     *
     * <p>Used to identify this recipe type in the recipe manager.
     */
    public static final RecipeType<SpellInscriberRecipe> SPELL_INSCRIBER_RECIPE_TYPE = Registry.register(
            Registries.RECIPE_TYPE,
            Identifier.of(Regen.MOD_ID, "spell_inscriber"),
            new RecipeType<SpellInscriberRecipe>() {
                @Override
                public String toString() {
                    return "spell_inscriber";
                }
            });

    /**
     * Registers the mod’s custom recipes.
     *
     * <p>Currently logs registration, but can be expanded if needed.
     */
    public static void registerRecipes() {
        Regen.LOGGER.info("Registering Custom Recipes for " + Regen.MOD_ID);
    }
}
