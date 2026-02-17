package fdu.kaoyanrank.controller;

import fdu.kaoyanrank.common.Result;
import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private  UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDto userDto) {
        String token = userService.login(userDto);
        return Result.success(token);
    }
}
