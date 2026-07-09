package com.makeyourjurney.presentation.controller;

import com.makeyourjurney.application.actor.TravelPlanningActor;
import com.makeyourjurney.domain.model.TripIntent;
import com.makeyourjurney.domain.model.TripPlanResult;
import com.makeyourjurney.domain.port.AiPort;
import com.makeyourjurney.presentation.dto.request.IntentRequest;
import com.makeyourjurney.presentation.dto.request.PlanRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/v1/planner")
public class PlannerController {

    private final TravelPlanningActor travelPlanningActor;
    private final AiPort aiPort;
    private final Executor plannerExecutor;

    public PlannerController(TravelPlanningActor travelPlanningActor, AiPort aiPort, @Qualifier("plannerExecutor") Executor plannerExecutor) {
        this.travelPlanningActor = travelPlanningActor;
        this.aiPort = aiPort;
        this.plannerExecutor = plannerExecutor;
    }

    @PostMapping("/plan")
    public CompletableFuture<TripPlanResult> plan(@Valid @RequestBody PlanRequest request) {
        return CompletableFuture.supplyAsync(() -> travelPlanningActor.run(request.toDomain()), plannerExecutor);
    }

    @PostMapping("/intent")
    public TripIntent intent(@Valid @RequestBody IntentRequest request) {
        return aiPort.parseIntent(request.message(), request.previousIntent());
    }
}
