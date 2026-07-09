package com.makeyourjurney.domain.model;

import java.util.List;
import java.time.LocalDate;

public record ItineraryDay(
        int day,
        LocalDate date,
        List<ItineraryItem> items
) {
}
