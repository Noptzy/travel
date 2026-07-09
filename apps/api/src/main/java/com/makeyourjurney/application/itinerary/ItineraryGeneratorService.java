package com.makeyourjurney.application.itinerary;

import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.ItineraryDay;
import com.makeyourjurney.domain.model.ItineraryItem;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.model.TripRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItineraryGeneratorService {

    private static final int MAX_ACTIVITIES_PER_DAY = 3;
    private static final long LONG_ROUTE_MINUTES = 300;

    public List<ItineraryDay> generate(TripRequest request, RouteSummary route, List<ActivityOption> selectedActivities) {
        int days = request.days();
        boolean longRoute = route != null && route.durationMinutes() > LONG_ROUTE_MINUTES;

        List<List<ActivityOption>> perDayActivities = distribute(selectedActivities, days, longRoute);

        List<ItineraryDay> result = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            List<ItineraryItem> items = new ArrayList<>();
            if (day == 1) {
                items.add(item("08:00", "Berangkat dari " + request.origin(), "Perjalanan menuju " + request.destination(), "ROUTE"));
                items.add(item("13:00", "Check-in hotel", "Istirahat sejenak setelah perjalanan", "CHECKIN"));
            }
            int activityIndex = 0;
            for (ActivityOption activity : perDayActivities.get(day - 1)) {
                String time = switch (activityIndex++) { case 0 -> "09:00"; case 1 -> "13:30"; default -> "16:30"; };
                items.add(new ItineraryItem(time, activity.name(), activity.address(), "ACTIVITY", activity.pricePerPerson()));
            }
            if (day == days) {
                items.add(item("10:00", "Check-out hotel", "Bersiap pulang", "CHECKOUT"));
                items.add(item("11:00", "Belanja oleh-oleh", "Waktu santai sebelum pulang", "SOUVENIR"));
                items.add(item("14:00", "Pulang ke " + request.origin(), "Perjalanan kembali", "ROUTE"));
            }
            result.add(new ItineraryDay(day, request.startDate().plusDays(day - 1L), items));
        }
        return result;
    }

    private List<List<ActivityOption>> distribute(List<ActivityOption> activities, int days, boolean longRoute) {
        List<List<ActivityOption>> perDay = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            perDay.add(new ArrayList<>());
        }
        if (activities.isEmpty() || days == 0) {
            return perDay;
        }
        int day = 0;
        for (ActivityOption activity : activities) {
            boolean isFirstDay = day == 0;
            int cap = (isFirstDay && longRoute) ? 1 : MAX_ACTIVITIES_PER_DAY;
            if (perDay.get(day).size() >= cap) {
                day = (day + 1) % days;
            }
            perDay.get(day).add(activity);
            day = (day + 1) % days;
        }
        return perDay;
    }

    private ItineraryItem item(String time, String title, String description, String type) {
        return new ItineraryItem(time, title, description, type, BigDecimal.ZERO);
    }
}
