package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShareItServerTest {

    @Test
    void contextLoads() {
        // Просто проверяем что контекст загружается
        assertTrue(true);
    }

    @Test
    void mainMethodStarts() {
        // Тест для метода main
        ShareItServer.main(new String[]{});
    }
}