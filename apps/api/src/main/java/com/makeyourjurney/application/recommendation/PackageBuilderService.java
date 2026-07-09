package com.makeyourjurney.application.recommendation;

import com.makeyourjurney.application.budget.BudgetCalculationService;
import com.makeyourjurney.domain.enums.PackageType;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.BudgetSummary;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.TripPackage;
import com.makeyourjurney.domain.model.TransportOption;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class PackageBuilderService {

    private final BudgetCalculationService budgetCalculationService;

    public PackageBuilderService(BudgetCalculationService budgetCalculationService) {
        this.budgetCalculationService = budgetCalculationService;
    }

    public List<TripPackage> buildPackages(
            List<HotelOption> scoredHotels,
            List<ActivityOption> scoredActivities,
            BigDecimal totalBudget,
            int people,
            int nights,
            int days,
            double routeDistanceKm,
            TravelMode travelMode,
            boolean international
    ) {
        List<HotelOption> hotelsByQualityDesc = scoredHotels.stream().sorted(qualityDesc()).toList();
        List<HotelOption> hotelsByQualityAsc = scoredHotels.stream().sorted(qualityAsc()).toList();
        List<HotelOption> knownPriceHotelsByPrice = scoredHotels.stream()
                .filter(hotel -> hotel.pricePerNight().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(HotelOption::pricePerNight))
                .toList();
        List<ActivityOption> activitiesByScoreDesc = scoredActivities.stream()
                .sorted(Comparator.comparing(ActivityOption::score, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        List<ActivityOption> activitiesForHemat = scoredActivities.stream()
                .sorted(Comparator.comparing(ActivityOption::pricePerPerson)
                        .thenComparing(ActivityOption::rating))
                .toList();

        HotelOption hematHotel = !knownPriceHotelsByPrice.isEmpty()
                ? knownPriceHotelsByPrice.get(0)
                : hotelsByQualityAsc.stream().findFirst().orElse(null);
        HotelOption balancedHotel = !knownPriceHotelsByPrice.isEmpty()
                ? knownPriceHotelsByPrice.get(knownPriceHotelsByPrice.size() / 2)
                : (hotelsByQualityAsc.isEmpty() ? null : hotelsByQualityAsc.get(hotelsByQualityAsc.size() / 2));
        HotelOption nyamanHotel = hotelsByQualityDesc.isEmpty() ? null : hotelsByQualityDesc.get(0);

        List<ActivityOption> hematActivities = activitiesForHemat.stream().limit(8).toList();
        List<ActivityOption> balancedActivities = activitiesByScoreDesc.stream().limit(14).toList();
        List<ActivityOption> nyamanActivities = activitiesByScoreDesc.stream().limit(20).toList();

        TripPackage hemat = buildPackage(PackageType.HEMAT, hematHotel, hematActivities,
                totalBudget, people, nights, days, routeDistanceKm, travelMode, international,
                "Hotel termurah dan aktivitas hemat agar total tetap jauh di bawah budget.");

        TripPackage balanced = buildPackage(PackageType.BALANCED, balancedHotel, balancedActivities,
                totalBudget, people, nights, days, routeDistanceKm, travelMode, international,
                "Kombinasi harga, rating, dan aktivitas paling seimbang.");

        TripPackage nyaman = buildPackage(PackageType.NYAMAN, nyamanHotel, nyamanActivities,
                totalBudget, people, nights, days, routeDistanceKm, travelMode, international,
                "Hotel rating tertinggi dan aktivitas terbanyak untuk kenyamanan maksimal.");

        return List.of(hemat, balanced, nyaman);
    }

    private TripPackage buildPackage(
            PackageType type,
            HotelOption hotel,
            List<ActivityOption> activities,
            BigDecimal totalBudget,
            int people,
            int nights,
            int days,
            double routeDistanceKm,
            TravelMode travelMode,
            boolean international,
            String reason
    ) {
        List<ActivityOption> pricedActivities = activities.stream()
                .map(activity -> withEstimatedActivityPrice(activity, type, totalBudget, people, days))
                .toList();
        BigDecimal hotelPricePerNight = hotel == null ? BigDecimal.ZERO : hotel.pricePerNight();
        List<BigDecimal> activityPrices = pricedActivities.stream().map(ActivityOption::pricePerPerson).toList();
        List<TransportOption> transportOptions = transportOptions(international, routeDistanceKm, people, travelMode);
        TransportOption transport = transportOptions.get(switch (type) { case HEMAT -> 0; case BALANCED -> 1; case NYAMAN -> 2; });

        BudgetSummary summary = budgetCalculationService.calculateWithTransportCost(
                totalBudget, people, nights, days, hotelPricePerNight, activityPrices, transport.estimatedCost()
        );

        return new TripPackage(
                type,
                summary.estimatedTotal(),
                summary.status(),
                hotel == null ? List.of() : List.of(hotel),
                pricedActivities,
                transport,
                transportOptions,
                summary,
                reason
        );
    }

    private Comparator<HotelOption> qualityDesc() {
        return Comparator.comparing(HotelOption::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(HotelOption::score, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(HotelOption::reviewCount, Comparator.reverseOrder());
    }

    private Comparator<HotelOption> qualityAsc() {
        return Comparator.comparing(HotelOption::rating, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(HotelOption::score, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(HotelOption::reviewCount);
    }

    private ActivityOption withEstimatedActivityPrice(ActivityOption activity, PackageType type, BigDecimal budget, int people, int days) {
        if (activity.pricePerPerson().compareTo(BigDecimal.ZERO) > 0) return activity;
        BigDecimal share = switch (type) {
            case HEMAT -> BigDecimal.valueOf(0.015);
            case BALANCED -> BigDecimal.valueOf(0.025);
            case NYAMAN -> BigDecimal.valueOf(0.04);
        };
        BigDecimal price = budget.multiply(share)
                .divide(BigDecimal.valueOf((long) Math.max(people, 1) * Math.max(days, 1)), 0, java.math.RoundingMode.HALF_UP);
        return new ActivityOption(activity.externalId(), activity.name(), activity.city(), activity.address(), price,
                "IDR", activity.rating(), activity.reviewCount(), activity.imageUrl(), activity.source(), activity.sourceUrl(),
                activity.durationHours(), activity.tags(), activity.score());
    }

    private List<TransportOption> transportOptions(boolean international, double distanceKm, int people, TravelMode requestedMode) {
        if (international) {
            return List.of(
                    flight("AirAsia / Scoot", "Ekonomi hemat", Math.max(1_500_000, distanceKm * 520), people),
                    flight("Garuda Indonesia", "Ekonomi full-service", Math.max(2_250_000, distanceKm * 720), people),
                    flight("Singapore Airlines / ANA", "Ekonomi premium", Math.max(3_250_000, distanceKm * 980), people)
            );
        }

        if (distanceKm >= 250) {
            return List.of(
                    new TransportOption("BUS", "Bus antarkota", "Kelas ekonomi", "Opsi darat paling hemat.", BigDecimal.valueOf(Math.max(250_000, distanceKm * 450) * people)),
                    new TransportOption("KERETA", "KAI", "Kelas ekonomi premium", "Nyaman untuk perjalanan antarkota.", BigDecimal.valueOf(Math.max(400_000, distanceKm * 700) * people)),
                    new TransportOption("PESAWAT", "Maskapai domestik", "Ekonomi full-service", "Opsi tercepat untuk jarak jauh.", BigDecimal.valueOf(Math.max(900_000, distanceKm * 1_100) * people))
            );
        }

        return List.of(
                new TransportOption("MOTOR", "Motor", "Mandiri", "BBM dan tol/parkir estimasi rute.", BigDecimal.valueOf(distanceKm * 700).setScale(0, java.math.RoundingMode.HALF_UP)),
                new TransportOption("MOBIL", "Mobil", "Sewa / pribadi", "BBM dan tol estimasi rute.", BigDecimal.valueOf(distanceKm * 2_000).setScale(0, java.math.RoundingMode.HALF_UP)),
                new TransportOption("TRANSPORT_UMUM", "Kereta / travel", "Tiket pulang-pergi", "Harga estimasi per penumpang.", BigDecimal.valueOf(Math.max(120_000, distanceKm * 850) * people).setScale(0, java.math.RoundingMode.HALF_UP))
        );
    }

    private TransportOption flight(String provider, String service, double perPerson, int people) {
        return new TransportOption("PESAWAT", provider, service,
                "Penerbangan pulang-pergi; harga estimasi dapat berubah.",
                BigDecimal.valueOf(perPerson * Math.max(people, 1)).setScale(0, java.math.RoundingMode.HALF_UP));
    }
}
