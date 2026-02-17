package fdu.kaoyanrank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.exception.ServiceException;
import fdu.kaoyanrank.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLoginSuccess() throws Exception {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setExamNo("102461234567890");
        userDto.setIdCard("310110200001011234");

        String expectedToken = "mock-token-123456";
        when(userService.login(any(UserDto.class))).thenReturn(expectedToken);

        // Act & Assert
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(expectedToken))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void testLoginValidationFail() throws Exception {
        // Arrange
        UserDto userDto = new UserDto();
        // Missing examNo and idCard

        when(userService.login(any(UserDto.class)))
                .thenThrow(new ServiceException(400, "考生编号和证件号码不能为空"));

        // Act & Assert
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk()) // GlobalExceptionHandler catches exception and returns 200 OK with error body
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("考生编号和证件号码不能为空"));
    }
}
