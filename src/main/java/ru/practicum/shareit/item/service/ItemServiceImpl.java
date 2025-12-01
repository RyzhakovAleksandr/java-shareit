package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с ID %d не найден", ownerId)));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new ValidationException(String.format("Запрос с ID %d не найден", itemDto.getRequestId())));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена", itemId)));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoWithBookings getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена", id)));

        ItemDtoWithBookings.BookingInfo lastBooking = null;
        ItemDtoWithBookings.BookingInfo nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> pastBookings = bookingRepository.findPastBookingsForItem(id);
            if (!pastBookings.isEmpty()) {
                Booking last = pastBookings.getFirst();
                lastBooking = new ItemDtoWithBookings.BookingInfo(last.getId(), last.getBooker().getId(), last.getStart(), last.getEnd());
            }

            List<Booking> futureBookings = bookingRepository.findFutureBookingsForItem(id);
            if (!futureBookings.isEmpty()) {
                Booking next = futureBookings.getFirst();
                nextBooking = new ItemDtoWithBookings.BookingInfo(next.getId(), next.getBooker().getId(), next.getStart(), next.getEnd());
            }
        }

        List<CommentDto> comments = commentRepository.findCommentDtosByItemId(id);

        return new ItemDtoWithBookings(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                lastBooking,
                nextBooking,
                comments
        );
    }

    @Override
    public List<ItemDtoWithBookings> getItemsByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с ID %d не найден", ownerId)));

        List<Item> items = itemRepository.findByOwnerId(ownerId);

        return items.stream()
                .map(item -> {
                    List<Booking> pastBookings = bookingRepository.findPastBookingsForItem(item.getId());
                    List<Booking> futureBookings = bookingRepository.findFutureBookingsForItem(item.getId());

                    ItemDtoWithBookings.BookingInfo lastBooking = null;
                    ItemDtoWithBookings.BookingInfo nextBooking = null;

                    if (!pastBookings.isEmpty()) {
                        Booking last = pastBookings.getFirst();
                        lastBooking = new ItemDtoWithBookings.BookingInfo(last.getId(), last.getBooker().getId(), last.getStart(), last.getEnd());
                    }

                    if (!futureBookings.isEmpty()) {
                        Booking next = futureBookings.getFirst();
                        nextBooking = new ItemDtoWithBookings.BookingInfo(next.getId(), next.getBooker().getId(), next.getStart(), next.getEnd());
                    }

                    List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toList());

                    return new ItemDtoWithBookings(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.isAvailable(),
                            item.getRequest() != null ? item.getRequest().getId() : null,
                            lastBooking,
                            nextBooking,
                            comments
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long id, Long ownerId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена", id)));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец может удалить вещь");
        }

        itemRepository.deleteById(id);
    }

    @Override
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long authorId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена", itemId)));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с ID %d не найден", authorId)));

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(
                itemId, authorId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }
}
