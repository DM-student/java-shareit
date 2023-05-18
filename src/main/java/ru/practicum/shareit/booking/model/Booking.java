package ru.practicum.shareit.booking.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {
    private long id = Long.MIN_VALUE;

    private long itemOwnerId = Long.MIN_VALUE;

    private long receiverId = Long.MIN_VALUE;

    private LocalDateTime bookingOpening;
}
