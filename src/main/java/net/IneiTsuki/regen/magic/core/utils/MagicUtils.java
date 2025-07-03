package net.IneiTsuki.regen.magic.core.utils;

import net.IneiTsuki.regen.magic.api.MagicEnums;
import net.IneiTsuki.regen.magic.core.constants.MagicConstants;

import java.util.List;

public class MagicUtils {

    /**
     * Calculates casting delay in ticks based on the clarifications provided.
     */
    public static int computeCastingDelay(List<MagicEnums.Clarification> clarifications) {
        int delay = MagicConstants.BASE_CASTING_DELAY_TICKS;

        for (MagicEnums.Clarification clarification : clarifications) {
            switch (clarification) {
                case MUCH -> delay += MagicConstants.DELAY_MUCH_BONUS;
                case LITTLE -> delay += MagicConstants.DELAY_LITTLE_REDUCTION;
                case SOME -> delay += MagicConstants.DELAY_SOME_MODIFIER;
                default -> {} // other clarifications don't affect casting delay
            }
        }

        return Math.max(
                MagicConstants.MIN_CASTING_DELAY_TICKS,
                Math.min(MagicConstants.MAX_CASTING_DELAY_TICKS, delay)
        );
    }
}