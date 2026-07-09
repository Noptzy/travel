package com.makeyourjurney.infrastructure.ai;

import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;
import com.makeyourjurney.domain.model.TripIntent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class TripIntentMerger {

    private TripIntentMerger() {
    }

    static TripIntent merge(TripIntent parsed, TripIntent previous) {
        String origin = coalesce(parsed.origin(), field(previous, TripIntent::origin));
        String destination = coalesce(parsed.destination(), field(previous, TripIntent::destination));
        LocalDate startDate = coalesce(parsed.startDate(), field(previous, TripIntent::startDate));
        LocalDate endDate = coalesce(parsed.endDate(), field(previous, TripIntent::endDate));
        Integer days = coalesce(parsed.days(), field(previous, TripIntent::days));
        Integer nights = coalesce(parsed.nights(), field(previous, TripIntent::nights));
        Integer people = coalesce(parsed.people(), field(previous, TripIntent::people));
        BigDecimal budget = coalesce(parsed.budget(), field(previous, TripIntent::budget));
        TripStyle tripStyle = coalesce(parsed.tripStyle(), field(previous, TripIntent::tripStyle));
        TravelMode travelMode = coalesce(parsed.travelMode(), field(previous, TripIntent::travelMode));
        List<String> preferences = !parsed.preferences().isEmpty()
                ? parsed.preferences()
                : previous == null ? List.of() : previous.preferences();

        List<String> missing = new ArrayList<>();
        if (destination == null || destination.isBlank()) {
            missing.add("destination");
        }
        if (budget == null) {
            missing.add("budget");
        }
        if (people == null) {
            missing.add("people");
        }
        boolean hasDuration = (days != null && nights != null) || (startDate != null && endDate != null);
        if (!hasDuration) {
            missing.add("days/nights");
        }

        return new TripIntent(
                origin, destination, startDate, endDate, days, nights, people, budget,
                tripStyle, travelMode, List.copyOf(preferences), List.copyOf(missing), missing.isEmpty()
        );
    }

    private static <T> T field(TripIntent intent, java.util.function.Function<TripIntent, T> getter) {
        return intent == null ? null : getter.apply(intent);
    }

    private static <T> T coalesce(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
