package com.makeyourjurney.domain.port;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {
    <T> Optional<T> get(String key, Class<T> type);

    void put(String key, Object value, Duration ttl);
}
