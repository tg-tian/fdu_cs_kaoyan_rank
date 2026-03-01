package fdu.kaoyanrank.service;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.entity.User;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface UserService {
    void login(UserDto userDto, SseEmitter emitter, String ip);
    List<User> getAllUserCreateTimes();
}
