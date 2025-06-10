package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Component
public class BookingMapper {

    public BookingDtoResponse toBookingDto(Booking booking) {
        return BookingDtoResponse.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }

    public Booking toBooking(BookingDtoRequest bookingDtoRequest, User booker, Item item) {
        Booking booking = new Booking();
        booking.setStart(bookingDtoRequest.getStart());
        booking.setEnd(bookingDtoRequest.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public BookingDtoForItem toItemBookingDto(Booking booking) {
        BookingDtoForItem bookingDtoForItem = new BookingDtoForItem();
        bookingDtoForItem.setId(booking.getId());
        bookingDtoForItem.setStart(booking.getStart());
        bookingDtoForItem.setEnd(booking.getEnd());
        bookingDtoForItem.setStatus(booking.getStatus());
        if (booking.getBooker() != null) {
            bookingDtoForItem.setBookerId(booking.getBooker().getId());
        }
        return bookingDtoForItem;
    }
}