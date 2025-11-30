package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                            @RequestHeader(USER_ID_HEADER) Long requestorId) {
        return itemRequestService.createItemRequest(itemRequestDto, requestorId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@PathVariable Long requestId,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getItemRequestById(requestId, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestsByUser(@RequestHeader(USER_ID_HEADER) Long requestorId) {
        return itemRequestService.getItemRequestsByUser(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getAllItemRequests(userId);
    }

    @DeleteMapping("/{requestsId}")
    public void deleteItemRequest(@PathVariable Long requestId,
                                  @RequestHeader(USER_ID_HEADER) Long requestorId) {
        itemRequestService.deleteItemRequest(requestId, requestorId);
    }
}
