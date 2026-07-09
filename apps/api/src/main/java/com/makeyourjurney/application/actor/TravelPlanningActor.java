package com.makeyourjurney.application.actor;

import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.model.TripPackage;
import com.makeyourjurney.domain.model.TripPlanResult;
import com.makeyourjurney.domain.model.TripRequest;
import com.makeyourjurney.domain.port.TravelDataPort;
import com.makeyourjurney.infrastructure.cache.PlanCacheKeyBuilder;
import com.makeyourjurney.domain.port.CachePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class TravelPlanningActor implements Actor<TripRequest, TripPlanResult> {

    private static final int HOTEL_SEARCH_LIMIT = 3;
    private static final int ACTIVITY_SEARCH_LIMIT = 20;
    private static final int HEADLINE_PACKAGE_INDEX = 1;

    private final TravelDataPort travelDataPort;
    private final MapsRouteActor mapsRouteActor;
    private final RecommendationActor recommendationActor;
    private final BudgetActor budgetActor;
    private final ItineraryActor itineraryActor;
    private final CachePort cachePort;
    private final long planCacheTtlMinutes;

    public TravelPlanningActor(
            TravelDataPort travelDataPort,
            MapsRouteActor mapsRouteActor,
            RecommendationActor recommendationActor,
            BudgetActor budgetActor,
            ItineraryActor itineraryActor,
            CachePort cachePort,
            @Value("${app.plan-cache-ttl-minutes}") long planCacheTtlMinutes
    ) {
        this.travelDataPort = travelDataPort;
        this.mapsRouteActor = mapsRouteActor;
        this.recommendationActor = recommendationActor;
        this.budgetActor = budgetActor;
        this.itineraryActor = itineraryActor;
        this.cachePort = cachePort;
        this.planCacheTtlMinutes = planCacheTtlMinutes;
    }

    @Override
    public TripPlanResult run(TripRequest request) {
        String cacheKey = PlanCacheKeyBuilder.key(request);
        var cached = cachePort.get(cacheKey, TripPlanResult.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        var routeFuture = CompletableFuture.supplyAsync(() -> mapsRouteActor.run(
                new MapsRouteActor.Input(request.origin(), request.destination(), request.travelMode())
        ));
        var hotelsFuture = CompletableFuture.supplyAsync(() -> travelDataPort.searchHotels(
                request.destination(), request.startDate(), request.endDate(), request.people(), HOTEL_SEARCH_LIMIT
        ));
        var activitiesFuture = CompletableFuture.supplyAsync(() ->
                travelDataPort.searchActivities(request.destination(), ACTIVITY_SEARCH_LIMIT));

        RouteSummary route = routeFuture.join();
        List<HotelOption> hotels = hotelsFuture.join();
        List<ActivityOption> activities = activitiesFuture.join();

        RecommendationActor.Output recommendation = recommendationActor.run(
                new RecommendationActor.Input(request, hotels, activities, route.distanceKm(), route.estimatedOnly())
        );

        TripPackage headlinePackage = recommendation.packages().get(HEADLINE_PACKAGE_INDEX);
        var budget = headlinePackage.budget();

        var itinerary = itineraryActor.run(
                new ItineraryActor.Input(request, route, headlinePackage.activities())
        );

        TripPlanResult result = new TripPlanResult(
                UUID.randomUUID(), request.origin(), request.destination(), request.startDate(), request.endDate(),
                route, budget, recommendation.packages(), itinerary
        );

        cachePort.put(cacheKey, result, Duration.ofMinutes(planCacheTtlMinutes));
        return result;
    }
}
