package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController itemRequestController;

    @Test
    void createRequest() {
        ItemRequestCreateDto request = new ItemRequestCreateDto();
        ItemRequestResponseDto expected = new ItemRequestResponseDto();
        when(itemRequestService.createRequest(1L, request)).thenReturn(expected);

        ItemRequestResponseDto result = itemRequestController.createRequest(request, 1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getUserRequests() {
        List<ItemRequestResponseDto> expected = Collections.singletonList(new ItemRequestResponseDto());
        when(itemRequestService.getUserRequests(1L)).thenReturn(expected);

        List<ItemRequestResponseDto> result = itemRequestController.getUserRequests(1L);

        assertThat(result).isEqualTo(expected);
    }
}