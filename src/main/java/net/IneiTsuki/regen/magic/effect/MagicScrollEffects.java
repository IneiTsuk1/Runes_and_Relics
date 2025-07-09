package net.IneiTsuki.regen.magic.effect;

import net.IneiTsuki.regen.magic.api.MagicEnums;
import net.IneiTsuki.regen.magic.api.MagicEffect;
import net.IneiTsuki.regen.magic.core.constants.MagicConstants;
import net.IneiTsuki.regen.magic.core.utils.MagicInteractionRules;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

/**
 * Collection of magic spell effects for the magic system.
 * <p>
 * This class contains implementations of various magical effects that can be
 * applied by magic scrolls. Each effect handles its own validation, applies
 * interaction rules, and provides appropriate feedback to the user.
 * <p>
 * All effects follow the MagicEffect interface contract and handle errors gracefully.
 */
public final class MagicScrollEffects {

    private MagicScrollEffects() {
        throw new UnsupportedOperationException("Effects class cannot be instantiated");
    }

    /**
     * Fire spell effect that creates fire blocks and potentially damages entities.
     * <p>
     * Clarification effects:
     * - AREA: Increases base radius
     * - MANY: Adds additional radius
     * - MUCH: Amplifies all effects
     * - LITTLE: Reduces all effects
     * - CONTROL: More precise placement, no self-damage
     * - DESTRUCTION: Damages entities in range
     * - CONSTRUCTION: Creates more stable fires
     * - MOVE: Creates fire trail or projectile effect
     *
     * @param world The world where the spell is cast
     * @param user The player casting the spell
     * @param clarifications The clarifications modifying the spell
     * @param types The magic types (should contain FIRE)
     * @return true if the spell was successfully cast, false otherwise
     */
    public static boolean fireSpell(World world, PlayerEntity user,
                                    List<MagicEnums.Clarification> clarifications,
                                    List<MagicEnums.MagicType> types) {
        try {
            // Validate parameters
            MagicEffect.validateParameters(world, user, clarifications, types);

            if (!types.contains(MagicEnums.MagicType.FIRE)) {
                return false; // Not a fire spell
            }

            // Resolve clarification conflicts
            List<MagicEnums.Clarification> effectiveClarifications =
                    MagicInteractionRules.resolveClairificationConflicts(clarifications);

            // Calculate effect parameters
            int baseRadius = MagicConstants.FIRE_BASE_RADIUS;
            int effectiveRadius = MagicInteractionRules.calculateRadiusModifier(
                    effectiveClarifications, baseRadius);

            double intensityModifier = MagicInteractionRules.calculateIntensityModifier(
                    effectiveClarifications);

            double typeInteractionModifier = MagicInteractionRules.calculateTypeInteractionMultiplier(types);

            // Apply type interactions (e.g., Fire + Water = reduced effect)
            double finalIntensity = intensityModifier * typeInteractionModifier;

            // Get center position
            BlockPos center = user.getBlockPos();

            // Handle special clarifications
            boolean isControlled = effectiveClarifications.contains(MagicEnums.Clarification.CONTROL);
            boolean isDestructive = effectiveClarifications.contains(MagicEnums.Clarification.DESTRUCTION);
            boolean isConstructive = effectiveClarifications.contains(MagicEnums.Clarification.CONSTRUCTION);
            boolean hasMovement = effectiveClarifications.contains(MagicEnums.Clarification.MOVE);

            // Apply fire placement
            int firesPlaced = placeFireBlocks(world, center, effectiveRadius, isConstructive, finalIntensity);

            // Apply entity effects if destructive
            if (isDestructive) {
                damageEntitiesInRange(world, center, effectiveRadius, user, isControlled, finalIntensity);
            }

            // Apply movement effects
            if (hasMovement) {
                applyMovementEffect(world, user, isControlled, finalIntensity);
            }

            // Provide feedback
            String intensityDesc = getIntensityDescription(finalIntensity);
            String effectDesc = getEffectDescription(effectiveClarifications);

            user.sendMessage(Text.literal(String.format(
                    "You unleash %s %s fire spell! (%d fires created)",
                    intensityDesc, effectDesc, firesPlaced)), false);

            // Play appropriate sound
            world.playSound(null, center,
                    finalIntensity > 1.5 ? SoundEvents.ENTITY_BLAZE_SHOOT : SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.PLAYERS,
                    MagicConstants.DEFAULT_SOUND_VOLUME * (float)Math.min(finalIntensity, 2.0),
                    MagicConstants.DEFAULT_SOUND_PITCH);

            return firesPlaced > 0; // Success if we placed at least one fire

        } catch (Exception e) {
            // Log error and return failure
            // Regen.LOGGER.error("Error in fire spell", e);
            return false;
        }
    }

    /**
     * Places fire blocks in the specified radius around the center position.
     */
    private static int placeFireBlocks(World world, BlockPos center, int radius,
                                       boolean isConstructive, double intensity) {
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(center, "Center position cannot be null");

        int firesPlaced = 0;
        int maxFires = (int) (50 * intensity); // Limit total fires to prevent lag

        for (BlockPos pos : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (firesPlaced >= maxFires) break;

            // Check if we can place fire here
            if (canPlaceFireAt(world, pos, isConstructive)) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                firesPlaced++;
            }
        }

        return firesPlaced;
    }

    /**
     * Checks if fire can be placed at the given position.
     */
    private static boolean canPlaceFireAt(World world, BlockPos pos, boolean isConstructive) {
        // Must be air block
        if (!world.isAir(pos)) {
            return false;
        }

        // Check if there's a suitable surface below
        BlockPos below = pos.down();

        if (isConstructive) {
            // Constructive fire can be placed on any solid block
            return world.getBlockState(below).isSolid();
        } else {
            // Regular fire needs burnable surface
            return world.getBlockState(below).isBurnable();
        }
    }

    /**
     * Damages entities in range if the spell is destructive.
     */
    private static void damageEntitiesInRange(World world, BlockPos center, int radius,
                                              PlayerEntity caster, boolean isControlled,
                                              double intensity) {
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(center, "Center position cannot be null");
        Objects.requireNonNull(caster, "Caster cannot be null");

        double actualRadius = radius + 0.5; // Add 0.5 for better coverage
        Box damageBox = new Box(center).expand(actualRadius);

        List<Entity> entities = world.getOtherEntities(null, damageBox);

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            // Skip the caster if controlled
            if (isControlled && entity == caster) continue;

            // Calculate damage based on distance and intensity
            double distance = entity.getPos().distanceTo(center.toCenterPos());
            if (distance > actualRadius) continue;

            double damageMultiplier = 1.0 - (distance / actualRadius); // Closer = more damage
            float damage = (float) (4.0 * intensity * damageMultiplier);

            // Apply fire damage
            DamageSource fireSource = world.getDamageSources().create(DamageTypes.IN_FIRE);
            livingEntity.damage(fireSource, damage);

            // Set on fire
            int fireTicks = (int) (60 * intensity * damageMultiplier); // 3 seconds base
            livingEntity.setOnFireFor(Math.max(1, fireTicks / 20));
        }
    }

    /**
     * Applies movement-based effects to the spell.
     */
    private static void applyMovementEffect(World world, PlayerEntity user,
                                            boolean isControlled, double intensity) {
        Objects.requireNonNull(world, "World cannot be null");
        Objects.requireNonNull(user, "User cannot be null");

        if (isControlled) {
            // Controlled movement: Add fire resistance
            // TODO: Apply fire resistance potion effect
            user.sendMessage(Text.literal("You feel protected from your own flames!"), true);
        } else {
            // Uncontrolled movement: Set user on fire briefly
            int fireDuration = Math.max(1, (int) (MagicConstants.FIRE_MOVE_DURATION_SECONDS * intensity));
            user.setOnFireFor(fireDuration);
        }
    }

    /**
     * Gets a description of the spell intensity.
     */
    private static String getIntensityDescription(double intensity) {
        if (intensity >= 2.0) return "devastating";
        if (intensity >= 1.5) return "powerful";
        if (intensity >= 1.0) return "moderate";
        if (intensity >= 0.5) return "weak";
        return "feeble";
    }

    /**
     * Gets a description of the spell effects based on clarifications.
     */
    private static String getEffectDescription(List<MagicEnums.Clarification> clarifications) {
        if (clarifications.contains(MagicEnums.Clarification.CONTROL)) {
            return "controlled";
        }
        if (clarifications.contains(MagicEnums.Clarification.DESTRUCTION)) {
            return "destructive";
        }
        if (clarifications.contains(MagicEnums.Clarification.CONSTRUCTION)) {
            return "constructive";
        }
        if (clarifications.contains(MagicEnums.Clarification.AREA)) {
            return "area";
        }
        return "basic";
    }
}