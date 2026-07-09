package com.makeyourjurney.application.recommendation;

import com.makeyourjurney.domain.model.HotelOption;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
public class HotelScoringService {

    private static final double LOCATION_SCORE_PLACEHOLDER = 1.0;
    private static final double REVIEW_COUNT_CAP = 500.0;

    public BigDecimal score(HotelOption hotel, BigDecimal referenceBudgetPerNight, List<String> preferences) {
        double budgetScore = budgetScore(hotel.pricePerNight(), referenceBudgetPerNight);
        double ratingScore = clamp01(hotel.rating() == null ? 0.5 : hotel.rating().doubleValue() / 10.0);
        double reviewScore = clamp01(hotel.reviewCount() / REVIEW_COUNT_CAP);
        double preferenceScore = preferenceScore(hotel, preferences);

        double total = budgetScore * 0.35
                + ratingScore * 0.25
                + reviewScore * 0.15
                + LOCATION_SCORE_PLACEHOLDER * 0.15
                + preferenceScore * 0.10;

        return BigDecimal.valueOf(total * 100).setScale(2, RoundingMode.HALF_UP);
    }

    private double budgetScore(BigDecimal price, BigDecimal referenceBudgetPerNight) {
        if (referenceBudgetPerNight == null || referenceBudgetPerNight.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.5;
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.5;
        }
        double ratio = price.doubleValue() / referenceBudgetPerNight.doubleValue();
        return clamp01(1.0 - (ratio / 2.0));
    }

    private double preferenceScore(HotelOption hotel, List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return 0.5;
        }
        String haystack = (hotel.name() + " " + hotel.address()).toLowerCase(Locale.ROOT);
        long matches = preferences.stream()
                .filter(pref -> haystack.contains(pref.toLowerCase(Locale.ROOT)))
                .count();
        return clamp01((double) matches / preferences.size());
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
