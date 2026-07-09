package com.makeyourjurney.application.actor;

import com.makeyourjurney.application.itinerary.ItineraryGeneratorService;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.ItineraryDay;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.model.TripRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItineraryActor implements Actor<ItineraryActor.Input, List<ItineraryDay>> {

    public record Input(TripRequest request, RouteSummary route, List<ActivityOption> selectedActivities) {
    }

    private final ItineraryGeneratorService itineraryGeneratorService;

    public ItineraryActor(ItineraryGeneratorService itineraryGeneratorService) {
        this.itineraryGeneratorService = itineraryGeneratorService;
    }

    @Override
    public List<ItineraryDay> run(Input input) {
        return itineraryGeneratorService.generate(input.request(), input.route(), input.selectedActivities());
    }
}
