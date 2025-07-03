package net.IneiTsuki.regen.magic.core.utils;

import net.IneiTsuki.regen.magic.api.MagicEnums;
import net.IneiTsuki.regen.magic.core.constants.MagicConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for calculating interaction rules between clarifications and magic types.
 *
 * This class handles the complex logic of how different magical components interact,
 * including conflicts, amplifications, and modifications to spell parameters.
 */
public final class MagicInteractionRules {

    private MagicInteractionRules() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates the effective clarifications after resolving conflicts.
     * Higher priority clarifications override lower priority ones in conflicts.
     *
     * @param clarifications The input clarifications (must not be null)
     * @return Immutable list of effective clarifications after conflict resolution
     */
    public static List<MagicEnums.Clarification> resolveClairificationConflicts(
            List<MagicEnums.Clarification> clarifications) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");

        if (clarifications.isEmpty()) {
            return List.of();
        }

        List<MagicEnums.Clarification> resolved = new ArrayList<>(clarifications);

        // Handle intensity conflicts (MUCH vs LITTLE vs SOME)
        resolveIntensityConflicts(resolved);

        // Handle control vs destruction conflicts
        resolveControlDestructionConflicts(resolved);

        return List.copyOf(resolved);
    }

    /**
     * Calculates the intensity modifier based on clarifications.
     *
     * @param clarifications The clarifications to analyze
     * @return The intensity multiplier (1.0 = normal, >1.0 = amplified, <1.0 = reduced)
     */
    public static double calculateIntensityModifier(List<MagicEnums.Clarification> clarifications) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");

        double modifier = MagicConstants.SOME_INTENSITY_MULTIPLIER; // Default

        if (clarifications.contains(MagicEnums.Clarification.MUCH)) {
            modifier = MagicConstants.MUCH_INTENSITY_MULTIPLIER;
        } else if (clarifications.contains(MagicEnums.Clarification.LITTLE)) {
            modifier = MagicConstants.LITTLE_INTENSITY_MULTIPLIER;
        }

        return modifier;
    }

    /**
     * Calculates the radius modifier based on clarifications.
     *
     * @param clarifications The clarifications to analyze
     * @param baseRadius The base radius before modifications
     * @return The modified radius
     */
    public static int calculateRadiusModifier(List<MagicEnums.Clarification> clarifications,
                                              int baseRadius) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");

        int radius = baseRadius;

        if (clarifications.contains(MagicEnums.Clarification.AREA)) {
            radius += MagicConstants.FIRE_AREA_RADIUS_BONUS;
        }

        if (clarifications.contains(MagicEnums.Clarification.MANY)) {
            radius += MagicConstants.FIRE_MANY_RADIUS_BONUS;
        }

        // Apply intensity modifiers
        double intensityModifier = calculateIntensityModifier(clarifications);
        if (intensityModifier != 1.0) {
            radius = Math.max(1, (int) (radius * intensityModifier));
        }

        return radius;
    }

    /**
     * Calculates the overall effect multiplier based on magic type interactions.
     *
     * @param types The magic types to analyze
     * @return The effect multiplier based on type interactions
     */
    public static double calculateTypeInteractionMultiplier(List<MagicEnums.MagicType> types) {
        Objects.requireNonNull(types, "Magic types cannot be null");

        if (types.size() <= 1) {
            return MagicConstants.NEUTRAL_MULTIPLIER;
        }

        double multiplier = 1.0;

        // Check all pairs of magic types
        for (int i = 0; i < types.size(); i++) {
            for (int j = i + 1; j < types.size(); j++) {
                MagicEnums.MagicType type1 = types.get(i);
                MagicEnums.MagicType type2 = types.get(j);

                MagicEnums.InteractionType interaction = type1.getInteractionWith(type2);

                switch (interaction) {
                    case AMPLIFY -> multiplier *= MagicConstants.AMPLIFY_MULTIPLIER;
                    case OPPOSE -> multiplier *= MagicConstants.OPPOSE_MULTIPLIER;
                    case NEUTRAL -> multiplier *= MagicConstants.NEUTRAL_MULTIPLIER;
                }
            }
        }

        return multiplier;
    }

    /**
     * Determines if a spell combination is stable and can be cast.
     * Highly conflicting combinations may be unstable.
     *
     * @param clarifications The clarifications in the spell
     * @param types The magic types in the spell
     * @return true if the combination is stable, false if it's too conflicted
     */
    public static boolean isStableCombination(List<MagicEnums.Clarification> clarifications,
                                              List<MagicEnums.MagicType> types) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");

        // Count conflicts in clarifications
        long clarificationConflicts = countClarificationConflicts(clarifications);

        // Count opposing magic types
        long typeConflicts = countTypeConflicts(types);

        // Combination is unstable if there are too many conflicts
        return clarificationConflicts <= 1 && typeConflicts <= 1;
    }

    /**
     * Gets a description of what modifications are applied to a spell.
     *
     * @param clarifications The clarifications in the spell
     * @param types The magic types in the spell
     * @return A human-readable description of the modifications
     */
    public static String getModificationDescription(List<MagicEnums.Clarification> clarifications,
                                                    List<MagicEnums.MagicType> types) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");

        List<String> modifications = new ArrayList<>();

        double intensityMod = calculateIntensityModifier(clarifications);
        if (intensityMod > 1.0) {
            modifications.add("amplified");
        } else if (intensityMod < 1.0) {
            modifications.add("weakened");
        }

        if (clarifications.contains(MagicEnums.Clarification.AREA)) {
            modifications.add("area effect");
        }

        if (clarifications.contains(MagicEnums.Clarification.MANY)) {
            modifications.add("multi-target");
        }

        if (clarifications.contains(MagicEnums.Clarification.CONTROL)) {
            modifications.add("precise");
        }

        if (clarifications.contains(MagicEnums.Clarification.MOVE)) {
            modifications.add("mobile");
        }

        double typeMod = calculateTypeInteractionMultiplier(types);
        if (typeMod > 1.0) {
            modifications.add("synergistic");
        } else if (typeMod < 1.0) {
            modifications.add("conflicted");
        }

        return modifications.isEmpty() ? "standard" : String.join(", ", modifications);
    }

    // Private helper methods

    private static void resolveIntensityConflicts(List<MagicEnums.Clarification> clarifications) {
        Set<MagicEnums.Clarification> intensityMods = clarifications.stream()
                .filter(c -> c == MagicEnums.Clarification.MUCH ||
                        c == MagicEnums.Clarification.LITTLE ||
                        c == MagicEnums.Clarification.SOME)
                .collect(Collectors.toSet());

        if (intensityMods.size() > 1) {
            // Remove all intensity modifiers
            clarifications.removeAll(intensityMods);

            // Add back the highest priority one
            if (intensityMods.contains(MagicEnums.Clarification.MUCH)) {
                clarifications.add(MagicEnums.Clarification.MUCH);
            } else if (intensityMods.contains(MagicEnums.Clarification.SOME)) {
                clarifications.add(MagicEnums.Clarification.SOME);
            } else {
                clarifications.add(MagicEnums.Clarification.LITTLE);
            }
        }
    }

    private static void resolveControlDestructionConflicts(List<MagicEnums.Clarification> clarifications) {
        boolean hasControl = clarifications.contains(MagicEnums.Clarification.CONTROL);
        boolean hasDestruction = clarifications.contains(MagicEnums.Clarification.DESTRUCTION);

        if (hasControl && hasDestruction) {
            // CONTROL has higher priority, remove DESTRUCTION
            clarifications.remove(MagicEnums.Clarification.DESTRUCTION);
        }
    }

    private static long countClarificationConflicts(List<MagicEnums.Clarification> clarifications) {
        long conflicts = 0;

        for (int i = 0; i < clarifications.size(); i++) {
            for (int j = i + 1; j < clarifications.size(); j++) {
                if (clarifications.get(i).conflictsWith(clarifications.get(j))) {
                    conflicts++;
                }
            }
        }

        return conflicts;
    }

    private static long countTypeConflicts(List<MagicEnums.MagicType> types) {
        long conflicts = 0;

        for (int i = 0; i < types.size(); i++) {
            for (int j = i + 1; j < types.size(); j++) {
                if (types.get(i).getInteractionWith(types.get(j)) == MagicEnums.InteractionType.OPPOSE) {
                    conflicts++;
                }
            }
        }

        return conflicts;
    }
}