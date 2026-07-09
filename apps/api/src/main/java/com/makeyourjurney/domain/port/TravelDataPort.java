package com.makeyourjurney.domain.port;

import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.Review;

import java.time.LocalDate;
import java.util.List;

public interface TravelDataPort {
    List<HotelOption> searchHotels(String destination, LocalDate checkIn, LocalDate checkOut, int people, int limit);

    List<ActivityOption> searchActivities(String destination, int limit);

    List<Review> fetchReviews(String sourceUrl, int limit);
}
