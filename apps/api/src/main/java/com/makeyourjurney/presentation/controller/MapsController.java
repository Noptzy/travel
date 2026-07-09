package com.makeyourjurney.presentation.controller;

import com.makeyourjurney.application.actor.MapsRouteActor;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.presentation.dto.request.MapsRouteRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maps")
public class MapsController {

    private final MapsRouteActor mapsRouteActor;

    public MapsController(MapsRouteActor mapsRouteActor) {
        this.mapsRouteActor = mapsRouteActor;
    }

    @PostMapping("/route")
    public RouteSummary route(@Valid @RequestBody MapsRouteRequest request) {
        return mapsRouteActor.run(request.toActorInput());
    }
}
