package com.makeyourjurney.infrastructure.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.Review;
import com.makeyourjurney.domain.port.TravelDataPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "app.apify.enabled", havingValue = "false", matchIfMissing = true)
public class SeedTravelDataAdapter implements TravelDataPort {

    private final ObjectMapper objectMapper;

    public SeedTravelDataAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<HotelOption> searchHotels(String destination, LocalDate checkIn, LocalDate checkOut, int people, int limit) {
        List<HotelOption> hotels = readList("seed/hotels-" + slugify(destination) + ".json", HotelOption[].class);
        if (hotels.isEmpty()) {
            hotels = fallbackHotels(slugify(destination));
        }
        return hotels.stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<ActivityOption> searchActivities(String destination, int limit) {
        List<ActivityOption> activities = readList("seed/activities-" + slugify(destination) + ".json", ActivityOption[].class);
        if (activities.isEmpty()) {
            activities = fallbackActivities(slugify(destination));
        }
        return activities.stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<Review> fetchReviews(String sourceUrl, int limit) {
        return List.of();
    }

    private <T> List<T> readList(String classpathPath, Class<T[]> arrayType) {
        try (InputStream in = new ClassPathResource(classpathPath).getInputStream()) {
            T[] items = objectMapper.readValue(in, arrayType);
            return List.of(items);
        } catch (IOException e) {
            return List.of();
        }
    }

    private String slugify(String city) {
        return city.trim().toLowerCase(Locale.ROOT).replace(" ", "-");
    }

    private List<HotelOption> fallbackHotels(String city) {
        return switch (city) {
            case "bali" -> List.of(
                    hotel("bali-hotel-1", "Ubud Valley Boutique Resort", "Bali", "Ubud, Gianyar, Bali", 850000, 8.7, "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80"),
                    hotel("bali-hotel-2", "Seminyak Sunset Hotel", "Bali", "Seminyak, Badung, Bali", 620000, 8.4, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                    hotel("bali-hotel-3", "Nusa Dua Comfort Stay", "Bali", "Nusa Dua, Bali", 1250000, 9.1, "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80")
            );
            case "surabaya" -> List.of(
                    hotel("sby-hotel-1", "Tunjungan City Hotel", "Surabaya", "Jl. Tunjungan, Surabaya", 520000, 8.2, "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"),
                    hotel("sby-hotel-2", "Gubeng Business Stay", "Surabaya", "Gubeng, Surabaya", 430000, 7.9, "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80")
            );
            case "bandung" -> List.of(
                    hotel("bdg-hotel-1", "Braga Heritage Stay", "Bandung", "Braga, Bandung", 480000, 8.4, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                    hotel("bdg-hotel-2", "Dago Hills Hotel", "Bandung", "Dago, Bandung", 650000, 8.7, "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"),
                    hotel("bdg-hotel-3", "Lembang Scenic Resort", "Bandung", "Lembang, Bandung Barat", 920000, 9.0, "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80")
            );
            case "semarang" -> List.of(
                    hotel("smg-hotel-1", "Simpang Lima City Stay", "Semarang", "Simpang Lima, Semarang", 420000, 8.2, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                    hotel("smg-hotel-2", "Kota Lama Heritage Hotel", "Semarang", "Kota Lama, Semarang", 560000, 8.6, "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"),
                    hotel("smg-hotel-3", "Candisari Comfort Suites", "Semarang", "Candisari, Semarang", 720000, 8.9, "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80")
            );
            case "jepang", "japan", "tokyo" -> List.of(
                    hotel("jp-hotel-1", "Shinjuku City Hotel", "Tokyo", "Shinjuku, Tokyo, Japan", 1700000, 8.6, "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=1200&q=80"),
                    hotel("jp-hotel-2", "Asakusa Riverside Stay", "Tokyo", "Asakusa, Tokyo, Japan", 1350000, 8.3, "https://images.unsplash.com/photo-1557409518-691ebcd96038?auto=format&fit=crop&w=1200&q=80"),
                    hotel("jp-hotel-3", "Ginza Comfort Hotel", "Tokyo", "Ginza, Tokyo, Japan", 2200000, 9.0, "https://images.unsplash.com/photo-1512692723619-8b3e68365c9c?auto=format&fit=crop&w=1200&q=80")
            );
            case "korea", "korea-selatan", "south-korea", "seoul" -> List.of(
                    hotel("kr-hotel-1", "Hongdae Urban Stay", "Seoul", "Hongdae, Seoul, South Korea", 1200000, 8.4, "https://images.unsplash.com/photo-1538485399081-7191377e8241?auto=format&fit=crop&w=1200&q=80"),
                    hotel("kr-hotel-2", "Myeongdong Central Hotel", "Seoul", "Myeongdong, Seoul, South Korea", 1550000, 8.7, "https://images.unsplash.com/photo-1506816561089-5cc37b3aa9b0?auto=format&fit=crop&w=1200&q=80"),
                    hotel("kr-hotel-3", "Gangnam Premium Stay", "Seoul", "Gangnam, Seoul, South Korea", 2100000, 9.1, "https://images.unsplash.com/photo-1590490359683-658d3d23f972?auto=format&fit=crop&w=1200&q=80")
            );
            default -> genericHotels(city);
        };
    }

    private List<ActivityOption> fallbackActivities(String city) {
        return switch (city) {
            case "bali" -> List.of(
                    activity("bali-act-1", "Tanah Lot Sunset Tour", "Bali", "Tanah Lot, Tabanan, Bali", 30000, 8.8, 2, "https://images.unsplash.com/photo-1537953773345-d172ccf13cf1?auto=format&fit=crop&w=1200&q=80", List.of("alam", "budaya", "sunset")),
                    activity("bali-act-2", "Ubud Monkey Forest", "Bali", "Jl. Monkey Forest, Ubud, Bali", 80000, 8.1, 1.5, "https://images.unsplash.com/photo-1518548419970-58e3b4079ab2?auto=format&fit=crop&w=1200&q=80", List.of("alam", "keluarga")),
                    activity("bali-act-3", "Mount Batur Sunrise Trekking", "Bali", "Kintamani, Bangli, Bali", 350000, 9.0, 6, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", List.of("adventure", "alam", "healing")),
                    activity("bali-act-4", "Waterbom Bali", "Bali", "Jl. Kartika Plaza, Kuta, Bali", 450000, 8.9, 4, "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=1200&q=80", List.of("keluarga", "adventure", "waterpark"))
            );
            case "surabaya" -> List.of(
                    activity("sby-act-1", "House of Sampoerna Tour", "Surabaya", "Jl. Taman Sampoerna No. 6, Surabaya", 0, 8.2, 1.5, "https://images.unsplash.com/photo-1565967511849-76a60a516170?auto=format&fit=crop&w=1200&q=80", List.of("budaya", "museum", "gratis")),
                    activity("sby-act-2", "Surabaya Zoo", "Surabaya", "Jl. Setail No. 1, Surabaya", 50000, 7.4, 3, "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=1200&q=80", List.of("keluarga", "alam"))
            );
            case "bandung" -> List.of(
                    activity("bdg-act-1", "Braga Street Walk", "Bandung", "Jl. Braga, Bandung", 0, 8.5, 2, "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=1200&q=80", List.of("heritage", "jalan", "foto")),
                    activity("bdg-act-2", "Tangkuban Perahu Trip", "Bandung", "Lembang, Bandung Barat", 250000, 8.8, 4, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", List.of("alam", "gunung", "keluarga")),
                    activity("bdg-act-3", "Kawah Putih Visit", "Bandung", "Ciwidey, Bandung", 150000, 8.7, 3, "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80", List.of("alam", "foto", "healing")),
                    activity("bdg-act-4", "Bandung Culinary Night", "Bandung", "Cibadak, Bandung", 120000, 8.6, 2, "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&w=1200&q=80", List.of("kuliner", "malam", "lokal"))
            );
            case "semarang" -> List.of(
                    activity("smg-act-1", "Lawang Sewu Heritage Tour", "Semarang", "Jl. Pemuda, Semarang", 30000, 8.7, 2, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", List.of("sejarah", "budaya", "foto")),
                    activity("smg-act-2", "Kota Lama Walk", "Semarang", "Kota Lama, Semarang", 0, 8.5, 2, "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=1200&q=80", List.of("heritage", "jalan", "foto")),
                    activity("smg-act-3", "Sam Poo Kong Visit", "Semarang", "Gedung Batu, Semarang", 35000, 8.4, 1.5, "https://images.unsplash.com/photo-1526481280693-3bfa7568e0f3?auto=format&fit=crop&w=1200&q=80", List.of("budaya", "sejarah", "keluarga")),
                    activity("smg-act-4", "Semarang Culinary Night", "Semarang", "Pecinan, Semarang", 120000, 8.6, 2, "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&w=1200&q=80", List.of("kuliner", "malam", "lokal"))
            );
            case "jepang", "japan", "tokyo" -> List.of(
                    activity("jp-act-1", "Sensoji Temple Walk", "Tokyo", "Asakusa, Tokyo, Japan", 0, 8.8, 2, "https://images.unsplash.com/photo-1526481280693-3bfa7568e0f3?auto=format&fit=crop&w=1200&q=80", List.of("budaya", "temple", "jalan")),
                    activity("jp-act-2", "Shibuya Crossing", "Tokyo", "Shibuya, Tokyo, Japan", 0, 8.5, 1.5, "https://images.unsplash.com/photo-1542051841857-5f90071e7989?auto=format&fit=crop&w=1200&q=80", List.of("kota", "foto", "belanja")),
                    activity("jp-act-3", "Tokyo Skytree", "Tokyo", "Sumida, Tokyo, Japan", 260000, 8.7, 2, "https://images.unsplash.com/photo-1536098561742-ca998e48cbcc?auto=format&fit=crop&w=1200&q=80", List.of("landmark", "view", "keluarga")),
                    activity("jp-act-4", "Ueno Park", "Tokyo", "Ueno, Tokyo, Japan", 0, 8.4, 2.5, "https://images.unsplash.com/photo-1522383225653-ed111181a951?auto=format&fit=crop&w=1200&q=80", List.of("taman", "alam", "museum"))
            );
            case "korea", "korea-selatan", "south-korea", "seoul" -> List.of(
                    activity("kr-act-1", "Gyeongbokgung Palace", "Seoul", "Jongno-gu, Seoul, South Korea", 40000, 8.9, 2.5, "https://images.unsplash.com/photo-1534274867514-d5b47ef89ed7?auto=format&fit=crop&w=1200&q=80", List.of("budaya", "palace", "sejarah")),
                    activity("kr-act-2", "N Seoul Tower", "Seoul", "Namsan, Seoul, South Korea", 180000, 8.6, 2, "https://images.unsplash.com/photo-1517154421773-0529f29ea451?auto=format&fit=crop&w=1200&q=80", List.of("landmark", "view", "foto")),
                    activity("kr-act-3", "Bukchon Hanok Village", "Seoul", "Jongno-gu, Seoul, South Korea", 0, 8.5, 2, "https://images.unsplash.com/photo-1548115184-bc6544d06a58?auto=format&fit=crop&w=1200&q=80", List.of("budaya", "jalan", "foto")),
                    activity("kr-act-4", "Myeongdong Street Food", "Seoul", "Myeongdong, Seoul, South Korea", 120000, 8.4, 2, "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&w=1200&q=80", List.of("kuliner", "belanja", "malam"))
            );
            default -> genericActivities(city);
        };
    }

    private List<HotelOption> genericHotels(String city) {
        String displayCity = displayCity(city);
        String prefix = city.replace("-", "");
        return List.of(
                hotel(prefix + "-hotel-1", displayCity + " Central Stay", displayCity, "Pusat kota " + displayCity, 550000, 8.2, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"),
                hotel(prefix + "-hotel-2", displayCity + " Comfort Hotel", displayCity, "Area wisata " + displayCity, 760000, 8.6, "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80"),
                hotel(prefix + "-hotel-3", displayCity + " Premium Suites", displayCity, "Distrik utama " + displayCity, 1100000, 8.9, "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80")
        );
    }

    private List<ActivityOption> genericActivities(String city) {
        String displayCity = displayCity(city);
        String prefix = city.replace("-", "");
        return List.of(
                activity(prefix + "-act-1", displayCity + " City Walk", displayCity, "Pusat kota " + displayCity, 0, 8.3, 2, "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=1200&q=80", List.of("jalan", "foto", "kota")),
                activity(prefix + "-act-2", displayCity + " Landmark Visit", displayCity, "Landmark utama " + displayCity, 120000, 8.5, 2, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80", List.of("landmark", "budaya", "keluarga")),
                activity(prefix + "-act-3", displayCity + " Local Food Tour", displayCity, "Area kuliner " + displayCity, 180000, 8.6, 2.5, "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&w=1200&q=80", List.of("kuliner", "lokal", "malam")),
                activity(prefix + "-act-4", displayCity + " Scenic Spot", displayCity, "Spot populer " + displayCity, 90000, 8.4, 2, "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80", List.of("alam", "foto", "healing"))
        );
    }

    private String displayCity(String city) {
        String[] parts = city.replace("-", " ").split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return builder.isEmpty() ? "Tujuan" : builder.toString();
    }

    private HotelOption hotel(String id, String name, String city, String address, long price, double rating, String imageUrl) {
        return new HotelOption(id, name, city, address, BigDecimal.valueOf(price), "IDR", BigDecimal.valueOf(rating), 1000, imageUrl, "seed", "https://www.google.com/maps/search/?api=1&query=" + name, null);
    }

    private ActivityOption activity(String id, String name, String city, String address, long price, double rating, double durationHours, String imageUrl, List<String> tags) {
        return new ActivityOption(id, name, city, address, BigDecimal.valueOf(price), "IDR", BigDecimal.valueOf(rating), 1000, imageUrl, "seed", "https://www.google.com/maps/search/?api=1&query=" + name, BigDecimal.valueOf(durationHours), tags, null);
    }
}
