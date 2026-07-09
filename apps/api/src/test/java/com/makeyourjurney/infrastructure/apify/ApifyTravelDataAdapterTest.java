package com.makeyourjurney.infrastructure.apify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.port.CachePort;
import com.makeyourjurney.infrastructure.maps.NominatimActivityFallbackClient;
import com.makeyourjurney.infrastructure.maps.NominatimHotelFallbackClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApifyTravelDataAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ApifyActorRunner actorRunner;
    @Mock
    private CachePort cachePort;
    @Mock
    private NominatimHotelFallbackClient nominatimHotelFallbackClient;
    @Mock
    private NominatimActivityFallbackClient nominatimActivityFallbackClient;

    @Test
    void searchHotels_prefersPricedTravelokaDataBeforeGooglePlacesFallback() throws Exception {
        when(cachePort.get(any(), eq(HotelOption[].class))).thenReturn(Optional.empty());
        when(actorRunner.runSyncGetDatasetItems(eq("traveloka/hotels"), any())).thenReturn(objectMapper.readTree("""
                [
                  {
                    "externalId": "hotel-1",
                    "name": "Traveloka Priced Hotel",
                    "city": "Chiang Mai",
                    "address": "Real address",
                    "pricePerNight": 720000,
                    "currency": "IDR",
                    "rating": 8.7,
                    "reviewCount": 120,
                    "imageUrl": "https://example.com/hotel.jpg",
                    "sourceUrl": "https://www.traveloka.com/hotel/example"
                  }
                ]
                """));

        var adapter = adapter();
        List<HotelOption> hotels = adapter.searchHotels(
                "Chiang Mai",
                LocalDate.parse("2026-07-16"),
                LocalDate.parse("2026-07-24"),
                4,
                3
        );

        assertThat(hotels).hasSize(1);
        assertThat(hotels.get(0).name()).isEqualTo("Traveloka Priced Hotel");
        assertThat(hotels.get(0).pricePerNight()).isEqualByComparingTo("720000");
        assertThat(hotels.get(0).source()).isEqualTo("Traveloka via Apify");
        verify(actorRunner, never()).runSyncGetDatasetItems(eq("google/places"), any());
    }

    private ApifyTravelDataAdapter adapter() {
        return new ApifyTravelDataAdapter(
                actorRunner,
                new ApifyNormalizer(),
                cachePort,
                nominatimHotelFallbackClient,
                nominatimActivityFallbackClient,
                "traveloka/hotels",
                "traveloka/activities",
                "traveloka/reviews",
                "google/places",
                24
        );
    }
}
