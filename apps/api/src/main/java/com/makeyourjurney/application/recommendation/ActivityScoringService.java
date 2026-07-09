package com.makeyourjurney.application.recommendation;

import com.makeyourjurney.domain.model.ActivityOption;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
public class ActivityScoringService {

    private static final double LOCATION_SCORE_PLACEHOLDER = 1.0;
    private static final double IDEAL_DURATION_HOURS = 2.5;

    public BigDecimal score(ActivityOption activity, BigDecimal referenceBudgetPerPerson, List<String> preferences) {
        double budgetScore = budgetScore(activity.pricePerPerson(), referenceBudgetPerPerson);
        double ratingScore = clamp01(activity.rating() == null ? 0.5 : activity.rating().doubleValue() / 10.0);
        double durationFitScore = durationFitScore(activity.durationHours());
        double preferenceScore = preferenceScore(activity, preferences);

        double total = budgetScore * 0.30
                + ratingScore * 0.25
                + LOCATION_SCORE_PLACEHOLDER * 0.20
                + durationFitScore * 0.15
                + preferenceScore * 0.10;

        return BigDecimal.valueOf(total * 100).setScale(2, RoundingMode.HALF_UP);
    }

    private double budgetScore(BigDecimal price, BigDecimal referenceBudgetPerPerson) {
        if (referenceBudgetPerPerson == null || referenceBudgetPerPerson.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.5;
        }
        double ratio = price.doubleValue() / referenceBudgetPerPerson.doubleValue();
        return clamp01(1.0 - (ratio / 2.0));
    }

    private double durationFitScore(BigDecimal durationHours) {
        if (durationHours == null) {
            return 0.5;
        }
        double diff = Math.abs(durationHours.doubleValue() - IDEAL_DURATION_HOURS);
        return clamp01(1.0 - (diff / IDEAL_DURATION_HOURS));
    }

    private double preferenceScore(ActivityOption activity, List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return 0.5;
        }
        String haystack = (activity.name() + " " + String.join(" ", activity.tags())).toLowerCase(Locale.ROOT);
        long matches = preferences.stream()
                .filter(pref -> haystack.contains(pref.toLowerCase(Locale.ROOT)))
                .count();
        return clamp01((double) matches / preferences.size());
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
