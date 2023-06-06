package ru.practicum.shareit.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@EnableJpaRepositories(basePackages = "ru.practicum.shareit")
public interface BookingDataBaseStorage extends JpaRepository<Booking, Long> {
    @Query(value = "select b " +
            "from Booking as b " +
            "where b.userId = ?1 " +
            "order by b.start")
    List<Booking> getBookingsByUserIdSortedByDate(Long id);

    @Query(value = "select b " +
            "from Booking as b " +
            "where b.userId = ?1 " +
            "order by b.start desc")
    List<Booking> getBookingsByUserIdSortedByDateReverse(Long id);

    @Query(value = "select b " +
            "from Booking as b " +
            "order by b.start")
    List<Booking> getBookingsSortedByDate();

    @Query(value = "select b " +
            "from Booking as b " +
            "order by b.start desc")
    List<Booking> getBookingsSortedByDateReverse();
}
