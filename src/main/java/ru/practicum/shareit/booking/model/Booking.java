package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    @NotNull(message = "Id пользователя не был указан.")
    private Long userId;

    @NotNull(message = "Id предмета не был указан.")
    @Column(name = "item_id")
    private Long itemId;

    @NotNull(message = "Состояние не указано.")
    @Column(name = "state")
    private BookingState state;

    @Column(name = "start_date")
    @NotNull(message = "Дата начала отсутствует.")
    private LocalDateTime start;

    @Column(name = "end_date")
    @NotNull(message = "Дата конца отсутствует.")
    private LocalDateTime end;

    public Booking getClearCopy() {
        return new Booking(id, userId, itemId, state, start, end);
    }

    public void mergeFrom(Booking otherBooking) {
        if(otherBooking.id != null) {
            id = otherBooking.id;
        }
        if(otherBooking.userId != null) {
            userId = otherBooking.userId;
        }
        if(otherBooking.itemId != null) {
            itemId = otherBooking.itemId;
        }
        if(otherBooking.state != null) {
            state = otherBooking.state;
        }
        if(otherBooking.start != null) {
            start = otherBooking.start;
        }
        if(otherBooking.end != null) {
            end = otherBooking.end;
        }
    }
}
