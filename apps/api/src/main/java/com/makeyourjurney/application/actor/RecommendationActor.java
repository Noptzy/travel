package com.makeyourjurney.application.actor;

import com.makeyourjurney.application.recommendation.ActivityScoringService;
import com.makeyourjurney.application.recommendation.HotelScoringService;
import com.makeyourjurney.application.recommendation.PackageBuilderService;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.TripPackage;
import com.makeyourjurney.domain.model.TripRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class RecommendationActor implements Actor<RecommendationActor.Input, RecommendationActor.Output> {

    public record Input(TripRequest request, List<HotelOption> hotels, List<ActivityOption> activities,
                        double routeDistanceKm, boolean internationalRoute) {
    }

    public record Output(List<HotelOption> scoredHotels, List<ActivityOption> scoredActivities, List<TripPackage> packages) {
    }

    private static final BigDecimal HOTEL_BUDGET_SHARE = BigDecimal.valueOf(0.35);
    private static final BigDecimal ACTIVITY_BUDGET_SHARE = BigDecimal.valueOf(0.15);

    private final HotelScoringService hotelScoringService;
    private final ActivityScoringService activityScoringService;
    private final PackageBuilderService packageBuilderService;

    public RecommendationActor(
            HotelScoringService hotelScoringService,
            ActivityScoringService activityScoringService,
            PackageBuilderService packageBuilderService
    ) {
        this.hotelScoringService = hotelScoringService;
        this.activityScoringService = activityScoringService;
        this.packageBuilderService = packageBuilderService;
    }

    @Override
    public Output run(Input input) {
        TripRequest request = input.request();
        int nights = Math.max(request.nights(), 1);
        int people = Math.max(request.people(), 1);
        int days = Math.max(request.days(), 1);

        BigDecimal nightlyHotelBudget = request.budget()
                .multiply(HOTEL_BUDGET_SHARE)
                .divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
        BigDecimal perPersonActivityBudget = request.budget()
                .multiply(ACTIVITY_BUDGET_SHARE)
                .divide(BigDecimal.valueOf((long) days * people), 2, RoundingMode.HALF_UP);

        List<HotelOption> scoredHotels = input.hotels().stream()
                .map(hotel -> withScore(hotel, hotelScoringService.score(hotel, nightlyHotelBudget, request.preferences())))
                .toList();

        List<ActivityOption> scoredActivities = input.activities().stream()
                .map(activity -> withScore(activity, activityScoringService.score(activity, perPersonActivityBudget, request.preferences())))
                .toList();

        List<TripPackage> packages = packageBuilderService.buildPackages(
                scoredHotels, scoredActivities, request.budget(), people, nights, days,
                input.routeDistanceKm(), request.travelMode(), input.internationalRoute()
        );

        return new Output(scoredHotels, scoredActivities, packages);
    }

    private HotelOption withScore(HotelOption hotel, BigDecimal score) {
        return new HotelOption(
                hotel.externalId(), hotel.name(), hotel.city(), hotel.address(), hotel.pricePerNight(),
                hotel.currency(), hotel.rating(), hotel.reviewCount(), hotel.imageUrl(), hotel.source(),
                hotel.sourceUrl(), score
        );
    }

    private ActivityOption withScore(ActivityOption activity, BigDecimal score) {
        return new ActivityOption(
                activity.externalId(), activity.name(), activity.city(), activity.address(), activity.pricePerPerson(),
                activity.currency(), activity.rating(), activity.reviewCount(), activity.imageUrl(), activity.source(),
                activity.sourceUrl(), activity.durationHours(), activity.tags(), score
        );
    }
}
