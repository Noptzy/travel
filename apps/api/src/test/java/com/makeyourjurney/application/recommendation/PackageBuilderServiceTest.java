package com.makeyourjurney.application.recommendation;

import com.makeyourjurney.application.budget.BudgetCalculationService;
import com.makeyourjurney.domain.enums.PackageType;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageBuilderServiceTest {

    private final PackageBuilderService service = new PackageBuilderService(new BudgetCalculationService());

    @Test
    void buildPackages_zeroHotelPrices_remainUnknownInsteadOfBeingEstimatedPerPackage() {
        var packages = service.buildPackages(
                List.of(
                        hotel("h1", "South China Hotel", BigDecimal.ZERO, "4.3", "6"),
                        hotel("h2", "Yuhai Resort", BigDecimal.ZERO, "4.8", "8"),
                        hotel("h3", "Sanya Bay Inn", BigDecimal.ZERO, "4.1", "7")
                ),
                List.of(activity("a1", BigDecimal.ZERO, "8")),
                BigDecimal.valueOf(10_000_000),
                4,
                8,
                9,
                1000,
                TravelMode.CAR,
                true
        );

        assertThat(packages)
                .extracting(pack -> pack.hotels().get(0).pricePerNight())
                .containsOnly(BigDecimal.ZERO);
        assertThat(packages.get(0).hotels().get(0).name()).isEqualTo("Sanya Bay Inn");
        assertThat(packages.get(1).hotels().get(0).name()).isEqualTo("South China Hotel");
        assertThat(packages.get(2).hotels().get(0).name()).isEqualTo("Yuhai Resort");
    }

    @Test
    void buildPackages_usesDifferentHotelsWhenMultipleOptionsExist() {
        var packages = service.buildPackages(
                List.of(
                        hotel("cheap", "Cheap Hotel", BigDecimal.valueOf(180_000), "4.3", "5"),
                        hotel("mid", "Balanced Hotel", BigDecimal.valueOf(240_000), "4.5", "7"),
                        hotel("best", "Comfort Hotel", BigDecimal.valueOf(320_000), "4.9", "9")
                ),
                List.of(activity("a1", BigDecimal.valueOf(50_000), "8")),
                BigDecimal.valueOf(10_000_000),
                4,
                8,
                9,
                1000,
                TravelMode.CAR,
                true
        );

        assertThat(packages).hasSize(3);
        assertThat(packages.get(0).type()).isEqualTo(PackageType.HEMAT);
        assertThat(packages.get(0).hotels().get(0).name()).isEqualTo("Cheap Hotel");
        assertThat(packages.get(1).type()).isEqualTo(PackageType.BALANCED);
        assertThat(packages.get(1).hotels().get(0).name()).isEqualTo("Balanced Hotel");
        assertThat(packages.get(2).type()).isEqualTo(PackageType.NYAMAN);
        assertThat(packages.get(2).hotels().get(0).name()).isEqualTo("Comfort Hotel");
    }

    private HotelOption hotel(String id, String name, BigDecimal price, String rating, String score) {
        return new HotelOption(
                id,
                name,
                "Sanya",
                "Real address",
                price,
                "IDR",
                new BigDecimal(rating),
                100,
                "https://example.com/hotel.jpg",
                "Google Maps via Apify",
                "https://maps.google.com/?cid=" + id,
                new BigDecimal(score)
        );
    }

    private ActivityOption activity(String id, BigDecimal price, String score) {
        return new ActivityOption(
                id,
                "Activity " + id,
                "Sanya",
                "Real address",
                price,
                "IDR",
                BigDecimal.valueOf(4.5),
                100,
                "https://example.com/activity.jpg",
                "Google Maps via Apify",
                "https://maps.google.com/?cid=" + id,
                BigDecimal.valueOf(2),
                List.of("tourist attraction"),
                new BigDecimal(score)
        );
    }
}
