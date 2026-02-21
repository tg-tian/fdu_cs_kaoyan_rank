package fdu.kaoyanrank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
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
        UserDto userDto = new UserDto();
        userDto.setExamNo("102461234567890");
        userDto.setIdCard("310110200001011234");

        doNothing().when(userService).login(any(UserDto.class), any(SseEmitter.class), any(String.class));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginValidationFail() throws Exception {
        UserDto userDto = new UserDto();

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void testLoginValidationFailWhenExamNoWhitespaceOnly() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setExamNo("   ");
        userDto.setIdCard("310110200001011234");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("考生编号不能为空"));

        verifyNoInteractions(userService);
    }

    @Test
    void testLoginValidationFailWhenExamNoTooLong() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setExamNo("1".repeat(101));
        userDto.setIdCard("310110200001011234");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("考生编号长度必须在1到100之间"));

        verifyNoInteractions(userService);
    }

    @Test
    void testLoginValidationFailWhenIdCardTooLong() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setExamNo("102461234567890");
        userDto.setIdCard("1".repeat(101));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("证件号码长度必须在1到100之间"));

        verifyNoInteractions(userService);
    }
}
