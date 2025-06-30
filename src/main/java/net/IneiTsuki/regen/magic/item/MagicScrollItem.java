package net.IneiTsuki.regen.magic.item;

import net.IneiTsuki.regen.magic.MagicConstants;
import net.IneiTsuki.regen.magic.MagicEnums;
import net.IneiTsuki.regen.magic.MagicInteractionRules;
import net.IneiTsuki.regen.magic.interfaces.MagicEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

/**
 * Represents a magic scroll item that can be used to cast spells.
 *
 * Magic scrolls contain immutable lists of clarifications and magic types
 * that define their spell effects. When used, they apply their magic effect
 * and are consumed (unless the player is in creative mode).
 *
 * The scroll validates its combination stability and provides detailed
 * tooltips showing the magical components and their interactions.
 */
public class MagicScrollItem extends Item {
    private final List<MagicEnums.Clarification> clarifications;
    private final List<MagicEnums.MagicType> types;
    private final MagicEffect effect;
    private final boolean isStable;

    /**
     * Creates a new magic scroll item.
     *
     * @param settings The item settings
     * @param clarifications The clarifications for this scroll (must not be null or empty)
     * @param types The magic types for this scroll (must not be null or empty)
     * @param effect The magic effect to apply when used (must not be null)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public MagicScrollItem(Settings settings,
                           List<MagicEnums.Clarification> clarifications,
                           List<MagicEnums.MagicType> types,
                           MagicEffect effect) {
        super(settings);

        // Validate inputs
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");
        Objects.requireNonNull(effect, "Magic effect cannot be null");

        if (clarifications.isEmpty()) {
            throw new IllegalArgumentException("Clarifications list cannot be empty");
        }

        if (types.isEmpty()) {
            throw new IllegalArgumentException("Magic types list cannot be empty");
        }

        // Store immutable copies
        this.clarifications = List.copyOf(clarifications);
        this.types = List.copyOf(types);
        this.effect = effect;
        this.isStable = MagicInteractionRules.isStableCombination(clarifications, types);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // Validate we can use this item
        if (world == null || user == null) {
            return TypedActionResult.fail(itemStack);
        }

        if (!world.isClient()) {
            try {
                // Check if combination is stable
                if (!isStable) {
                    user.sendMessage(Text.literal(MagicConstants.ERROR_INVALID_COMBINATION)
                            .formatted(Formatting.RED), false);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,
                            MagicConstants.DEFAULT_SOUND_VOLUME, MagicConstants.DEFAULT_SOUND_PITCH);
                    return TypedActionResult.fail(itemStack);
                }

                // Validate effect can be applied
                if (!effect.canApply(world, user, clarifications, types)) {
                    user.sendMessage(Text.literal(MagicConstants.ERROR_INSUFFICIENT_POWER)
                            .formatted(Formatting.YELLOW), false);
                    return TypedActionResult.fail(itemStack);
                }

                // Play scroll sound
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS,
                        MagicConstants.DEFAULT_SOUND_VOLUME, MagicConstants.DEFAULT_SOUND_PITCH);

                // Apply the magic effect
                boolean success = effect.apply(world, user, clarifications, types);

                if (success) {
                    // Consume the scroll (unless creative mode)
                    if (!user.getAbilities().creativeMode) {
                        itemStack.decrement(1);
                    }
                    return TypedActionResult.success(itemStack, false);
                } else {
                    // Effect failed
                    user.sendMessage(Text.literal(MagicConstants.ERROR_SPELL_BACKFIRE)
                            .formatted(Formatting.RED), false);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,
                            MagicConstants.DEFAULT_SOUND_VOLUME, MagicConstants.DEFAULT_SOUND_PITCH);
                    return TypedActionResult.fail(itemStack);
                }

            } catch (Exception e) {
                // Handle any unexpected errors gracefully
                user.sendMessage(Text.literal(MagicConstants.ERROR_SPELL_BACKFIRE)
                        .formatted(Formatting.RED), false);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,
                        MagicConstants.DEFAULT_SOUND_VOLUME, MagicConstants.DEFAULT_SOUND_PITCH);

                // Log the error for debugging (you might want to use your mod's logger)
                // Regen.LOGGER.error("Error applying magic effect", e);

                return TypedActionResult.fail(itemStack);
            }
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        // Stability warning
        if (!isStable) {
            tooltip.add(Text.literal("⚠ Unstable Combination ⚠")
                    .formatted(Formatting.BOLD, Formatting.RED));
            tooltip.add(Text.literal("This scroll may backfire!")
                    .formatted(Formatting.ITALIC, Formatting.DARK_RED));
            tooltip.add(Text.empty()); // Empty line for spacing
        }

        // Clarifications section
        tooltip.add(Text.literal("Clarifications:")
                .formatted(Formatting.GRAY, Formatting.UNDERLINE));
        for (var clarification : clarifications) {
            tooltip.add(Text.literal("• " + clarification.getFormattedName())
                    .formatted(Formatting.BOLD)
                    .styled(style -> style.withColor(clarification.getColor())));
        }

        tooltip.add(Text.empty()); // Empty line for spacing

        // Magic types section
        tooltip.add(Text.literal("Magic Types:")
                .formatted(Formatting.GRAY, Formatting.UNDERLINE));
        for (var magicType : types) {
            tooltip.add(Text.literal("• " + magicType.getFormattedName())
                    .formatted(Formatting.BOLD)
                    .styled(style -> style.withColor(magicType.getColor())));
        }

        tooltip.add(Text.empty()); // Empty line for spacing

        // Interaction effects
        String modifications = MagicInteractionRules.getModificationDescription(clarifications, types);
        tooltip.add(Text.literal("Effect: " + modifications)
                .formatted(Formatting.ITALIC, Formatting.AQUA));

        // Usage instructions
        tooltip.add(Text.empty()); // Empty line for spacing
        tooltip.add(Text.literal("Right-click to cast spell")
                .formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
    }

    /**
     * Gets an immutable copy of the clarifications for this scroll.
     *
     * @return Immutable list of clarifications
     */
    public List<MagicEnums.Clarification> getClarifications() {
        return clarifications; // Already immutable from constructor
    }

    /**
     * Gets an immutable copy of the magic types for this scroll.
     *
     * @return Immutable list of magic types
     */
    public List<MagicEnums.MagicType> getMagicTypes() {
        return types; // Already immutable from constructor
    }

    /**
     * Gets the magic effect for this scroll.
     *
     * @return The magic effect
     */
    public MagicEffect getEffect() {
        return effect;
    }

    /**
     * Checks if this scroll's combination is stable.
     *
     * @return true if the combination is stable, false if it may backfire
     */
    public boolean isStable() {
        return isStable;
    }
}