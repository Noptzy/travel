package com.makeyourjurney.application.actor;

import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.port.MapsPort;
import org.springframework.stereotype.Component;

@Component
public class MapsRouteActor implements Actor<MapsRouteActor.Input, RouteSummary> {

    public record Input(String origin, String destination, TravelMode travelMode) {
    }

    private final MapsPort mapsPort;

    public MapsRouteActor(MapsPort mapsPort) {
        this.mapsPort = mapsPort;
    }

    @Override
    public RouteSummary run(Input input) {
        var originPoint = mapsPort.geocode(input.origin());
        var destinationPoint = mapsPort.geocode(input.destination());
        return mapsPort.route(originPoint, destinationPoint, input.travelMode());
    }
}
