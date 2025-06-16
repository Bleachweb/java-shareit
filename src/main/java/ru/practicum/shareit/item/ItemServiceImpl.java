package ru.practicum.shareit.item;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.comment.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    @Override
    public CreatedItemDtoResponse createItem(int userId, ItemDtoRequest itemDtoRequest) {

        User user = getUser(userId);
        Item createdItem = itemRepository.save(itemMapper.dtoToItem(itemDtoRequest, user, null));
        return itemMapper.createdItemDtoResponse(createdItem);
    }

    @Override
    public ItemDtoResponse updateItem(int userId, int itemId, ItemDtoRequest itemDtoRequest) {

        User oldUser = getUser(userId);
        Item oldItem = getItem(itemId);

        if (oldItem.getUser().getId() != userId) {
            log.warn("Пользователь с id {} не является владельцем вещи с id {}", userId, itemId);
            throw new ValidationException("Пользователь не является владельцем вещи");
        }
        if (itemDtoRequest.getName() == null || itemDtoRequest.getName().isEmpty()) {
            itemDtoRequest.setName(oldItem.getName());
        }
        if (itemDtoRequest.getDescription() == null || itemDtoRequest.getDescription().isEmpty()) {
            itemDtoRequest.setDescription(oldItem.getDescription());
        }
        if (itemDtoRequest.getAvailable() == null) {
            itemDtoRequest.setAvailable(oldItem.isAvailable());
        }

        Item updatedItem = itemRepository.save(itemMapper.dtoToItem(itemDtoRequest, oldUser, itemId));

        return itemMapper.itemToDtoResponse(updatedItem);
    }

    @Override
    public ItemDto getItemById(int userId, int itemId) {
        getUser(userId);
        Item item = getItem(itemId);
        ItemDtoResponse itemResponse = itemMapper.itemToDtoResponse(item);

        List<CommentDtoResponse> commentResponses = commentRepository.getAllCommentsByItemId(itemId)
                .stream()
                .map(commentMapper::commentToDtoResponse)
                .toList();

        Booking nextBooking = getNextBooking(userId);
        Booking lastBooking = getLastBooking(userId);

        return itemMapper.toItemDto(userId, itemResponse, commentResponses,
                bookingMapper.toItemBookingDto(nextBooking),
                bookingMapper.toItemBookingDto(lastBooking));
    }

    @Override
    public List<ItemDto> getItems(int userId) {
        User user = getUser(userId);

        List<Item> items = itemRepository.findAllItemsByUserOrderByIdAsc(user);

        List<Booking> bookings = bookingRepository.findAllBookingsByOwnerWithItemsAndBookers(userId);
        List<Comment> comments = commentRepository.findAllCommentsByOwnerWithItemsAndAuthors(userId);

        Map<Integer, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Integer, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        List<ItemDto> itemsDto = new ArrayList<>();

        for (Item item : items) {
            ItemDtoResponse itemResponse = itemMapper.itemToDtoResponse(item);

            List<CommentDtoResponse> commentResponses = commentsByItem.getOrDefault(item.getId(), Collections.emptyList())
                    .stream()
                    .map(commentMapper::commentToDtoResponse)
                    .toList();

            List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());

            Booking nextBooking = itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);

            Booking lastBooking = itemBookings.stream()
                    .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);

            BookingDtoForItem nextBookingDto = nextBooking != null ?
                    bookingMapper.toItemBookingDto(nextBooking) : null;
            BookingDtoForItem lastBookingDto = lastBooking != null ?
                    bookingMapper.toItemBookingDto(lastBooking) : null;

            ItemDto itemDto = itemMapper.toItemDto(userId, itemResponse, commentResponses,
                    nextBookingDto, lastBookingDto);
            itemsDto.add(itemDto);
        }
        return itemsDto;
    }

    @Override
    public List<ItemDtoResponse> searchItems(int userId, String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.search(text);

        return items.stream()
                .map(itemMapper::itemToDtoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDtoResponse addComment(int userId, int itemId, CommentDtoRequest commentDtoRequest) {

        User user = getUser(userId);
        Item item = getItem(itemId);
        Set<Integer> itemIds = new HashSet<>();
        itemIds.add(itemId);

        List<Booking> bookings = bookingRepository.findAllBookingsByItemIdInOrderByStartDesc(itemIds);

        if (bookings.isEmpty()) {
            log.warn("Для вещи с id {}  бронирования не было", itemId);
            throw new ValidationException("Для вещи с id: " + itemId + " бронирования не было");
        }

        Booking booking = bookings.getFirst();

        if (booking.getBooker().getId() != userId || booking.getEnd().isAfter(LocalDateTime.now())) {
            log.warn("Пользователь с id {} не бронировал вещь с id {} или срок бронирования не истек", userId, itemId);
            throw new ValidationException("Пользователь с id: " + userId +
                    " не бронировал вещь с id: " + itemId +
                    " или срок бронирования не истек");
        }

        Comment comment = commentMapper.dtoToComment(commentDtoRequest, item, user);
        comment.setCreated(LocalDateTime.now());

        return commentMapper.commentToDtoResponse(commentRepository.save(comment));
    }

    private User getUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + userId + " не найден"));
    }

    private Item getItem(int itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id - " + itemId + " не найден"));
    }

    private Booking getNextBooking(int userId) {
        List<Booking> bookings = bookingRepository
                .findAllBookingsByItemUserIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());

        Booking booking = new Booking();

        if (bookings != null && !bookings.isEmpty()) {
            booking = bookings.getFirst();
        }
        return booking;
    }

    private Booking getLastBooking(int userId) {
        List<Booking> bookings = bookingRepository
                .findAllBookingsByItemUserIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());

        Booking booking = new Booking();

        if (bookings != null && !bookings.isEmpty()) {
            booking = bookings.getFirst();
        }
        return booking;
    }
}