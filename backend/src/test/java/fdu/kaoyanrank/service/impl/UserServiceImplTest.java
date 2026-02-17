package fdu.kaoyanrank.service.impl;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @Test
    void testLogin_UserExists_Success() {
        UserDto userDto = new UserDto();
        String ip = "127.0.0.1";
        userDto.setExamNo("102461234567897");
        userDto.setIdCard("310110200001011334");
        SseEmitter emitter = new SseEmitter(30000L);
        assertDoesNotThrow(() -> userService.login(userDto, emitter, ip));
    }

}
