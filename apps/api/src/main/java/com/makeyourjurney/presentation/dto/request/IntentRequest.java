package com.makeyourjurney.presentation.dto.request;

import com.makeyourjurney.domain.model.TripIntent;
import jakarta.validation.constraints.NotBlank;

public record IntentRequest(
        @NotBlank String message,
        TripIntent previousIntent
) {
}
