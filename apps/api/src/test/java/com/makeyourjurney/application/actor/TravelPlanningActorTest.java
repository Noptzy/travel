package com.makeyourjurney.application.actor;

import com.makeyourjurney.domain.enums.BudgetStatus;
import com.makeyourjurney.domain.enums.PackageType;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.BudgetSummary;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.RouteGeometry;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.model.TransportOption;
import com.makeyourjurney.domain.model.TripPackage;
import com.makeyourjurney.domain.model.TripPlanResult;
import com.makeyourjurney.domain.model.TripRequest;
import com.makeyourjurney.domain.port.CachePort;
import com.makeyourjurney.domain.port.TravelDataPort;
import com.makeyourjurney.infrastructure.cache.PlanCacheKeyBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelPlanningActorTest {

    @Mock
    private TravelDataPort travelDataPort;
    @Mock
    private MapsRouteActor mapsRouteActor;
    @Mock
    private RecommendationActor recommendationActor;
    @Mock
    private BudgetActor budgetActor;
    @Mock
    private ItineraryActor itineraryActor;
    @Mock
    private CachePort cachePort;

    @Test
    void run_cacheHit_returnsCachedPlanAndSkipsExpensiveActors() {
        TripRequest request = request();
        TripPlanResult cached = planResult(request, route(), List.of(packageFor(PackageType.BALANCED)));
        when(cachePort.get(PlanCacheKeyBuilder.key(request), TripPlanResult.class)).thenReturn(Optional.of(cached));

        TripPlanResult result = actor().run(request);

        assertThat(result).isSameAs(cached);
        verify(mapsRouteActor, never()).run(any());
        verify(travelDataPort, never()).searchHotels(any(), any(), any(), eq(0), eq(0));
        verify(cachePort, never()).put(any(), any(), any());
    }

    @Test
    void run_cacheMiss_buildsPlanAndStoresItWithConfiguredTtl() {
        TripRequest request = request();
        RouteSummary route = route();
        List<HotelOption> hotels = List.of(hotel("hotel-1"));
        List<ActivityOption> activities = List.of(activity("act-1"));
        List<TripPackage> packages = List.of(
                packageFor(PackageType.HEMAT),
                packageFor(PackageType.BALANCED),
                packageFor(PackageType.NYAMAN)
        );

        when(cachePort.get(PlanCacheKeyBuilder.key(request), TripPlanResult.class)).thenReturn(Optional.empty());
        when(mapsRouteActor.run(any())).thenReturn(route);
        when(travelDataPort.searchHotels("Bandung", request.startDate(), request.endDate(), 4, 3)).thenReturn(hotels);
        when(travelDataPort.searchActivities("Bandung", 20)).thenReturn(activities);
        when(recommendationActor.run(any())).thenReturn(new RecommendationActor.Output(hotels, activities, packages));
        when(itineraryActor.run(any())).thenReturn(List.of());

        TripPlanResult result = actor().run(request);

        assertThat(result.origin()).isEqualTo("Jakarta");
        assertThat(result.destination()).isEqualTo("Bandung");
        assertThat(result.route()).isSameAs(route);
        assertThat(result.packages()).containsExactlyElementsOf(packages);
        assertThat(result.budget()).isEqualTo(packages.get(1).budget());

        ArgumentCaptor<TripPlanResult> planCaptor = ArgumentCaptor.forClass(TripPlanResult.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(cachePort).put(eq(PlanCacheKeyBuilder.key(request)), planCaptor.capture(), ttlCaptor.capture());
        assertThat(planCaptor.getValue()).isSameAs(result);
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void run_routeFailure_propagatesErrorAndDoesNotCachePartialPlan() {
        TripRequest request = request();
        when(cachePort.get(PlanCacheKeyBuilder.key(request), TripPlanResult.class)).thenReturn(Optional.empty());
        when(mapsRouteActor.run(any())).thenThrow(new IllegalArgumentException("Lokasi tidak ditemukan: Atlantis"));

        assertThatThrownBy(() -> actor().run(request))
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("Lokasi tidak ditemukan: Atlantis");

        verify(cachePort, never()).put(any(), any(), any());
    }

    private TravelPlanningActor actor() {
        return new TravelPlanningActor(
                travelDataPort, mapsRouteActor, recommendationActor, budgetActor, itineraryActor, cachePort, 15
        );
    }

    private TripRequest request() {
        return new TripRequest(
                "Jakarta",
                "Bandung",
                LocalDate.parse("2026-07-16"),
                LocalDate.parse("2026-07-24"),
                9,
                8,
                4,
                BigDecimal.valueOf(10_000_000),
                TripStyle.BALANCED,
                TravelMode.CAR,
                List.of("kuliner")
        );
    }

    private TripPlanResult planResult(TripRequest request, RouteSummary route, List<TripPackage> packages) {
        return new TripPlanResult(
                java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
                request.origin(), request.destination(), request.startDate(), request.endDate(),
                route, packages.get(0).budget(), packages, List.of()
        );
    }

    private RouteSummary route() {
        return new RouteSummary(
                new GeoPoint("Jakarta", -6.2088, 106.8456, 1.0),
                new GeoPoint("Bandung", -6.9175, 107.6191, 1.0),
                168.5,
                180,
                "3j 0m",
                "driving-car",
                RouteGeometry.lineString(List.of(
                        List.of(106.8456, -6.2088),
                        List.of(107.6191, -6.9175)
                )),
                List.of(),
                false
        );
    }

    private TripPackage packageFor(PackageType type) {
        BudgetSummary budget = new BudgetSummary(
                "IDR",
                BigDecimal.valueOf(10_000_000),
                BigDecimal.valueOf(4_000_000),
                BigDecimal.valueOf(1_000_000),
                BigDecimal.valueOf(2_700_000),
                BigDecimal.valueOf(500_000),
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(10_000_000),
                BigDecimal.ZERO,
                BudgetStatus.WITHIN_BUDGET
        );
        TransportOption transport = new TransportOption("MOBIL", "Mobil", "Pribadi", "Estimasi", BigDecimal.valueOf(500_000));
        return new TripPackage(
                type,
                budget.estimatedTotal(),
                budget.status(),
                List.of(hotel(type.name())),
                List.of(activity(type.name())),
                transport,
                List.of(transport),
                budget,
                type.name()
        );
    }

    private HotelOption hotel(String id) {
        return new HotelOption(
                id, "Hotel " + id, "Bandung", "Dago", BigDecimal.valueOf(500_000),
                "IDR", BigDecimal.valueOf(8.5), 100, "https://example.com/hotel.jpg", "test", null, BigDecimal.TEN
        );
    }

    private ActivityOption activity(String id) {
        return new ActivityOption(
                id, "Activity " + id, "Bandung", "Braga", BigDecimal.valueOf(100_000),
                "IDR", BigDecimal.valueOf(8.2), 100, "https://example.com/activity.jpg", "test", null,
                BigDecimal.valueOf(2), List.of("kuliner"), BigDecimal.TEN
        );
    }
}
