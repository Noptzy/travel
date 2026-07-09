package com.makeyourjurney.domain.port;

import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.RouteSummary;

public interface MapsPort {
    GeoPoint geocode(String query);

    RouteSummary route(GeoPoint origin, GeoPoint destination, TravelMode travelMode);
}
