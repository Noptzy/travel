package com.makeyourjurney.presentation.exception;

import java.util.Map;

public record ApiError(String message, Map<String, String> fieldErrors) {
    public static ApiError of(String message) {
        return new ApiError(message, Map.of());
    }
}
