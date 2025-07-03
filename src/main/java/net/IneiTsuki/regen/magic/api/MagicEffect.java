package net.IneiTsuki.regen.magic.api;

import net.IneiTsuki.regen.magic.core.MagicConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

/**
 * Functional interface for magic effects in the magic system.
 *
 * Magic effects receive immutable lists of clarifications and magic types,
 * along with the world and user context, to produce magical outcomes.
 *
 * Effects should handle their own error conditions gracefully and provide
 * appropriate feedback to the user.
 *
 * @since 1.0
 */
@FunctionalInterface
public interface MagicEffect {

    /**
     * Applies the magical effect in the given world context.
     *
     * @param world The world where the magic is being applied (must not be null)
     * @param user The player casting the spell (must not be null)
     * @param clarifications Immutable list of clarifications modifying the spell (must not be null or empty)
     * @param types Immutable list of magic types defining the spell (must not be null or empty)
     * @return true if the effect was successfully applied, false otherwise
     * @throws IllegalArgumentException if any parameter is null or lists are empty
     */
    boolean apply(World world, PlayerEntity user,
                  List<MagicEnums.Clarification> clarifications,
                  List<MagicEnums.MagicType> types);

    /**
     * Validates that the effect can be applied with the given parameters.
     * Default implementation performs basic null and empty checks.
     *
     * @param world The world context
     * @param user The player casting the spell
     * @param clarifications The clarifications for the spell
     * @param types The magic types for the spell
     * @return true if the effect can be applied, false otherwise
     */
    default boolean canApply(World world, PlayerEntity user,
                             List<MagicEnums.Clarification> clarifications,
                             List<MagicEnums.MagicType> types) {
        return world != null &&
                user != null &&
                clarifications != null &&
                !clarifications.isEmpty() &&
                types != null &&
                !types.isEmpty();
    }

    /**
     * Validates input parameters for magic effects.
     *
     * @param world The world context
     * @param user The player casting the spell
     * @param clarifications The clarifications for the spell
     * @param types The magic types for the spell
     * @throws IllegalArgumentException if any parameter is invalid
     */
    static void validateParameters(World world, PlayerEntity user,
                                   List<MagicEnums.Clarification> clarifications,
                                   List<MagicEnums.MagicType> types) {
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");

        if (clarifications.isEmpty()) {
            throw new IllegalArgumentException("Clarifications list cannot be empty");
        }

        if (types.isEmpty()) {
            throw new IllegalArgumentException("Magic types list cannot be empty");
        }
    }

    default int getCastDelayTicks() {
        return MagicConstants.DEFAULT_CAST_DELAY_TICKS;
    }

}