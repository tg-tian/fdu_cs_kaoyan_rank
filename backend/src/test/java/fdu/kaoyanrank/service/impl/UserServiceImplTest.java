package fdu.kaoyanrank.service.impl;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @Test
    void testLogin_UserExists_Success() {
        UserDto userDto = new UserDto();
        String ip = "127.0.0.1";
        userDto.setExamNo("223451789012345");
        userDto.setIdCard("311111111111111111");
        SseEmitter emitter = new SseEmitter(30000L);
        assertDoesNotThrow(() -> userService.login(userDto, emitter, ip));
    }

    @Test
    void testLogin_ConcurrentLoad() throws InterruptedException {
        int count = 200;
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            UserDto userDto = new UserDto();
            String examNo = "10246" + String.format("%010d", ThreadLocalRandom.current().nextLong(10000000000L));
            String idCard = "310115" + String.format("%012d", ThreadLocalRandom.current().nextLong(1000000000000L));

            userDto.setExamNo(examNo);
            userDto.setIdCard(idCard);

            SseEmitter emitter = new SseEmitter(30000L);
            emitter.onCompletion(latch::countDown);
            emitter.onTimeout(latch::countDown);
            emitter.onError((e) -> latch.countDown());

            String ip = "127.0.0.1";

            assertDoesNotThrow(() -> userService.login(userDto, emitter, ip));
        }

        // Wait for all tasks to complete (or timeout)
        latch.await(30, TimeUnit.SECONDS);
    }
}
