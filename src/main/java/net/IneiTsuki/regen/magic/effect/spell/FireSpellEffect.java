package net.IneiTsuki.regen.magic.effect.spell;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.magic.api.MagicEffect;
import net.IneiTsuki.regen.magic.api.MagicEnums;
import net.IneiTsuki.regen.magic.components.ManaComponent;
import net.IneiTsuki.regen.magic.components.ModComponents;
import net.IneiTsuki.regen.magic.core.constants.MagicConstants;
import net.IneiTsuki.regen.magic.core.utils.MagicInteractionRules;
import net.IneiTsuki.regen.magic.effect.active.ActiveSpellEffect;
import net.IneiTsuki.regen.magic.effect.active.ActiveSpellTracker;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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

            ManaComponent mana = ModComponents.MANA.get(user);
            int manaCost = calculateManaCost(cls, tys);

            if (!mana.hasEnoughMana(manaCost)) {
                user.sendMessage(Text.literal("Not enough mana to cast this spell!"), true);
                return false;
            }

            // Consume mana and sync
            mana.consumeMana(manaCost);
            mana.syncToClient();

            if (!types.contains(MagicEnums.MagicType.FIRE)) {
                return false;
            }

            int baseRadius = MagicConstants.FIRE_BASE_RADIUS;
            int effectiveRadius = MagicInteractionRules.calculateRadiusModifier(clarifications, baseRadius);
            double intensityModifier = MagicInteractionRules.calculateIntensityModifier(clarifications);
            double typeInteractionModifier = MagicInteractionRules.calculateTypeInteractionMultiplier(types);
            double finalIntensity = intensityModifier * typeInteractionModifier;

            BlockPos center = user.getBlockPos();

            boolean isConstructive = clarifications.contains(MagicEnums.Clarification.CONSTRUCTION);

            // Place fire blocks and track positions
            placedFirePositions.clear();
            placedFirePositions.addAll(placeFireBlocks(world, center, effectiveRadius, isConstructive, finalIntensity));

            if (placedFirePositions.isEmpty()) {
                Regen.LOGGER.warn("FireSpellEffect: No fire blocks placed, not registering active spell");
                return false;
            }

// Consume mana and sync
            mana.consumeMana(manaCost);
            mana.syncToClient();

            if (!types.contains(MagicEnums.MagicType.FIRE)) {
                return false;
            }

            int duration = getActiveDurationTicks(world, user, cls, tys);

// CREATE AND REGISTER THE ACTIVE SPELL EFFECT
            ActiveSpellEffect activeSpell = new ActiveSpellEffect(
                    user, this, cls, tys, duration
            );
            ActiveSpellTracker.add(activeSpell);

// Send message only after successful activation
            user.sendMessage(Text.literal(String.format(
                    "You unleash %s %s fire spell! (%d fires created)",
                    getIntensityDescription(finalIntensity),
                    getEffectDescription(clarifications),
                    placedFirePositions.size()
            )), false);

            world.playSound(null, center,
                    finalIntensity > 1.5 ? SoundEvents.ENTITY_BLAZE_SHOOT : SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.PLAYERS,
                    MagicConstants.DEFAULT_SOUND_VOLUME * (float) Math.min(finalIntensity, 2.0),
                    MagicConstants.DEFAULT_SOUND_PITCH);

            return true;

        } catch (Exception e) {
            Regen.LOGGER.error("FireSpellEffect: Error in apply method", e);
            return false;
        }
    }

    private int calculateManaCost(List<MagicEnums.Clarification> clarifications, List<MagicEnums.MagicType> types) {
        // Simple example: base 10 mana, +5 per clarification, +10 if FIRE type present
        int cost = 10;
        cost += clarifications.size() * 5;
        if (types.contains(MagicEnums.MagicType.FIRE)) {
            cost += 10;
        }
        return cost;
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
        for (BlockPos pos : placedFirePositions) {
            if (world.getBlockState(pos).isOf(Blocks.FIRE)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
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
            return world.getBlockState(below).isSolidBlock(world, below);
        } else {
            return world.getBlockState(below).isBurnable();
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
