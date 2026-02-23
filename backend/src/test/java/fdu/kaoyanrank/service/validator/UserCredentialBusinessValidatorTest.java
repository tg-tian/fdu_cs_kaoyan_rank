package fdu.kaoyanrank.service.validator;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.exception.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserCredentialBusinessValidatorTest {

    private final UserCredentialBusinessValidator validator = new UserCredentialBusinessValidator();

    @Test
    void shouldPassWhenCredentialFormatIsValid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("11010519491231002X");

        assertDoesNotThrow(() -> validator.validate(userDto));
    }

    @Test
    void shouldThrowWhenExamNoFormatIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("12345ABC");
        userDto.setIdCard("11010519491231002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardFormatIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("invalid-id-card");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenUserDtoIsNull() {
        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(null));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenExamNoIsNull() {
        UserDto userDto = new UserDto();
        userDto.setExamNo(null);
        userDto.setIdCard("11010519491231002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardIsNull() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenExamNoHasFourteenDigits() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("12345678901234");
        userDto.setIdCard("11010519491231002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenExamNoHasSixteenDigits() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("1234567890123456");
        userDto.setIdCard("11010519491231002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenExamNoHasFifteenCharactersButNonNumeric() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("12345678901234A");
        userDto.setIdCard("11010519491231002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardStartsWithZero() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("01010519491231002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardHasSeventeenDigits() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("11010519491231002");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardHasNineteenDigits() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("11010519491231002X1");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldPassWhenIdCardEndsWithLowercaseX() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("11010519491231002x");

        assertDoesNotThrow(() -> validator.validate(userDto));
    }

    @Test
    void shouldThrowWhenIdCardHasFifteenDigits() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("110101900101123");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardChecksumIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("110105194912310021");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardBirthdayIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("11010519990230002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardProvinceCodeIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("99010519491231002X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldThrowWhenIdCardSequenceCodeIsAllZero() {
        UserDto userDto = new UserDto();
        userDto.setExamNo("123456789012345");
        userDto.setIdCard("11010519491231000X");

        ServiceException exception = assertThrows(ServiceException.class, () -> validator.validate(userDto));
        assertEquals(400, exception.getCode());
    }
}
