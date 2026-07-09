package com.makeyourjurney.domain.port;

import com.makeyourjurney.domain.model.TripIntent;

public interface AiPort {
    TripIntent parseIntent(String message, TripIntent previousIntent);
}
