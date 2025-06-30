package net.IneiTsuki.regen.magic;

/**
 * Constants for the magic system to avoid magic numbers and provide
 * centralized configuration.
 */
public final class MagicConstants {

    // Scroll Configuration
    public static final int SCROLL_MAX_STACK_SIZE = 16;
    public static final String SCROLL_NAME_PREFIX = "scroll_";
    public static final String SCROLL_NAME_SEPARATOR = "_";

    // Fire Spell Constants
    public static final int FIRE_BASE_RADIUS = 1;
    public static final int FIRE_AREA_RADIUS_BONUS = 2;
    public static final int FIRE_MANY_RADIUS_BONUS = 2;
    public static final int FIRE_MUCH_RADIUS_MULTIPLIER = 2;
    public static final int FIRE_LITTLE_RADIUS_DIVISOR = 2;
    public static final int FIRE_MOVE_DURATION_SECONDS = 5;
    public static final int FIRE_DESTRUCTION_DAMAGE_RADIUS = 1;

    // Sound Configuration
    public static final float DEFAULT_SOUND_VOLUME = 1.0F;
    public static final float DEFAULT_SOUND_PITCH = 1.0F;

    // Effect Modifiers
    public static final double MUCH_INTENSITY_MULTIPLIER = 2.0;
    public static final double LITTLE_INTENSITY_MULTIPLIER = 0.5;
    public static final double SOME_INTENSITY_MULTIPLIER = 1.0;

    // Interaction Multipliers
    public static final double AMPLIFY_MULTIPLIER = 1.5;
    public static final double OPPOSE_MULTIPLIER = 0.5;
    public static final double NEUTRAL_MULTIPLIER = 1.0;

    // Error Messages
    public static final String ERROR_SPELL_BACKFIRE = "The spell backfires and fizzles out!";
    public static final String ERROR_INVALID_COMBINATION = "This magical combination is unstable!";
    public static final String ERROR_INSUFFICIENT_POWER = "You lack the power to cast this spell!";

    // Success Messages
    public static final String SUCCESS_FIRE_SPELL = "You unleash a fiery spell!";
    public static final String SUCCESS_SCROLL_CAST = "You cast %s %s magic!";

    private MagicConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}