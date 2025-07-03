package net.IneiTsuki.regen.magic.item;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.magic.core.MagicConstants;
import net.IneiTsuki.regen.magic.api.MagicEnums.Clarification;
import net.IneiTsuki.regen.magic.api.MagicEnums.MagicType;
import net.IneiTsuki.regen.magic.effect.MagicScrollEffects;
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

    private MagicScrollItems() {
        throw new UnsupportedOperationException("Registry class cannot be instantiated");
    }

    /**
     * Registers all magic scroll items with the game registry.
     * This method should be called during mod initialization.
     */
    public static void registerItems() {
        Regen.LOGGER.info("Beginning magic scroll registration...");

        try {
            // Register basic 1x1 scrolls (single clarification + single magic type)
            registerBasicScrolls();

            // Register compound scrolls (custom combinations)
            registerCompoundScrolls();

            Regen.LOGGER.info("Successfully registered {} magic scrolls", MAGIC_SCROLLS.size());

        } catch (Exception e) {
            Regen.LOGGER.error("Failed to register magic scrolls", e);
            throw new RuntimeException("Magic scroll registration failed", e);
        }
    }

    /**
     * Registers basic scrolls for each clarification-type combination.
     */
    private static void registerBasicScrolls() {
        for (Clarification clarification : Clarification.values()) {
            for (MagicType magicType : MagicType.values()) {

                List<Clarification> clarifications = List.of(clarification);
                List<MagicType> types = List.of(magicType);

                // Default effect - just sends a message
                MagicEffect effect = createDefaultEffect(clarifications, types);

                // Override with custom effects for specific combinations
                effect = getCustomEffectOrDefault(clarifications, types, effect);

                registerMagicScroll(clarifications, types, effect);
            }
        }
    }

    /**
     * Registers compound scrolls with multiple clarifications or types.
     */
    private static void registerCompoundScrolls() {
        // Fire: Area + Many — with 2s casting delay
        registerMagicScroll(
                List.of(Clarification.AREA, Clarification.MANY),
                List.of(MagicType.FIRE),
                wrapWithDelay(MagicScrollEffects::fireSpell, 40) // 2 seconds
        );

        // Fire + Water = Steam — with 3s delay
        registerMagicScroll(
                List.of(Clarification.CONTROL),
                List.of(MagicType.FIRE, MagicType.WATER),
                wrapWithDelay(createSteamEffect(), 60) // 3 seconds
        );

        // Much Destruction Fire + Ice = Thermal Shock — 4s delay
        registerMagicScroll(
                List.of(Clarification.MUCH, Clarification.DESTRUCTION),
                List.of(MagicType.FIRE, MagicType.ICE),
                wrapWithDelay(createThermalShockEffect(), 80) // 4 seconds
        );
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
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");
        Objects.requireNonNull(effect, "Magic effect cannot be null");

        if (clarifications.isEmpty()) {
            throw new IllegalArgumentException("Clarifications list cannot be empty");
        }

        if (types.isEmpty()) {
            throw new IllegalArgumentException("Magic types list cannot be empty");
        }

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
     * Generates a unique name for a scroll based on its clarifications and types.
     *
     * @param clarifications The clarifications for the scroll
     * @param types The magic types for the scroll
     * @return The generated scroll name (without prefix)
     */
    private static String generateScrollName(List<Clarification> clarifications,
                                             List<MagicType> types) {
        String clarificationPart = clarifications.stream()
                .map(Clarification::getName)
                .collect(Collectors.joining(MagicConstants.SCROLL_NAME_SEPARATOR));

        String typePart = types.stream()
                .map(MagicType::getName)
                .collect(Collectors.joining(MagicConstants.SCROLL_NAME_SEPARATOR));

        return MagicConstants.SCROLL_NAME_PREFIX +
                clarificationPart +
                MagicConstants.SCROLL_NAME_SEPARATOR +
                typePart;
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
                        .collect(Collectors.joining(" "));

                String typeNames = tys.stream()
                        .map(MagicType::getFormattedName)
                        .collect(Collectors.joining(" "));

                String message = String.format(MagicConstants.SUCCESS_SCROLL_CAST,
                        clarificationNames, typeNames);

                user.sendMessage(Text.literal(message), false);
                return true;

            } catch (Exception e) {
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
        if (clarifications.contains(Clarification.CONTROL) &&
                types.contains(MagicType.FIRE) &&
                clarifications.size() == 1 &&
                types.size() == 1) {
            return MagicScrollEffects::fireSpell;
        }

        // Add more custom combinations here as needed

        return defaultEffect;
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
                return false;
            }
        };
    }

    /**
     * Adds all registered magic scrolls to the creative inventory.
     *
     * @param entries The item group entries to add scrolls to
     */
    public static void addItemsToItemGroup(FabricItemGroupEntries entries) {
        Objects.requireNonNull(entries, "Entries cannot be null");

        // Add scrolls in a deterministic order (by name)
        MAGIC_SCROLLS.values().stream()
                .sorted((a, b) -> {
                    // Sort by stability first (stable scrolls first), then by name
                    if (a.isStable() != b.isStable()) {
                        return a.isStable() ? -1 : 1;
                    }
                    // Compare by registry name if available, otherwise by clarifications/types
                    return 0; // Maintain insertion order for items with same stability
                })
                .forEach(entries::add);
    }

    /**
     * Gets a registered magic scroll by its clarifications and types.
     *
     * @param clarifications The clarifications to search for
     * @param types The magic types to search for
     * @return The matching scroll, or null if not found
     */
    public static Optional<MagicScrollItem> getScroll(List<Clarification> clarifications,
                                                      List<MagicType> types) {
        Objects.requireNonNull(clarifications, "Clarifications cannot be null");
        Objects.requireNonNull(types, "Magic types cannot be null");

        if (clarifications.isEmpty() || types.isEmpty()) {
            return Optional.empty();
        }

        String name = generateScrollName(clarifications, types);
        return Optional.ofNullable(MAGIC_SCROLLS.get(name));
    }

    /**
     * Gets all registered magic scrolls.
     *
     * @return Immutable map of all registered scrolls
     */
    public static Map<String, MagicScrollItem> getAllScrolls() {
        return Map.copyOf(MAGIC_SCROLLS);
    }

    private static MagicEffect wrapWithDelay(MagicEffect base, int delayTicks) {
        return new MagicEffect() {
            @Override
            public boolean canApply(World world, PlayerEntity user, List<Clarification> clarifications, List<MagicType> types) {
                return base.canApply(world, user, clarifications, types);
            }

            @Override
            public boolean apply(World world, PlayerEntity user, List<Clarification> clarifications, List<MagicType> types) {
                return base.apply(world, user, clarifications, types);
            }

            @Override
            public int getCastDelayTicks() {
                return delayTicks;
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
}