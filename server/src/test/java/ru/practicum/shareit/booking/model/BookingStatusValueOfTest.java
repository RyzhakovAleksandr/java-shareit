package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BookingStatusValueOfTest {

    @Test
    void valueOf_shouldWorkForAllValues() {
        assertThat(BookingStatus.valueOf("WAITING")).isEqualTo(BookingStatus.WAITING);
        assertThat(BookingStatus.valueOf("APPROVED")).isEqualTo(BookingStatus.APPROVED);
        assertThat(BookingStatus.valueOf("REJECTED")).isEqualTo(BookingStatus.REJECTED);
        assertThat(BookingStatus.valueOf("CANCELED")).isEqualTo(BookingStatus.CANCELED);
    }
}