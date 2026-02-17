package fdu.kaoyanrank.service;

import fdu.kaoyanrank.dto.UserDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface UserService {
    void login(UserDto userDto, SseEmitter emitter, String ip);
}
