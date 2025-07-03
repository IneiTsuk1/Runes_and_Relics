package net.IneiTsuki.regen.magic.effect.spell;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.magic.api.MagicEffect;
import net.IneiTsuki.regen.magic.api.MagicEnums;
import net.IneiTsuki.regen.magic.core.constants.MagicConstants;
import net.IneiTsuki.regen.magic.core.utils.MagicInteractionRules;
import net.IneiTsuki.regen.magic.effect.active.ActiveSpellEffect;
import net.IneiTsuki.regen.magic.effect.active.ActiveSpellTracker;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class FireSpellEffect implements MagicEffect {

    private final Set<BlockPos> placedFirePositions = new HashSet<>();
    private final List<MagicEnums.Clarification> clarifications;
    private final List<MagicEnums.MagicType> types;

    public FireSpellEffect(List<MagicEnums.Clarification> clarifications,
                           List<MagicEnums.MagicType> types) {
        this.clarifications = MagicInteractionRules.resolveClairificationConflicts(clarifications);
        this.types = types;
    }

    @Override
    public boolean apply(World world, PlayerEntity user,
                         List<MagicEnums.Clarification> cls,
                         List<MagicEnums.MagicType> tys) {
        try {
            Objects.requireNonNull(world);
            Objects.requireNonNull(user);

            if (!types.contains(MagicEnums.MagicType.FIRE)) {
                return false;
            }

            int baseRadius = MagicConstants.FIRE_BASE_RADIUS;
            int effectiveRadius = MagicInteractionRules.calculateRadiusModifier(clarifications, baseRadius);
            double intensityModifier = MagicInteractionRules.calculateIntensityModifier(clarifications);
            double typeInteractionModifier = MagicInteractionRules.calculateTypeInteractionMultiplier(types);
            double finalIntensity = intensityModifier * typeInteractionModifier;

            BlockPos center = user.getBlockPos();

            boolean isControlled = clarifications.contains(MagicEnums.Clarification.CONTROL);
            boolean isDestructive = clarifications.contains(MagicEnums.Clarification.DESTRUCTION);
            boolean isConstructive = clarifications.contains(MagicEnums.Clarification.CONSTRUCTION);
            boolean hasMovement = clarifications.contains(MagicEnums.Clarification.MOVE);

            // Place fire blocks and track positions
            placedFirePositions.clear();
            placedFirePositions.addAll(placeFireBlocks(world, center, effectiveRadius, isConstructive, finalIntensity));

            if (isDestructive) {
                damageEntitiesInRange(world, center, effectiveRadius, user, isControlled, finalIntensity);
            }

            if (hasMovement) {
                applyMovementEffect(world, user, isControlled, finalIntensity);
            }

            String intensityDesc = getIntensityDescription(finalIntensity);
            String effectDesc = getEffectDescription(clarifications);

            user.sendMessage(Text.literal(String.format(
                    "You unleash %s %s fire spell! (%d fires created)",
                    intensityDesc, effectDesc, placedFirePositions.size())), false);

            world.playSound(null, center,
                    finalIntensity > 1.5 ? SoundEvents.ENTITY_BLAZE_SHOOT : SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.PLAYERS,
                    MagicConstants.DEFAULT_SOUND_VOLUME * (float) Math.min(finalIntensity, 2.0),
                    MagicConstants.DEFAULT_SOUND_PITCH);

            // CREATE AND REGISTER THE ACTIVE SPELL EFFECT
            if (!placedFirePositions.isEmpty()) {
                int duration = getActiveDurationTicks(world, user, cls, tys);

                // DEBUG LOGGING
                //Regen.LOGGER.info("FireSpellEffect: Registering active spell with duration: {}", duration);
                //Regen.LOGGER.info("FireSpellEffect: Active spell count before: {}", ActiveSpellTracker.getCount());

                ActiveSpellEffect activeSpell = new ActiveSpellEffect(
                        user, this, cls, tys, duration
                );
                ActiveSpellTracker.add(activeSpell);

                //Regen.LOGGER.info("FireSpellEffect: Active spell count after: {}", ActiveSpellTracker.getCount());
                //Regen.LOGGER.info("FireSpellEffect: Successfully registered active spell");

                return true;
            }

            Regen.LOGGER.warn("FireSpellEffect: No fire blocks placed, not registering active spell");
            return false;

        } catch (Exception e) {
            Regen.LOGGER.error("FireSpellEffect: Error in apply method", e);
            return false;
        }
    }

    @Override
    public boolean canApply(World world, PlayerEntity user,
                            List<MagicEnums.Clarification> cls,
                            List<MagicEnums.MagicType> tys) {
        return true; // No special restrictions
    }

    @Override
    public int getCastDelayTicks(World world, PlayerEntity user,
                                 List<MagicEnums.Clarification> cls,
                                 List<MagicEnums.MagicType> tys) {
        return 0;
    }

    @Override
    public int getActiveDurationTicks(World world, PlayerEntity user,
                                      List<MagicEnums.Clarification> cls,
                                      List<MagicEnums.MagicType> tys) {
        return MagicConstants.FIRE_EFFECT_DURATION_TICKS;
    }

    @Override
    public void onTick(World world, PlayerEntity user,
                       List<MagicEnums.Clarification> cls,
                       List<MagicEnums.MagicType> tys,
                       int ticksRemaining) {
        //Regen.LOGGER.info("FireSpellEffect onTick: ticksRemaining={}, user={}", ticksRemaining, user.getName().getString());
        // Optional periodic effects
    }

    @Override
    public void onEnd(World world, PlayerEntity user,
                      List<MagicEnums.Clarification> cls,
                      List<MagicEnums.MagicType> tys) {
        //Regen.LOGGER.info("FireSpellEffect onEnd called for user: {}", user.getName().getString());

        // Remove all placed fire blocks
        int removedCount = 0;
        for (BlockPos pos : placedFirePositions) {
            if (world.getBlockState(pos).isOf(Blocks.FIRE)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                removedCount++;
            }
        }
        placedFirePositions.clear();

        //Regen.LOGGER.info("FireSpellEffect onEnd: Removed {} fire blocks", removedCount);

        user.sendMessage(Text.literal("Your fire spell fades away."), true);
    }

    private static Set<BlockPos> placeFireBlocks(World world, BlockPos center, int radius,
                                                 boolean isConstructive, double intensity) {
        Set<BlockPos> placed = new HashSet<>();
        int maxFires = (int) (50 * intensity);

        for (BlockPos pos : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (placed.size() >= maxFires) break;

            if (canPlaceFireAt(world, pos, isConstructive)) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                placed.add(pos.toImmutable());
            }
        }
        return placed;
    }

    private static boolean canPlaceFireAt(World world, BlockPos pos, boolean isConstructive) {
        if (!world.isAir(pos)) {
            return false;
        }
        BlockPos below = pos.down();
        if (isConstructive) {
            return world.getBlockState(below).isSolid();
        } else {
            return world.getBlockState(below).isBurnable();
        }
    }

    private static void damageEntitiesInRange(World world, BlockPos center, int radius,
                                              PlayerEntity caster, boolean isControlled,
                                              double intensity) {
        double actualRadius = radius + 0.5;
        Box damageBox = new Box(center).expand(actualRadius);
        List<Entity> entities = world.getOtherEntities(null, damageBox);
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (isControlled && entity == caster) continue;

            double distance = entity.getPos().distanceTo(center.toCenterPos());
            if (distance > actualRadius) continue;

            double damageMultiplier = 1.0 - (distance / actualRadius);
            float damage = (float) (4.0 * intensity * damageMultiplier);

            livingEntity.damage(world.getDamageSources().create(net.minecraft.entity.damage.DamageTypes.IN_FIRE), damage);

            int fireTicks = (int) (60 * intensity * damageMultiplier);
            livingEntity.setOnFireFor(Math.max(1, fireTicks / 20));
        }
    }

    private static void applyMovementEffect(World world, PlayerEntity user,
                                            boolean isControlled, double intensity) {
        if (isControlled) {
            user.sendMessage(Text.literal("You feel protected from your own flames!"), true);
            // Optionally add fire resistance potion effect here
        } else {
            int fireDuration = Math.max(1, (int) (MagicConstants.FIRE_MOVE_DURATION_SECONDS * intensity));
            user.setOnFireFor(fireDuration);
        }
    }

    private static String getIntensityDescription(double intensity) {
        if (intensity >= 2.0) return "devastating";
        if (intensity >= 1.5) return "powerful";
        if (intensity >= 1.0) return "moderate";
        if (intensity >= 0.5) return "weak";
        return "feeble";
    }

    private static String getEffectDescription(List<MagicEnums.Clarification> clarifications) {
        if (clarifications.contains(MagicEnums.Clarification.CONTROL)) return "controlled";
        if (clarifications.contains(MagicEnums.Clarification.DESTRUCTION)) return "destructive";
        if (clarifications.contains(MagicEnums.Clarification.CONSTRUCTION)) return "constructive";
        if (clarifications.contains(MagicEnums.Clarification.AREA)) return "area";
        return "basic";
    }
}
