package com.makeyourjurney.presentation.dto.request;

import com.makeyourjurney.application.actor.MapsRouteActor;
import com.makeyourjurney.domain.enums.TravelMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MapsRouteRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull TravelMode travelMode
) {
    public MapsRouteActor.Input toActorInput() {
        return new MapsRouteActor.Input(origin, destination, travelMode);
    }
}
