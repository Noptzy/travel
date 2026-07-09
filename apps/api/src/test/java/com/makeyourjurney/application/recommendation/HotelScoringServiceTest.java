package com.makeyourjurney.application.recommendation;

import com.makeyourjurney.domain.model.HotelOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotelScoringServiceTest {

    private final HotelScoringService service = new HotelScoringService();

    @Test
    void score_zeroPriceIsNeutralNotArtificiallyBest() {
        BigDecimal unknownPriceScore = service.score(hotel(BigDecimal.ZERO), BigDecimal.valueOf(500_000), List.of());
        BigDecimal cheapHotelScore = service.score(hotel(BigDecimal.valueOf(250_000)), BigDecimal.valueOf(500_000), List.of());

        assertThat(cheapHotelScore).isGreaterThan(unknownPriceScore);
    }

    private HotelOption hotel(BigDecimal price) {
        return new HotelOption(
                "hotel",
                "Real Hotel",
                "Sanya",
                "Real address",
                price,
                "IDR",
                BigDecimal.valueOf(8.5),
                100,
                "https://example.com/hotel.jpg",
                "Google Maps via Apify",
                "https://maps.google.com/?cid=hotel",
                null
        );
    }
}
