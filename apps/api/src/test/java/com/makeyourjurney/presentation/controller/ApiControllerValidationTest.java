package com.makeyourjurney.presentation.controller;

import com.makeyourjurney.application.actor.BudgetActor;
import com.makeyourjurney.application.actor.MapsRouteActor;
import com.makeyourjurney.application.actor.TravelPlanningActor;
import com.makeyourjurney.domain.enums.BudgetStatus;
import com.makeyourjurney.domain.enums.PackageType;
import com.makeyourjurney.domain.model.BudgetSummary;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.RouteGeometry;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.model.TransportOption;
import com.makeyourjurney.domain.model.TripIntent;
import com.makeyourjurney.domain.model.TripPackage;
import com.makeyourjurney.domain.model.TripPlanResult;
import com.makeyourjurney.domain.port.AiPort;
import com.makeyourjurney.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PlannerController.class, MapsController.class, BudgetController.class})
@AutoConfigureMockMvc(addFilters = false)
class ApiControllerValidationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TravelPlanningActor travelPlanningActor;

    @MockitoBean
    private AiPort aiPort;

    @MockitoBean
    private MapsRouteActor mapsRouteActor;

    @MockitoBean
    private BudgetActor budgetActor;

    @MockitoBean
    private JwtService jwtService;

    @TestConfiguration
    static class ExecutorConfig {
        @Bean
        @Qualifier("plannerExecutor")
        Executor plannerExecutor() {
            return Runnable::run;
        }
    }

    @Test
    void plan_validPayload_returnsTripPlan() throws Exception {
        when(travelPlanningActor.run(any())).thenReturn(planResult());

        var result = mvc.perform(post("/api/v1/planner/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "origin": "Jakarta",
                                  "destination": "Bandung",
                                  "startDate": "2026-07-16",
                                  "endDate": "2026-07-24",
                                  "days": 9,
                                  "nights": 8,
                                  "people": 4,
                                  "budget": 10000000,
                                  "tripStyle": "BALANCED",
                                  "travelMode": "CAR",
                                  "preferences": ["kuliner"]
                                }
                                """))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("Bandung"))
                .andExpect(jsonPath("$.route.geometry.coordinates.length()").value(2))
                .andExpect(jsonPath("$.packages.length()").value(1));
    }

    @Test
    void plan_missingRequiredFields_returnsValidationErrorsAndDoesNotRunActor() throws Exception {
        mvc.perform(post("/api/v1/planner/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "origin": "",
                                  "startDate": "2026-07-16",
                                  "endDate": "2026-07-24",
                                  "days": 0,
                                  "nights": 8,
                                  "people": 0,
                                  "budget": -1,
                                  "tripStyle": "BALANCED",
                                  "travelMode": "CAR"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.origin").exists())
                .andExpect(jsonPath("$.fieldErrors.destination").exists())
                .andExpect(jsonPath("$.fieldErrors.days").exists())
                .andExpect(jsonPath("$.fieldErrors.people").exists())
                .andExpect(jsonPath("$.fieldErrors.budget").exists());

        verify(travelPlanningActor, never()).run(any());
    }

    @Test
    void plan_unknownEnum_returnsBadRequestInsteadOfInternalServerError() throws Exception {
        mvc.perform(post("/api/v1/planner/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "origin": "Jakarta",
                                  "destination": "Bandung",
                                  "startDate": "2026-07-16",
                                  "endDate": "2026-07-24",
                                  "days": 9,
                                  "nights": 8,
                                  "people": 4,
                                  "budget": 10000000,
                                  "tripStyle": "NOT_A_STYLE",
                                  "travelMode": "CAR",
                                  "preferences": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void maps_unknownLocation_returnsBadRequestFromActorError() throws Exception {
        when(mapsRouteActor.run(any())).thenThrow(new IllegalArgumentException("Lokasi tidak ditemukan: Atlantis"));

        mvc.perform(post("/api/v1/maps/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "origin": "Jakarta",
                                  "destination": "Atlantis",
                                  "travelMode": "CAR"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lokasi tidak ditemukan: Atlantis"));
    }

    @Test
    void maps_missingTravelMode_returnsValidationError() throws Exception {
        mvc.perform(post("/api/v1/maps/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "origin": "Jakarta",
                                  "destination": "Bandung"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.travelMode").exists());
    }

    @Test
    void budget_negativeBudget_returnsValidationErrorAndDoesNotRunActor() throws Exception {
        mvc.perform(post("/api/v1/budget/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "budget": -100,
                                  "people": 4,
                                  "nights": 8,
                                  "days": 9,
                                  "hotelPricePerNight": 500000,
                                  "activityPricesPerPerson": [100000],
                                  "routeDistanceKm": 168.5,
                                  "travelMode": "CAR"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.budget").exists());

        verify(budgetActor, never()).run(any());
    }

    @Test
    void intent_emptyMessage_returnsValidationError() throws Exception {
        mvc.perform(post("/api/v1/planner/intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    @Test
    void intent_validMessage_returnsParsedIntent() throws Exception {
        when(aiPort.parseIntent(any(), any())).thenReturn(new TripIntent(
                "Jakarta", "Bandung", LocalDate.parse("2026-07-16"), LocalDate.parse("2026-07-24"),
                9, 8, 4, BigDecimal.valueOf(10_000_000), null, null, List.of("kuliner"), List.of(), true
        ));

        mvc.perform(post("/api/v1/planner/intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Jakarta ke Bandung untuk 4 orang"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origin").value("Jakarta"))
                .andExpect(jsonPath("$.destination").value("Bandung"))
                .andExpect(jsonPath("$.readyToPlan").value(true));
    }

    private TripPlanResult planResult() {
        RouteSummary route = new RouteSummary(
                new GeoPoint("Jakarta", -6.2088, 106.8456, 1.0),
                new GeoPoint("Bandung", -6.9175, 107.6191, 1.0),
                168.5,
                180,
                "3j 0m",
                "driving",
                RouteGeometry.lineString(List.of(
                        List.of(106.8456, -6.2088),
                        List.of(107.6191, -6.9175)
                )),
                List.of(),
                false
        );
        BudgetSummary budget = new BudgetSummary(
                "IDR", BigDecimal.valueOf(10_000_000), BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.valueOf(9_000_000), BigDecimal.valueOf(1_000_000), BudgetStatus.UNDER_BUDGET
        );
        TransportOption transport = new TransportOption("MOBIL", "Mobil", "Pribadi", "Estimasi", BigDecimal.valueOf(500_000));
        TripPackage tripPackage = new TripPackage(
                PackageType.BALANCED, BigDecimal.valueOf(9_000_000), BudgetStatus.UNDER_BUDGET,
                List.of(), List.of(), transport, List.of(transport), budget, "Seimbang"
        );
        return new TripPlanResult(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Jakarta", "Bandung", LocalDate.parse("2026-07-16"), LocalDate.parse("2026-07-24"),
                route, budget, List.of(tripPackage), List.of()
        );
    }
}
