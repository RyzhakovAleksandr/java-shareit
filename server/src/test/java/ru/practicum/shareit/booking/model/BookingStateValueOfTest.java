package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BookingStateValueOfTest {

    @Test
    void valueOf_shouldWorkForAllValues() {
        assertThat(BookingState.valueOf("ALL")).isEqualTo(BookingState.ALL);
        assertThat(BookingState.valueOf("CURRENT")).isEqualTo(BookingState.CURRENT);
        assertThat(BookingState.valueOf("PAST")).isEqualTo(BookingState.PAST);
        assertThat(BookingState.valueOf("FUTURE")).isEqualTo(BookingState.FUTURE);
        assertThat(BookingState.valueOf("WAITING")).isEqualTo(BookingState.WAITING);
        assertThat(BookingState.valueOf("REJECTED")).isEqualTo(BookingState.REJECTED);
    }
}