package fdu.kaoyanrank.controller;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.interceptor.IpInterceptor;
import fdu.kaoyanrank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private  UserService userService;

    @PostMapping("/login")
    public SseEmitter login(@Valid @RequestBody UserDto userDto) {
        SseEmitter emitter = new SseEmitter(300000L);
        String ip = IpInterceptor.getIp();
        userService.login(userDto, emitter, ip);
        return emitter;
    }
}
