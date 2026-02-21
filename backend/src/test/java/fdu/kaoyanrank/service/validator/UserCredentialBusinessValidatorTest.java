package fdu.kaoyanrank.service.validator;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.exception.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserCredentialBusinessValidatorTest {

    private final UserCredentialBusinessValidator validator = new UserCredentialBusinessValidator();

    @Test
    void shouldPassWhenCredentialFormatIsValid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("11010119900101123X");

        assertDoesNotThrow(() -> validator.validate(userDto));
    }

    @Test
    void shouldThrowWhenExamNoFormatIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("12345ABC");
        userDto.setIdCard("110101199001011234");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        org.junit.jupiter.api.Assertions.assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardFormatIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("invalid-id-card");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        org.junit.jupiter.api.Assertions.assertEquals(400, exception.getCode());
    }
}
