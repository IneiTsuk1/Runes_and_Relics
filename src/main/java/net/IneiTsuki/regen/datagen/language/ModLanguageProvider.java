package net.IneiTsuki.regen.datagen.language;

import net.IneiTsuki.regen.block.ModBlocks;
import net.IneiTsuki.regen.item.ModItems;
import net.IneiTsuki.regen.magic.item.MagicScrollItem;
import net.IneiTsuki.regen.magic.item.MagicScrollItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A Fabric data provider for generating language files for multiple locales.
 *
 * <p>This class dynamically generates language translations for items, blocks, and tooltips,
 * including support for compound naming logic for magic scrolls based on types and clarifications.
 */
public class ModLanguageProvider extends FabricLanguageProvider {
    private final String languageCode;

    /**
     * Configuration for a specific language including translations and formatting preferences.
     *
     * @param basicTranslations         Static translations for core items, tooltips, and group names.
     * @param magicTranslations         Translation mappings for magic types (e.g. fire, water).
     * @param clarificationTranslations Translation mappings for clarifications (e.g. control, destruction).
     * @param conjunction               The word used to join multiple clarifications (e.g. "and").
     * @param useCompoundWords          Whether to concatenate words (used in German).
     */
    private record LanguageConfig(
            Map<String, String> basicTranslations,
            Map<String, String> magicTranslations,
            Map<String, String> clarificationTranslations,
            String conjunction,
            boolean useCompoundWords
    ) {}

    private static final Map<String, LanguageConfig> LANGUAGE_CONFIGS = Map.of(
            "en_us", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Spell Inscriber",
                            "staff_test", "Test Staff",
                            "item_group", "Regen",
                            "staff_tooltip", "Used to focus your magic",
                            "empty_scroll", "Blank Scroll",
                            "scroll_prefix", "",
                            "scroll_suffix", " Scroll"
                    ),
                    Map.of(
                            "fire", "Fire",
                            "water", "Water",
                            "ice", "Ice",
                            "air", "Air",
                            "earth",  "Earth",
                            "life", "Life",
                            "light", "Light",
                            "dark", "Dark",
                            "target", "Target"
                    ),
                    Map.of(
                            "control", "Control",
                            "destruction", "Destruction",
                            "construction", "Construction",
                            "much", "Much",
                            "many", "Many",
                            "area", "Area",
                            "some", "Some",
                            "little", "Little",
                            "move", "Move"
                    ),
                    " and ",
                    false // en_us doesn't use compound words
            ),

            "es_es", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Inscriptor de Hechizos",
                            "staff_test", "Bastón de Prueba",
                            "item_group", "Regen",
                            "staff_tooltip", "Usado para enfocar tu magia",
                            "empty_scroll", "Pergamino en Blanco",
                            "scroll_prefix", "Pergamino de ",
                            "scroll_suffix", ""
                    ),
                    Map.of(
                            "fire", "Fuego",
                            "water", "Agua",
                            "ice", "Hielo",
                            "air", "Aire",
                            "earth", "Tierra",
                            "life", "Vida",
                            "light", "Luz",
                            "dark", "Oscuridad",
                            "target", "Objetivo"
                    ),
                    Map.of(
                            "control", "Control",
                            "destruction", "Destrucción",
                            "construction", "Construcción",
                            "much", "Mucho",
                            "many", "Muchos",
                            "area", "Área",
                            "some", "Algo",
                            "little", "Poco",
                            "move", "Mover"
                    ),
                    " y ",
                    false // Spanish uses conjunctions
            ),

            "fr_fr", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Inscripteur de Sorts",
                            "staff_test", "Bâton de Test",
                            "item_group", "Regen",
                            "staff_tooltip", "Utilisé pour concentrer votre magie",
                            "empty_scroll", "Parchemin Vierge",
                            "scroll_prefix", "Parchemin de ",
                            "scroll_suffix", ""
                    ),
                    Map.of(
                            "fire", "Feu",
                            "water", "Eau",
                            "ice", "Glace",
                            "air", "Air",
                            "earth", "Terre",
                            "life", "Vie",
                            "light", "Lumière",
                            "dark", "Ténèbres",
                            "target", "Cible"
                    ),
                    Map.of(
                            "control", "Contrôle",
                            "destruction", "Destruction",
                            "construction", "Construction",
                            "much", "Beaucoup",
                            "many", "Nombreux",
                            "area", "Zone",
                            "some", "Quelque",
                            "little", "Peu",
                            "move", "Déplacer"
                    ),
                    " et ",
                    false // French uses conjunctions
            ),

            "de_de", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Zaubereinschreiber",
                            "staff_test", "Teststab",
                            "item_group", "Regen",
                            "staff_tooltip", "Wird verwendet, um deine Magie zu fokussieren",
                            "empty_scroll", "Leere Schriftrolle",
                            "scroll_prefix", "",
                            "scroll_suffix", "schriftrolle"
                    ),
                    Map.of(
                            "fire", "Feuer",
                            "water", "Wasser",
                            "ice", "Eis",
                            "air", "Luft",
                            "earth", "Erde",
                            "life", "Leben",
                            "light", "Licht",
                            "dark", "Dunkel",
                            "target", "Ziel"
                    ),
                    Map.of(
                            "control", "kontrolle",
                            "destruction", "zerstörung",
                            "construction", "konstruktion",
                            "much", "viel",
                            "many", "viele",
                            "area", "bereich",
                            "some", "etwas",
                            "little", "wenig",
                            "move", "bewegen"
                    ),
                    "",
                    true // German uses compound words
            )
    );

    /**
     * Constructs a new ModLanguageProvider for a given language.
     *
     * @param dataOutput     The Fabric data output instance.
     * @param languageCode   The locale code (e.g., "en_us").
     * @param registryLookup A future-wrapped registry context.
     */
    public ModLanguageProvider(FabricDataOutput dataOutput, String languageCode, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, languageCode, registryLookup);
        this.languageCode = languageCode;
    }

    /**
     * Main translation generation entrypoint.
     * Adds basic, scroll, and UI translations.
     *
     * @param registryLookup       The registry context used for resolving items/blocks.
     * @param translationBuilder   The translation builder used to register keys and values.
     */
    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        LanguageConfig config = LANGUAGE_CONFIGS.get(languageCode);

        if (config == null) {
            System.err.println("Warning: Unsupported language code: " + languageCode);
            return;
        }

        addBasicTranslations(translationBuilder, config.basicTranslations());
        addScrollTranslations(translationBuilder, config);
        addUITranslations(translationBuilder, config.basicTranslations());
    }

    /**
     * Adds translations for simple items and blocks.
     */
    private void addBasicTranslations(TranslationBuilder builder, Map<String, String> translations) {
        builder.add(ModBlocks.SPELL_INSCRIBER_BLOCK, translations.get("spell_inscriber"));
        builder.add(ModItems.STAFF_TEST, translations.get("staff_test"));
    }

    /**
     * Adds translations for all registered magic scroll items.
     */
    private void addScrollTranslations(TranslationBuilder builder, LanguageConfig config) {
        for (MagicScrollItem scrollItem : MagicScrollItems.getAllScrolls().values()) {
            String scrollName = formatScrollName(scrollItem, config);
            builder.add(scrollItem, scrollName);
        }
    }

    /**
     * Adds translations for UI elements and tooltips.
     */
    private void addUITranslations(TranslationBuilder builder, Map<String, String> translations) {
        builder.add("itemGroup.regen.main", translations.get("item_group"));
        builder.add("tooltip.regen.staff_test", translations.get("staff_tooltip"));
    }

    /**
     * Generates the final localized scroll name using type and clarification(s).
     */
    private String formatScrollName(MagicScrollItem scrollItem, LanguageConfig config) {
        if (scrollItem.getMagicTypes().isEmpty()) {
            return config.basicTranslations().get("empty_scroll");
        }

        String magicType = scrollItem.getMagicTypes().getFirst().getName().toLowerCase();
        String translatedType = config.magicTranslations().getOrDefault(magicType,
                capitalize(magicType.replace("_", " ")));

        String clarificationPart = formatClarifications(scrollItem, config);

        return buildScrollName(translatedType, clarificationPart, config);
    }

    /**
     * Converts a scroll's clarification list into a localized and formatted string.
     */
    private String formatClarifications(MagicScrollItem scrollItem, LanguageConfig config) {
        if (scrollItem.getClarifications().isEmpty()) {
            return "";
        }

        List<String> translatedClarifications = scrollItem.getClarifications().stream()
                .map(clarification -> {
                    String key = clarification.getName().toLowerCase();
                    return config.clarificationTranslations().getOrDefault(key, capitalize(key));
                })
                .toList();

        return joinClarifications(translatedClarifications, config);
    }

    /**
     * Joins multiple clarifications with the language-specific conjunction or compound form.
     */
    private String joinClarifications(List<String> clarifications, LanguageConfig config) {
        if (clarifications.isEmpty()) return "";
        if (clarifications.size() == 1) return clarifications.getFirst();

        if (config.useCompoundWords()) {
            return String.join("", clarifications).toLowerCase();
        } else {
            String allButLast = String.join(", ", clarifications.subList(0, clarifications.size() - 1));
            String last = clarifications.getLast();
            return allButLast + config.conjunction() + last;
        }
    }

    /**
     * Combines translated type and clarifications into a full item name.
     */
    private String buildScrollName(String translatedType, String clarificationPart, LanguageConfig config) {
        Map<String, String> translations = config.basicTranslations();
        String prefix = translations.getOrDefault("scroll_prefix", "");
        String suffix = translations.getOrDefault("scroll_suffix", " Scroll");

        if (config.useCompoundWords() && !clarificationPart.isEmpty()) {
            return prefix + translatedType + clarificationPart + suffix;
        } else if (!clarificationPart.isEmpty()) {
            return switch (languageCode) {
                case "es_es", "fr_fr" ->
                        prefix + translatedType + " de " + clarificationPart + suffix;
                default ->
                        translatedType + suffix + " of " + clarificationPart;
            };
        } else {
            return switch (languageCode) {
                case "es_es", "fr_fr" ->
                        prefix + translatedType + suffix;
                default ->
                        translatedType + suffix;
            };
        }
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // --------------------
    // Factory Methods
    // --------------------

    /**
     * Creates a ModLanguageProvider for a given language, validating the language code.
     *
     * @throws IllegalArgumentException if the language is unsupported.
     */
    public static ModLanguageProvider createProvider(String languageCode, FabricDataOutput dataOutput,
                                                     CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        if (!LANGUAGE_CONFIGS.containsKey(languageCode)) {
            throw new IllegalArgumentException("Unsupported language code: " + languageCode);
        }
        return new ModLanguageProvider(dataOutput, languageCode, registryLookup);
    }

    // Convenience methods for creating providers for supported languages
    public static ModLanguageProvider english(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        return createProvider("en_us", dataOutput, registryLookup);
    }

    public static ModLanguageProvider spanish(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        return createProvider("es_es", dataOutput, registryLookup);
    }

    public static ModLanguageProvider french(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        return createProvider("fr_fr", dataOutput, registryLookup);
    }

    public static ModLanguageProvider german(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        return createProvider("de_de", dataOutput, registryLookup);
    }

    /**
     * Returns the set of supported language codes.
     */
    public static Set<String> getSupportedLanguages() {
        return LANGUAGE_CONFIGS.keySet();
    }
}
