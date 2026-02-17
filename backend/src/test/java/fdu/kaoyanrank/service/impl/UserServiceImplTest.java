package fdu.kaoyanrank.service.impl;

import fdu.kaoyanrank.constant.RedisConstants;
import fdu.kaoyanrank.constant.UserConstants;
import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.entity.User;
import fdu.kaoyanrank.exception.ServiceException;
import fdu.kaoyanrank.interceptor.IpInterceptor;
import fdu.kaoyanrank.mapper.UserMapper;
import fdu.kaoyanrank.service.UserService;
import fdu.kaoyanrank.utils.HmacUtil;
import fdu.kaoyanrank.utils.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @Test
    void testLogin_UserExists_Success() {
        UserDto userDto = new UserDto();
        final String TEST_IP = "127.0.0.1";
        userDto.setExamNo("102461234567890");
        userDto.setIdCard("310110200001011234");
        ThreadLocal<String> threadLocal = new ThreadLocal();
        threadLocal.set(TEST_IP);
        String token = userService.login(userDto);
        threadLocal.remove();
        assertNotNull(token);
    }

}
