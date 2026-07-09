package com.makeyourjurney.infrastructure.ai;

import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;
import com.makeyourjurney.domain.model.TripIntent;
import com.makeyourjurney.domain.port.AiPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class RuleBasedAiAdapter implements AiPort {

    private static final Pattern ORIGIN = Pattern.compile("(?i)\\bdari\\b\\s+([A-Za-z]+)");
    private static final Pattern DESTINATION = Pattern.compile("(?i)\\bke\\b\\s+([A-Za-z]+)");
    private static final Pattern DESTINATION_FALLBACK = Pattern.compile("(?i)\\bmenuju\\b\\s+([A-Za-z]+)");
    private static final Pattern BUDGET = Pattern.compile("(?i)budget\\s*(?:rp\\.?)?\\s*([\\d.,]+)\\s*(juta|rb|ribu)?");
    private static final Pattern DAYS = Pattern.compile("(\\d+)\\s*hari");
    private static final Pattern NIGHTS = Pattern.compile("(\\d+)\\s*malam");
    private static final Pattern PEOPLE = Pattern.compile("(\\d+)\\s*orang");

    private static final Map<String, TripStyle> TRIP_STYLE_KEYWORDS = new LinkedHashMap<>();
    private static final Map<String, TravelMode> TRAVEL_MODE_KEYWORDS = new LinkedHashMap<>();
    private static final List<String> PREFERENCE_KEYWORDS = List.of(
            "hemat", "nyaman", "mewah", "romantis", "kuliner", "pantai", "gunung", "budaya", "belanja", "keluarga"
    );

    static {
        TRIP_STYLE_KEYWORDS.put("hemat", TripStyle.HEMAT);
        TRIP_STYLE_KEYWORDS.put("murah", TripStyle.HEMAT);
        TRIP_STYLE_KEYWORDS.put("nyaman", TripStyle.NYAMAN);
        TRIP_STYLE_KEYWORDS.put("mewah", TripStyle.NYAMAN);
        TRIP_STYLE_KEYWORDS.put("keluarga", TripStyle.FAMILY);
        TRIP_STYLE_KEYWORDS.put("pasangan", TripStyle.COUPLE);
        TRIP_STYLE_KEYWORDS.put("sendiri", TripStyle.SOLO);
        TRIP_STYLE_KEYWORDS.put("healing", TripStyle.HEALING);
        TRIP_STYLE_KEYWORDS.put("petualangan", TripStyle.ADVENTURE);
        TRIP_STYLE_KEYWORDS.put("adventure", TripStyle.ADVENTURE);

        TRAVEL_MODE_KEYWORDS.put("mobil", TravelMode.CAR);
        TRAVEL_MODE_KEYWORDS.put("motor", TravelMode.MOTOR);
        TRAVEL_MODE_KEYWORDS.put("jalan kaki", TravelMode.WALK);
        TRAVEL_MODE_KEYWORDS.put("sepeda", TravelMode.BIKE);
        TRAVEL_MODE_KEYWORDS.put("kereta", TravelMode.PUBLIC);
        TRAVEL_MODE_KEYWORDS.put("bus", TravelMode.PUBLIC);
        TRAVEL_MODE_KEYWORDS.put("transportasi umum", TravelMode.PUBLIC);
    }

    @Override
    public TripIntent parseIntent(String message, TripIntent previousIntent) {
        String lower = message.toLowerCase(Locale.ROOT);

        TripIntent parsed = new TripIntent(
                find(ORIGIN, message),
                findDestination(message),
                null, null,
                findInt(DAYS, message),
                findInt(NIGHTS, message),
                findInt(PEOPLE, message),
                findBudget(message),
                findKeyword(lower, TRIP_STYLE_KEYWORDS),
                findKeyword(lower, TRAVEL_MODE_KEYWORDS),
                findPreferences(lower),
                List.of(),
                false
        );

        return TripIntentMerger.merge(parsed, previousIntent);
    }

    private String findDestination(String message) {
        String value = find(DESTINATION, message);
        return value != null ? value : find(DESTINATION_FALLBACK, message);
    }

    private String find(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? capitalize(matcher.group(1)) : null;
    }

    private Integer findInt(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private BigDecimal findBudget(String message) {
        Matcher matcher = BUDGET.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        BigDecimal amount = new BigDecimal(matcher.group(1).replaceAll("[.,]", ""));
        String unit = matcher.group(2);
        if (unit == null) {
            return amount;
        }
        return switch (unit.toLowerCase(Locale.ROOT)) {
            case "juta" -> amount.multiply(BigDecimal.valueOf(1_000_000));
            case "rb", "ribu" -> amount.multiply(BigDecimal.valueOf(1_000));
            default -> amount;
        };
    }

    private <T> T findKeyword(String lowerMessage, Map<String, T> keywords) {
        for (Map.Entry<String, T> entry : keywords.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<String> findPreferences(String lowerMessage) {
        List<String> preferences = new ArrayList<>();
        for (String keyword : PREFERENCE_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                preferences.add(keyword);
            }
        }
        return preferences;
    }

    private String capitalize(String word) {
        return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1).toLowerCase(Locale.ROOT);
    }
}
