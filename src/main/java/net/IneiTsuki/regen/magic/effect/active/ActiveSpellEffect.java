package net.IneiTsuki.regen.magic.effect.active;

import net.IneiTsuki.regen.magic.api.MagicEffect;
import net.IneiTsuki.regen.magic.api.MagicEnums;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class ActiveSpellEffect {
    public final PlayerEntity caster;
    public final MagicEffect effect;
    public final List<MagicEnums.Clarification> clarifications;
    public final List<MagicEnums.MagicType> types;
    public int ticksRemaining;
    private final int totalDuration;
    private boolean hasStarted = false;

    public ActiveSpellEffect(PlayerEntity caster, MagicEffect effect,
                             List<MagicEnums.Clarification> clarifications,
                             List<MagicEnums.MagicType> types, int durationTicks) {
        this.caster = Objects.requireNonNull(caster, "Caster cannot be null");
        this.effect = Objects.requireNonNull(effect, "Effect cannot be null");
        this.clarifications = List.copyOf(Objects.requireNonNull(clarifications, "Clarifications cannot be null"));
        this.types = List.copyOf(Objects.requireNonNull(types, "Types cannot be null"));
        this.ticksRemaining = Math.max(0, durationTicks);
        this.totalDuration = this.ticksRemaining;
    }

    public void tick(World world) {
        if (ticksRemaining <= 0) {
            return; // Already expired
        }

        // Mark as started on first tick
        if (!hasStarted) {
            hasStarted = true;
        }

        // Call the effect's tick method
        effect.onTick(world, caster, clarifications, types, ticksRemaining);
        ticksRemaining--;

        // If this is the last tick, call onEnd
        if (ticksRemaining <= 0) {
            effect.onEnd(world, caster, clarifications, types);
        }
    }

    public boolean isExpired() {
        return ticksRemaining <= 0;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public float getProgress() {
        if (totalDuration <= 0) return 1.0f;
        return 1.0f - (float) ticksRemaining / totalDuration;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public int getTicksElapsed() {
        return totalDuration - ticksRemaining;
    }

    /**
     * Checks if this spell effect has a specific clarification
     */
    public boolean hasClarification(MagicEnums.Clarification clarification) {
        return clarifications.contains(clarification);
    }

    /**
     * Checks if this spell effect has a specific magic type
     */
    public boolean hasMagicType(MagicEnums.MagicType type) {
        return types.contains(type);
    }

    /**
     * Forcibly expires this spell effect, calling onEnd if it hasn't been called yet
     */
    public void forceExpire(World world) {
        if (ticksRemaining > 0) {
            ticksRemaining = 0;
            effect.onEnd(world, caster, clarifications, types);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ActiveSpellEffect other = (ActiveSpellEffect) obj;
        return Objects.equals(caster, other.caster) &&
                Objects.equals(effect, other.effect) &&
                Objects.equals(clarifications, other.clarifications) &&
                Objects.equals(types, other.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caster, effect, clarifications, types);
    }

    @Override
    public String toString() {
        return String.format("ActiveSpellEffect{effect=%s, caster=%s, remaining=%d/%d ticks}",
                effect.getClass().getSimpleName(),
                caster.getName().getString(),
                ticksRemaining,
                totalDuration);
    }
}