package net.IneiTsuki.regen.magic.api;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Contains the core enums for the magic system.
 *
 * The magic system works on a combination principle:
 * - Clarifications modify HOW magic is applied (scope, intensity, targeting)
 * - MagicTypes define WHAT kind of magic is used (elemental, conceptual)
 *
 * Spells are created by combining one or more clarifications with one or more magic types.
 * The interaction between these, creates emergent spell behaviors.
 */
@SuppressWarnings("unused")
public class MagicEnums {

    /**
     * Clarifications modify the scope, intensity, and targeting of magic spells.
     *
     * Interaction Rules:
     * - CONTROL takes precedence over chaotic effects (DESTRUCTION, AREA)
     * - MUCH/LITTLE are intensity modifiers that can conflict (MUCH wins)
     * - AREA/MANY affect scope differently (AREA = bigger effect, MANY = more targets)
     * - MOVE adds directional or motion-based components to spells
     */
    public enum Clarification {
        /** Precise, controlled magic with minimal side effects */
        CONTROL("control", 0x4A90E2, Priority.HIGH),

        /** Destructive, chaotic magic that damages or destroys */
        DESTRUCTION("destruction", 0xE24A4A, Priority.MEDIUM),

        /** Creative magic that builds, heals, or constructs */
        CONSTRUCTION("construction", 0x4AE24A, Priority.MEDIUM),

        /** High-intensity modifier that amplifies effects */
        MUCH("much", 0xE2A04A, Priority.LOW),

        /** Low-intensity modifier that reduces effects */
        LITTLE("little", 0xA04AE2, Priority.LOW),

        /** Moderate intensity, balanced effects */
        SOME("some", 0xE2E24A, Priority.LOW),

        /** Affects multiple targets or creates multiple effects */
        MANY("many", 0x4AE2A0, Priority.MEDIUM),

        /** Expands the area of effect */
        AREA("area", 0xE24AA0, Priority.MEDIUM),

        /** Adds movement, projectile, or directional components */
        MOVE("move", 0x808080, Priority.LOW);

        private final String name;
        private final int color;
        private final Priority priority;

        Clarification(String name, int color, Priority priority) {
            this.name = name;
            this.color = color;
            this.priority = priority;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public Priority getPriority() {
            return priority;
        }

        public String getFormattedName() {
            return Arrays.stream(name.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));
        }

        /**
         * Determines if this clarification conflicts with another.
         * Conflicting clarifications will have their effects modified according to priority rules.
         */
        public boolean conflictsWith(Clarification other) {
            // Intensity conflicts
            if ((this == MUCH || this == LITTLE || this == SOME) &&
                    (other == MUCH || other == LITTLE || other == SOME)) {
                return this != other;
            }

            // Control vs Destruction conflict
            if ((this == CONTROL && other == DESTRUCTION) ||
                    (this == DESTRUCTION && other == CONTROL)) {
                return true;
            }

            return false;
        }
    }

    /**
     * MagicTypes define the fundamental nature and element of magic being used.
     *
     * Interaction Rules:
     * - Opposing elements reduce each other's effects (FIRE/WATER, FIRE/ICE, LIFE/DARK)
     * - Similar elements can amplify each other (FIRE/LIGHT for radiance)
     * - Neutral elements (TARGET, AIR, EARTH) rarely conflict
     */
    public enum MagicType {
        /** Targeting and detection magic */
        TARGET("target", 0x800080, ElementGroup.NEUTRAL),

        /** Light, illumination, and radiant magic */
        LIGHT("light", 0xFFFF80, ElementGroup.RADIANT),

        /** Shadow, decay, and negative energy magic */
        DARK("dark", 0x404040, ElementGroup.SHADOW),

        /** Water, liquid, and flow magic */
        WATER("water", 0x4080FF, ElementGroup.ELEMENTAL),

        /** Life, healing, and growth magic */
        LIFE("life", 0x80FF80, ElementGroup.NATURAL),

        /** Fire, heat, and combustion magic */
        FIRE("fire", 0xFF4040, ElementGroup.ELEMENTAL),

        /** Air, wind, and atmospheric magic */
        AIR("air", 0xC0C0C0, ElementGroup.ELEMENTAL),

        /** Ice, cold, and freezing magic */
        ICE("ice", 0x80FFFF, ElementGroup.ELEMENTAL),

        /** Earth, stone, and mineral magic */
        EARTH("earth", 0x8B4513, ElementGroup.ELEMENTAL);

        private final String name;
        private final int color;
        private final ElementGroup group;

        MagicType(String name, int color, ElementGroup group) {
            this.name = name;
            this.color = color;
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public ElementGroup getGroup() {
            return group;
        }

        public String getFormattedName() {
            return Arrays.stream(name.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));
        }

        /**
         * Determines the interaction type between this magic type and another.
         */
        public InteractionType getInteractionWith(MagicType other) {
            if (this == other) return InteractionType.AMPLIFY;

            // Opposing elements
            if ((this == FIRE && (other == WATER || other == ICE)) ||
                    (this == WATER && (other == FIRE)) ||
                    (this == ICE && (other == FIRE)) ||
                    (this == LIGHT && other == DARK) ||
                    (this == DARK && other == LIGHT) ||
                    (this == LIFE && other == DARK) ||
                    (this == DARK && other == LIFE)) {
                return InteractionType.OPPOSE;
            }

            // Complementary elements
            if ((this == FIRE && other == LIGHT) ||
                    (this == LIGHT && other == FIRE) ||
                    (this == WATER && other == ICE) ||
                    (this == ICE && other == WATER) ||
                    (this == EARTH && other == LIFE) ||
                    (this == LIFE && other == EARTH)) {
                return InteractionType.AMPLIFY;
            }

            return InteractionType.NEUTRAL;
        }
    }

    /**
     * Priority levels for clarifications when conflicts occur.
     * Higher priority clarifications take precedence in conflicts.
     */
    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3);

        private final int level;

        Priority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Element groups for categorizing magic types.
     */
    public enum ElementGroup {
        ELEMENTAL,  // Fire, Water, Air, Earth, Ice
        RADIANT,    // Light-based magic
        SHADOW,     // Dark/negative energy
        NATURAL,    // Life, growth
        NEUTRAL     // Target, utility magic
    }

    /**
     * Types of interactions between magic types.
     */
    public enum InteractionType {
        /** Elements amplify each other's effects */
        AMPLIFY,
        /** Elements oppose and reduce each other's effects */
        OPPOSE,
        /** Elements have no special interaction */
        NEUTRAL
    }
}