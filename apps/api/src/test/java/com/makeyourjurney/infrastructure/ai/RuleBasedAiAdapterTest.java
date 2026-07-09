package com.makeyourjurney.infrastructure.ai;

import com.makeyourjurney.domain.model.TripIntent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedAiAdapterTest {

    private final RuleBasedAiAdapter adapter = new RuleBasedAiAdapter();

    @Test
    void parseIntent_specExampleSentence_extractsAllFields() {
        TripIntent intent = adapter.parseIntent(
                "Aku dari Jakarta mau ke Surabaya, budget 3 juta, 3 hari 2 malam untuk 2 orang", null
        );

        assertThat(intent.origin()).isEqualTo("Jakarta");
        assertThat(intent.destination()).isEqualTo("Surabaya");
        assertThat(intent.budget()).isEqualByComparingTo(BigDecimal.valueOf(3_000_000));
        assertThat(intent.days()).isEqualTo(3);
        assertThat(intent.nights()).isEqualTo(2);
        assertThat(intent.people()).isEqualTo(2);
        assertThat(intent.missingFields()).isEmpty();
        assertThat(intent.readyToPlan()).isTrue();
    }

    @Test
    void parseIntent_missingRequiredFields_listsThemAndIsNotReady() {
        TripIntent intent = adapter.parseIntent("Aku mau healing", null);

        assertThat(intent.missingFields()).contains("destination", "budget", "people", "days/nights");
        assertThat(intent.readyToPlan()).isFalse();
    }

    @Test
    void parseIntent_secondTurn_carriesForwardFieldsFromPreviousIntent() {
        TripIntent first = adapter.parseIntent("Aku dari Jakarta mau ke Surabaya", null);

        TripIntent second = adapter.parseIntent("budget 3 juta, 3 hari 2 malam untuk 2 orang", first);

        assertThat(second.origin()).isEqualTo("Jakarta");
        assertThat(second.destination()).isEqualTo("Surabaya");
        assertThat(second.budget()).isEqualByComparingTo(BigDecimal.valueOf(3_000_000));
        assertThat(second.readyToPlan()).isTrue();
    }
}
