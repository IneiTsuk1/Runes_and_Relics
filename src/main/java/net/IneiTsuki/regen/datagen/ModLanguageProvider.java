package net.IneiTsuki.regen.datagen;

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

public class ModLanguageProvider extends FabricLanguageProvider {
    private final String languageCode;

    // Language configuration record for better organization
    private record LanguageConfig(
            Map<String, String> basicTranslations,
            Map<String, String> magicTranslations,
            Map<String, String> clarificationTranslations,
            String conjunction,
            boolean useCompoundWords
    ) {}

    // Centralized language configurations
    private static final Map<String, LanguageConfig> LANGUAGE_CONFIGS = Map.of(
            "en_us", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Spell Inscriber",
                            "staff_test", "Test Staff",
                            "empty_scroll", "Empty Magic Scroll",
                            "scroll_suffix", " Scroll",
                            "item_group", "Regen",
                            "staff_tooltip", "A test magical staff"
                    ),
                    Map.of(
                            "fire", "Fire", "water", "Water", "earth", "Earth", "air", "Air",
                            "lightning", "Lightning", "ice", "Ice", "shadow", "Shadow", "light", "Light"
                    ),
                    Map.of(
                            "control", "Control", "destruction", "Destruction", "construction", "Construction",
                            "much", "Much", "little", "Little", "some", "Some", "many", "Many",
                            "area", "Area", "move", "Move"
                    ),
                    " and ",
                    false
            ),
            "es_es", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Inscriptor de Hechizos",
                            "staff_test", "Bastón de Prueba",
                            "empty_scroll", "Pergamino Mágico Vacío",
                            "scroll_suffix", "",
                            "scroll_prefix", "Pergamino de ",
                            "item_group", "Regen",
                            "staff_tooltip", "Un bastón mágico de prueba"
                    ),
                    Map.of(
                            "fire", "Fuego", "water", "Agua", "earth", "Tierra", "air", "Aire",
                            "lightning", "Rayo", "ice", "Hielo", "shadow", "Sombra", "light", "Luz"
                    ),
                    Map.of(
                            "control", "Control", "destruction", "Destrucción", "construction", "Construcción",
                            "much", "Mucho", "little", "Poco", "some", "Algo", "many", "Muchos",
                            "area", "Área", "move", "Mover"
                    ),
                    " y ",
                    false
            ),
            "fr_fr", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Inscripteur de Sorts",
                            "staff_test", "Bâton de Test",
                            "empty_scroll", "Parchemin Magique Vide",
                            "scroll_suffix", "",
                            "scroll_prefix", "Parchemin de ",
                            "item_group", "Regen",
                            "staff_tooltip", "Un bâton magique de test"
                    ),
                    Map.of(
                            "fire", "Feu", "water", "Eau", "earth", "Terre", "air", "Air",
                            "lightning", "Foudre", "ice", "Glace", "shadow", "Ombre", "light", "Lumière"
                    ),
                    Map.of(
                            "control", "Contrôle", "destruction", "Destruction", "construction", "Construction",
                            "much", "Beaucoup", "little", "Peu", "some", "Quelque", "many", "Nombreux",
                            "area", "Zone", "move", "Déplacer"
                    ),
                    " et ",
                    false
            ),
            "de_de", new LanguageConfig(
                    Map.of(
                            "spell_inscriber", "Zaubereinschreiber",
                            "staff_test", "Teststab",
                            "empty_scroll", "Leere Zauberrolle",
                            "scroll_suffix", "rolle",
                            "item_group", "Regen",
                            "staff_tooltip", "Ein magischer Teststab"
                    ),
                    Map.of(
                            "fire", "Feuer", "water", "Wasser", "earth", "Erd", "air", "Luft",
                            "lightning", "Blitz", "ice", "Eis", "shadow", "Schatten", "light", "Licht"
                    ),
                    Map.of(
                            "control", "Kontrolle", "destruction", "Zerstörung", "construction", "Konstruktion",
                            "much", "Viel", "little", "Wenig", "some", "Etwas", "many", "Viele",
                            "area", "Bereich", "move", "Bewegen"
                    ),
                    "",
                    true
            )
    );

    public ModLanguageProvider(FabricDataOutput dataOutput, String languageCode, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, languageCode, registryLookup);
        this.languageCode = languageCode;
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        LanguageConfig config = LANGUAGE_CONFIGS.get(languageCode);

        if (config == null) {
            // Log warning or throw exception for unsupported language
            System.err.println("Warning: Unsupported language code: " + languageCode);
            return;
        }

        // Basic translations
        addBasicTranslations(translationBuilder, config.basicTranslations());

        // Magic scroll translations
        addScrollTranslations(translationBuilder, config);

        // Additional UI translations
        addUITranslations(translationBuilder, config.basicTranslations());
    }

    private void addBasicTranslations(TranslationBuilder builder, Map<String, String> translations) {
        builder.add(ModBlocks.SPELL_INSCRIBER_BLOCK, translations.get("spell_inscriber"));
        builder.add(ModItems.STAFF_TEST, translations.get("staff_test"));
    }

    private void addScrollTranslations(TranslationBuilder builder, LanguageConfig config) {
        for (MagicScrollItem scrollItem : MagicScrollItems.getAllScrolls().values()) {
            String scrollName = formatScrollName(scrollItem, config);
            builder.add(scrollItem, scrollName);
        }
    }

    private void addUITranslations(TranslationBuilder builder, Map<String, String> translations) {
        builder.add("itemGroup.regen.main", translations.get("item_group"));
        builder.add("tooltip.regen.staff_test", translations.get("staff_tooltip"));
    }

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

    private String joinClarifications(List<String> clarifications, LanguageConfig config) {
        if (clarifications.isEmpty()) return "";
        if (clarifications.size() == 1) return clarifications.getFirst();

        if (config.useCompoundWords()) {
            // German compound words
            return String.join("", clarifications).toLowerCase();
        } else {
            // Other languages with conjunctions
            String allButLast = String.join(", ", clarifications.subList(0, clarifications.size() - 1));
            String last = clarifications.getLast();
            return allButLast + config.conjunction() + last;
        }
    }

    private String buildScrollName(String translatedType, String clarificationPart, LanguageConfig config) {
        Map<String, String> translations = config.basicTranslations();
        String prefix = translations.getOrDefault("scroll_prefix", "");
        String suffix = translations.getOrDefault("scroll_suffix", " Scroll");

        if (config.useCompoundWords() && !clarificationPart.isEmpty()) {
            // German compound: "Feuerkontrollerolle"
            return prefix + translatedType + clarificationPart + suffix;
        } else if (!clarificationPart.isEmpty()) {
            // Handle different language patterns
            return switch (languageCode) {
                case "es_es", "fr_fr" ->
                    // Spanish/French: "Pergamino de Fuego de Control"
                        prefix + translatedType + " de " + clarificationPart + suffix;
                default ->
                    // English: "Fire Scroll of Control"
                        translatedType + suffix + " of " + clarificationPart;
            };
        } else {
            // Simple case without clarifications
            return switch (languageCode) {
                case "es_es", "fr_fr" ->
                    // Spanish/French: "Pergamino de Fuego"
                        prefix + translatedType + suffix;
                default ->
                    // English: "Fire Scroll"
                        translatedType + suffix;
            };
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // Factory methods with improved error handling
    public static ModLanguageProvider createProvider(String languageCode, FabricDataOutput dataOutput,
                                                     CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        if (!LANGUAGE_CONFIGS.containsKey(languageCode)) {
            throw new IllegalArgumentException("Unsupported language code: " + languageCode);
        }
        return new ModLanguageProvider(dataOutput, languageCode, registryLookup);
    }

    // Convenience factory methods
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

    // Utility method to get supported languages
    public static Set<String> getSupportedLanguages() {
        return LANGUAGE_CONFIGS.keySet();
    }
}