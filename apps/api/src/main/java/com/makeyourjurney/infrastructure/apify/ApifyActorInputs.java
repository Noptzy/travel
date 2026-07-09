package com.makeyourjurney.infrastructure.apify;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public final class ApifyActorInputs {

    private static final String TRAVELOKA_HOTEL_SCRAPER_ACTOR = "hotels-scrapers/traveloka-hotel-scraper";
    private static final DateTimeFormatter TRAVELOKA_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Map<String, TravelokaLocation> TRAVELOKA_LOCATIONS = Map.ofEntries(
            location("bali", "102746", "Bali"),
            location("bandung", "103859", "Bandung"),
            location("jakarta", "102813", "Jakarta"),
            location("semarang", "106587", "Semarang"),
            location("surabaya", "103570", "Surabaya"),
            location("yogyakarta", "107442", "Yogyakarta"),
            location("jogja", "107442", "Yogyakarta"),
            location("jogjakarta", "107442", "Yogyakarta"),
            location("chiang mai", "10000199", "Mueang Chiang Mai District"),
            location("thailand", "10000039", "Bangkok"),
            location("bangkok", "10000039", "Bangkok"),
            location("pattaya", "10000061", "Pattaya"),
            location("japan", "20004612", "Tokyo"),
            location("jepang", "20004612", "Tokyo"),
            location("tokyo", "20004612", "Tokyo"),
            location("osaka", "20003522", "Osaka"),
            location("south korea", "20003986", "Seoul"),
            location("korea", "20003986", "Seoul"),
            location("korea selatan", "20003986", "Seoul"),
            location("seoul", "20003986", "Seoul"),
            location("singapore", "107493", "Singapore"),
            location("singapura", "107493", "Singapore"),
            location("united states", "30016907", "New York City"),
            location("amerika", "30016907", "New York City"),
            location("amerika serikat", "30016907", "New York City"),
            location("usa", "30016907", "New York City"),
            location("new york", "30016907", "New York City")
    );

    private ApifyActorInputs() {
    }

    public static Map<String, Object> hotelSearchInput(String destination, LocalDate checkIn, LocalDate checkOut, int people, int maxItems) {
        return hotelSearchInput("", destination, checkIn, checkOut, people, maxItems);
    }

    public static Map<String, Object> hotelSearchInput(String actorId, String destination, LocalDate checkIn, LocalDate checkOut, int people, int maxItems) {
        String url = hotelSearchUrl(destination, checkIn, checkOut, people);
        if (TRAVELOKA_HOTEL_SCRAPER_ACTOR.equals(actorId)) {
            return Map.of(
                    "filterArgs", List.of(url),
                    "limitPerPage", maxItems,
                    "totalLimit", maxItems,
                    "proxyConfiguration", Map.of("useApifyProxy", false)
            );
        }
        return Map.of(
                "urls", List.of(url),
                "ignore_url_failures", true,
                "max_items_per_url", maxItems,
                "proxy", proxyConfig()
        );
    }

    public static Map<String, Object> activitySearchInput(String destination, int maxItems) {
        String url = "https://www.traveloka.com/en-en/activities/search?q=" + encode(destination + " tour") + "&sort=RELEVANCE";
        return Map.of(
                "urls", List.of(url),
                "ignore_url_failures", true,
                "max_items_per_url", maxItems,
                "page", 1,
                "sort_by", "Default",
                "proxy", proxyConfig()
        );
    }

    public static Map<String, Object> reviewInput(String sourceUrl, int maxReviews) {
        return Map.of(
                "urls", List.of(sourceUrl),
                "max_review_per_page", maxReviews,
                "sort_by", "SORT_HELPFUL_DESCENDING",
                "aggregate_ratings", true
        );
    }

    public static Map<String, Object> placesInput(String destination, String search, int maxItems) {
        return Map.of(
                "searchStringsArray", List.of(search),
                "locationQuery", destination,
                "maxCrawledPlacesPerSearch", maxItems,
                "language", "id",
                "maxReviews", 0,
                "maxImages", 1,
                "maximumLeadsEnrichmentRecords", 0,
                "scrapeSocialMediaProfiles", Map.of(
                        "facebooks", false, "instagrams", false, "youtubes", false,
                        "tiktoks", false, "twitters", false)
        );
    }

    private static Map<String, Object> proxyConfig() {
        return Map.of(
                "useApifyProxy", true,
                "apifyProxyGroups", List.of("RESIDENTIAL"),
                "apifyProxyCountry", "SG"
        );
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String hotelSearchUrl(String destination, LocalDate checkIn, LocalDate checkOut, int people) {
        TravelokaLocation location = resolveLocation(destination);
        String spec = "%s.%s.1.%d.HOTEL_GEO.%s.%s.2".formatted(
                checkIn.format(TRAVELOKA_DATE),
                checkOut.format(TRAVELOKA_DATE),
                people,
                location.geoId(),
                encode(location.label())
        );
        return "https://www.traveloka.com/en-id/hotel/search?spec=" + spec;
    }

    private static TravelokaLocation resolveLocation(String destination) {
        String key = destination.trim().toLowerCase(Locale.ROOT)
                .replace("-", " ")
                .replaceAll("\\s+", " ");
        return TRAVELOKA_LOCATIONS.getOrDefault(key, new TravelokaLocation("0", destination.trim()));
    }

    private static Map.Entry<String, TravelokaLocation> location(String key, String geoId, String label) {
        return new AbstractMap.SimpleImmutableEntry<>(key, new TravelokaLocation(geoId, label));
    }

    private record TravelokaLocation(String geoId, String label) {
    }

}
