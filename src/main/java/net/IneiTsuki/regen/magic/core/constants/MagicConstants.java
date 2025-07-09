package net.IneiTsuki.regen.magic.core.constants;

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
    public static final int FIRE_EFFECT_DURATION_TICKS = 200;

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

    // Casting Timing Configuration
    public static final int BASE_CASTING_DELAY_TICKS = 20; // 1-second base delay
    public static final int MIN_CASTING_DELAY_TICKS = 5;   // minimum 0.25s
    public static final int MAX_CASTING_DELAY_TICKS = 100; // max 5 seconds

    public static final int DEFAULT_CAST_DELAY_TICKS = 40; // 2 seconds (20 ticks = 1 sec)


    public static final int DELAY_MUCH_BONUS = 20;   // adds 1s
    public static final int DELAY_LITTLE_REDUCTION = -10; // subtracts 0.5s
    public static final int DELAY_SOME_MODIFIER = 0; // neutral

    public static final int DEFAULT_MANA_COST = 10;



    private MagicConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}