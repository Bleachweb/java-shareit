package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    //Получение списка всех бронирований пользователя:
    // ALL.
    List<Booking> findAllBookingsByBookerIdOrderByStartDesc(int bookerId);

    // CURRENT.
    @Query("""
            SELECT b
            FROM Booking AS b
            WHERE b.booker.id = :bookerId
            AND b.start <= :time
            AND b.end >= :time
            """)
    List<Booking> findAllCurrentBookingsByBookerId(int bookerId, LocalDateTime time);

    // PAST.
    List<Booking> findAllBookingsByBookerIdAndEndBeforeOrderByStartDesc(int bookerId, LocalDateTime time);

    // FUTURE.
    List<Booking> findAllBookingsByBookerIdAndStartAfterOrderByStartDesc(int bookerId, LocalDateTime time);

    // WAITING.
    // REJECTED.
    List<Booking> findAllBookingsByBookerIdAndStatusOrderByStartDesc(int bookerId, BookingStatus status);

    //Получение списка всех бронирований владельца:
    // ALL.
    List<Booking> findAllBookingsByItemIdInOrderByStartDesc(Set<Integer> itemIds);

    // CURRENT.
    @Query("""
            SELECT b
            FROM Booking AS b
            WHERE b.item.user.id = :ownerId
            AND b.start <= :time
            AND b.end >= :time
            """)
    List<Booking> findAllCurrentBookingsByOwnerId(int ownerId, LocalDateTime time);

    // PAST.
    List<Booking> findAllBookingsByItemUserIdAndEndBeforeOrderByStartDesc(int ownerId, LocalDateTime time);

    // FUTURE.
    List<Booking> findAllBookingsByItemUserIdAndStartAfterOrderByStartDesc(int ownerId, LocalDateTime time);

    // WAITING.
    // REJECTED.
    List<Booking> findAllBookingsByItemUserIdAndStatusOrderByStartDesc(int ownerId, BookingStatus status);

    @Query("""
            SELECT b
            FROM Booking b
            JOIN FETCH b.item
            JOIN FETCH b.booker
            WHERE b.item.user.id = :ownerId
            """)
    List<Booking> findAllBookingsByOwnerWithItemsAndBookers(int ownerId);
}