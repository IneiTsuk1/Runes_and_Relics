package net.IneiTsuki.regen.magic.item;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.magic.api.MagicEnums;
import net.IneiTsuki.regen.magic.core.constants.MagicConstants;
import net.IneiTsuki.regen.magic.api.MagicEnums.Clarification;
import net.IneiTsuki.regen.magic.api.MagicEnums.MagicType;
import net.IneiTsuki.regen.magic.effect.spell.FireSpellEffect;
import net.IneiTsuki.regen.magic.effect.scroll.MagicScrollEffects;
import net.IneiTsuki.regen.magic.api.MagicEffect;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry and factory for magic scroll items.
 *
 * This class manages the registration of all magic scroll combinations,
 * provides thread-safe access to registered scrolls, and handles the
 * complex logic of scroll name generation and custom effect assignment.
 *
 * Scrolls are registered for every combination of clarifications and magic types,
 * with special effects assigned to specific combinations as needed.
 */
public final class MagicScrollItems {

    /**
     * Thread-safe map of all registered magic scrolls.
     * Key is the scroll name (without prefix), value is the scroll item.
     */
    private static final Map<String, MagicScrollItem> MAGIC_SCROLLS = new ConcurrentHashMap<>();

    /**
     * Set of registered scroll names to prevent duplicates.
     */
    private static final Set<String> REGISTERED_NAMES = ConcurrentHashMap.newKeySet();

    /**
     * Cached sorted list of scrolls for performance in creative menu.
     */
    private static volatile List<MagicScrollItem> cachedSortedScrolls = null;

    /**
     * Lock for registration operations to ensure thread safety.
     */
    private static final Object REGISTRATION_LOCK = new Object();

    private MagicScrollItems() {
        throw new UnsupportedOperationException("Registry class cannot be instantiated");
    }

    /**
     * Registers all magic scroll items with the game registry.
     * This method should be called during mod initialization.
     */
    public static void registerItems() {
        synchronized (REGISTRATION_LOCK) {
            Regen.LOGGER.info("Beginning magic scroll registration...");

            try {
                // Clear any existing registrations (for hot reloading scenarios)
                clearRegistrations();

                // Register basic 1x1 scrolls (single clarification + single magic type)
                registerBasicScrolls();

                // Register compound scrolls (custom combinations)
                registerCompoundScrolls();

                // Invalidate cached sorted list
                cachedSortedScrolls = null;

                Regen.LOGGER.info("Successfully registered {} magic scrolls", MAGIC_SCROLLS.size());

            } catch (Exception e) {
                Regen.LOGGER.error("Failed to register magic scrolls", e);
                throw new RuntimeException("Magic scroll registration failed", e);
            }
        }
    }

    /**
     * Clears all registrations (for hot reloading scenarios).
     */
    private static void clearRegistrations() {
        MAGIC_SCROLLS.clear();
        REGISTERED_NAMES.clear();
        cachedSortedScrolls = null;
    }

    /**
     * Registers basic scrolls for each clarification-type combination.
     */
    private static void registerBasicScrolls() {
        int registeredCount = 0;
        int totalCombinations = Clarification.values().length * MagicType.values().length;

        for (Clarification clarification : Clarification.values()) {
            for (MagicType magicType : MagicType.values()) {
                try {
                    List<Clarification> clarifications = List.of(clarification);
                    List<MagicType> types = List.of(magicType);

                    // Default effect - just sends a message
                    MagicEffect effect = createDefaultEffect(clarifications, types);

                    // Override with custom effects for specific combinations
                    effect = getCustomEffectOrDefault(clarifications, types, effect);

                    registerMagicScroll(clarifications, types, effect);
                    registeredCount++;

                } catch (Exception e) {
                    Regen.LOGGER.warn("Failed to register basic scroll for {} + {}: {}",
                            clarification.getName(), magicType.getName(), e.getMessage());
                }
            }
        }

        Regen.LOGGER.info("Registered {}/{} basic scroll combinations", registeredCount, totalCombinations);
    }

    /**
     * Registers compound scrolls with multiple clarifications or types.
     */
    private static void registerCompoundScrolls() {
        int registeredCount = 0;

        try {
            // Fire: Area + Many — with 2s casting delay (40 ticks), 10s duration (200 ticks)
            MagicEffect fireEffect = new FireSpellEffect(
                    List.of(Clarification.AREA, Clarification.MANY),
                    List.of(MagicType.FIRE)
            );
            registerMagicScroll(
                    List.of(Clarification.AREA, Clarification.MANY),
                    List.of(MagicType.FIRE),
                    wrapWithDelayAndDuration(fireEffect, 40, 200)
            );
            registeredCount++;

            // Fire + Water = Steam — with 3s delay
            registerMagicScroll(
                    List.of(Clarification.CONTROL),
                    List.of(MagicType.FIRE, MagicType.WATER),
                    wrapWithDelayAndDuration(createSteamEffect(), 60, 0)
            );
            registeredCount++;

            // Much Destruction Fire + Ice = Thermal Shock — 4s delay
            registerMagicScroll(
                    List.of(Clarification.MUCH, Clarification.DESTRUCTION),
                    List.of(MagicType.FIRE, MagicType.ICE),
                    wrapWithDelayAndDuration(createThermalShockEffect(), 80, 0)
            );
            registeredCount++;

        } catch (Exception e) {
            Regen.LOGGER.error("Failed to register compound scrolls", e);
        }

        Regen.LOGGER.info("Registered {} compound scroll combinations", registeredCount);
    }

    /**
     * Registers a single magic scroll with the given parameters.
     *
     * @param clarifications The clarifications for the scroll (must not be null or empty)
     * @param types The magic types for the scroll (must not be null or empty)
     * @param effect The magic effect to apply (must not be null)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws IllegalStateException if a scroll with the same name is already registered
     */
    private static void registerMagicScroll(List<Clarification> clarifications,
                                            List<MagicType> types,
                                            MagicEffect effect) {
        // Validate inputs
        validateRegistrationParameters(clarifications, types, effect);

        String name = generateScrollName(clarifications, types);

        // Check for duplicate registration
        if (!REGISTERED_NAMES.add(name)) {
            throw new IllegalStateException("Scroll with name '" + name + "' is already registered");
        }

        try {
            MagicScrollItem item = new MagicScrollItem(
                    new Item.Settings().maxCount(MagicConstants.SCROLL_MAX_STACK_SIZE),
                    clarifications,
                    types,
                    effect
            );

            Registry.register(Registries.ITEM, Regen.id(name), item);
            MAGIC_SCROLLS.put(name, item);

        } catch (Exception e) {
            // Remove from registered names if registration failed
            REGISTERED_NAMES.remove(name);
            throw new RuntimeException("Failed to register scroll: " + name, e);
        }
    }

    /**
     * Validates registration parameters.
     */
    private static void validateRegistrationParameters(List<Clarification> clarifications,
                                                       List<MagicType> types,
                                                       MagicEffect effect) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");
        Objects.requireNonNull(effect, "Magic effect cannot be null");

        if (clarifications.isEmpty()) {
            throw new IllegalArgumentException("Clarifications list cannot be empty");
        }

        if (types.isEmpty()) {
            throw new IllegalArgumentException("Magic types list cannot be empty");
        }

        // Check for null elements in lists
        if (clarifications.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Clarifications list cannot contain null elements");
        }

        if (types.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Magic types list cannot contain null elements");
        }
    }

    /**
     * Generates a unique name for a scroll based on its clarifications and types.
     *
     * @param clarifications The clarifications for the scroll
     * @param types The magic types for the scroll
     * @return The generated scroll name (without prefix)
     */
    private static String generateScrollName(List<Clarification> clarifications,
                                             List<MagicType> types) {
        try {
            String clarificationPart = clarifications.stream()
                    .map(Clarification::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(MagicConstants.SCROLL_NAME_SEPARATOR));

            String typePart = types.stream()
                    .map(MagicType::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(MagicConstants.SCROLL_NAME_SEPARATOR));

            return MagicConstants.SCROLL_NAME_PREFIX +
                    clarificationPart +
                    MagicConstants.SCROLL_NAME_SEPARATOR +
                    typePart;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate scroll name", e);
        }
    }

    /**
     * Creates a default effect that sends a message to the user.
     */
    private static MagicEffect createDefaultEffect(List<Clarification> clarifications,
                                                   List<MagicType> types) {
        return (world, user, cls, tys) -> {
            try {
                String clarificationNames = cls.stream()
                        .map(Clarification::getFormattedName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "));

                String typeNames = tys.stream()
                        .map(MagicType::getFormattedName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "));

                String message = String.format(MagicConstants.SUCCESS_SCROLL_CAST,
                        clarificationNames, typeNames);

                user.sendMessage(Text.literal(message), false);
                return true;

            } catch (Exception e) {
                Regen.LOGGER.error("Failed to execute default scroll effect", e);
                return false;
            }
        };
    }

    /**
     * Gets a custom effect for specific combinations, or returns the default.
     */
    private static MagicEffect getCustomEffectOrDefault(List<Clarification> clarifications,
                                                        List<MagicType> types,
                                                        MagicEffect defaultEffect) {
        // Control + Fire combination gets fire spell
        if (isSingleCombination(clarifications, types, Clarification.CONTROL, MagicType.FIRE)) {
            return MagicScrollEffects::fireSpell;
        }

        // Add more custom combinations here as needed
        // Examples:
        // if (isSingleCombination(clarifications, types, Clarification.DESTRUCTION, MagicType.EARTH)) {
        //     return MagicScrollEffects::earthquakeSpell;
        // }

        return defaultEffect;
    }

    /**
     * Helper method to check if lists contain exactly one specific clarification and type.
     */
    private static boolean isSingleCombination(List<Clarification> clarifications,
                                               List<MagicType> types,
                                               Clarification expectedClarification,
                                               MagicType expectedType) {
        return clarifications.size() == 1 &&
                types.size() == 1 &&
                clarifications.contains(expectedClarification) &&
                types.contains(expectedType);
    }

    /**
     * Creates a steam effect for Fire + Water combinations.
     */
    private static MagicEffect createSteamEffect() {
        return (world, user, clarifications, types) -> {
            try {
                // TODO: Implement steam effect
                user.sendMessage(Text.literal("You create a cloud of steam!"), false);
                return true;
            } catch (Exception e) {
                Regen.LOGGER.error("Failed to execute steam effect", e);
                return false;
            }
        };
    }

    /**
     * Creates a thermal shock effect for Fire + Ice combinations.
     */
    private static MagicEffect createThermalShockEffect() {
        return (world, user, clarifications, types) -> {
            try {
                // TODO: Implement thermal shock effect
                user.sendMessage(Text.literal("You unleash devastating thermal shock!"), false);
                return true;
            } catch (Exception e) {
                Regen.LOGGER.error("Failed to execute thermal shock effect", e);
                return false;
            }
        };
    }

    /**
     * Adds all registered magic scrolls to the creative inventory.
     * Uses caching for performance optimization.
     *
     * @param entries The item group entries to add scrolls to
     */
    public static void addItemsToItemGroup(FabricItemGroupEntries entries) {
        Objects.requireNonNull(entries, "Entries cannot be null");

        // Use cached sorted list if available
        List<MagicScrollItem> sortedScrolls = cachedSortedScrolls;
        if (sortedScrolls == null) {
            synchronized (REGISTRATION_LOCK) {
                sortedScrolls = cachedSortedScrolls;
                if (sortedScrolls == null) {
                    sortedScrolls = createSortedScrollsList();
                    cachedSortedScrolls = sortedScrolls;
                }
            }
        }

        // Add scrolls to entries
        sortedScrolls.forEach(entries::add);
    }

    /**
     * Creates a sorted list of scrolls for the creative menu.
     */
    private static List<MagicScrollItem> createSortedScrollsList() {
        return MAGIC_SCROLLS.values().stream()
                .sorted((a, b) -> {
                    // Sort by stability first (stable scrolls first)
                    if (a.isStable() != b.isStable()) {
                        return a.isStable() ? -1 : 1;
                    }
                    // Then by complexity (fewer clarifications/types first)
                    int complexityA = a.getClarifications().size() + a.getMagicTypes().size();
                    int complexityB = b.getClarifications().size() + b.getMagicTypes().size();
                    if (complexityA != complexityB) {
                        return Integer.compare(complexityA, complexityB);
                    }
                    // Finally by name (deterministic ordering)
                    return compareScrollsByName(a, b);
                })
                .collect(Collectors.toList());
    }

    /**
     * Compares two scrolls by their generated names.
     */
    private static int compareScrollsByName(MagicScrollItem a, MagicScrollItem b) {
        String nameA = generateScrollName(a.getClarifications(), a.getMagicTypes());
        String nameB = generateScrollName(b.getClarifications(), b.getMagicTypes());
        return nameA.compareTo(nameB);
    }

    /**
     * Gets a registered magic scroll by its clarifications and types.
     *
     * @param clarifications The clarifications to search for
     * @param types The magic types to search for
     * @return The matching scroll, or empty if not found
     */
    public static Optional<MagicScrollItem> getScroll(List<Clarification> clarifications,
                                                      List<MagicType> types) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");

        if (clarifications.isEmpty() || types.isEmpty()) {
            return Optional.empty();
        }

        try {
            String name = generateScrollName(clarifications, types);
            return Optional.ofNullable(MAGIC_SCROLLS.get(name));
        } catch (Exception e) {
            Regen.LOGGER.warn("Failed to get scroll for combination: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Gets all registered magic scrolls.
     * Returns a defensive copy to prevent external modification.
     *
     * @return Immutable map of all registered scrolls
     */
    public static Map<String, MagicScrollItem> getAllScrolls() {
        return Map.copyOf(MAGIC_SCROLLS);
    }

    /**
     * Wraps a magic effect with custom delay and duration.
     * This creates a new effect that overrides the timing methods.
     */
    private static MagicEffect wrapWithDelayAndDuration(MagicEffect base, int delayTicks, int durationTicks) {
        Objects.requireNonNull(base, "Base effect cannot be null");

        return new MagicEffect() {
            @Override
            public boolean apply(World world, PlayerEntity user,
                                 List<MagicEnums.Clarification> cls,
                                 List<MagicEnums.MagicType> tys) {
                return base.apply(world, user, cls, tys);
            }

            @Override
            public boolean canApply(World world, PlayerEntity user,
                                    List<MagicEnums.Clarification> cls,
                                    List<MagicEnums.MagicType> tys) {
                return base.canApply(world, user, cls, tys);
            }

            @Override
            public int getCastDelayTicks(World world, PlayerEntity user,
                                         List<MagicEnums.Clarification> cls,
                                         List<MagicEnums.MagicType> tys) {
                return Math.max(0, delayTicks);
            }

            @Override
            public int getActiveDurationTicks(World world, PlayerEntity user,
                                              List<MagicEnums.Clarification> cls,
                                              List<MagicEnums.MagicType> tys) {
                return Math.max(0, durationTicks);
            }

            @Override
            public void onTick(World world, PlayerEntity user,
                               List<MagicEnums.Clarification> cls,
                               List<MagicEnums.MagicType> tys,
                               int ticksRemaining) {
                base.onTick(world, user, cls, tys, ticksRemaining);
            }

            @Override
            public void onEnd(World world, PlayerEntity user,
                              List<MagicEnums.Clarification> cls,
                              List<MagicEnums.MagicType> tys) {
                base.onEnd(world, user, cls, tys);
            }
        };
    }

    /**
     * Gets the number of registered scrolls.
     *
     * @return The count of registered scrolls
     */
    public static int getScrollCount() {
        return MAGIC_SCROLLS.size();
    }

    /**
     * Checks if a scroll with the given combination exists.
     *
     * @param clarifications The clarifications to check
     * @param types The magic types to check
     * @return true if a scroll exists, false otherwise
     */
    public static boolean scrollExists(List<Clarification> clarifications,
                                       List<MagicType> types) {
        return getScroll(clarifications, types).isPresent();
    }

    /**
     * Gets statistics about registered scrolls.
     *
     * @return A map containing various statistics
     */
    public static Map<String, Object> getRegistrationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalScrolls", MAGIC_SCROLLS.size());
        stats.put("basicScrolls", (long) Clarification.values().length * MagicType.values().length);
        stats.put("compoundScrolls", MAGIC_SCROLLS.size() - (long) Clarification.values().length * MagicType.values().length);

        // Count by complexity
        Map<Integer, Long> complexityCount = MAGIC_SCROLLS.values().stream()
                .collect(Collectors.groupingBy(
                        scroll -> scroll.getClarifications().size() + scroll.getMagicTypes().size(),
                        Collectors.counting()
                ));
        stats.put("complexityDistribution", complexityCount);

        return stats;
    }
}