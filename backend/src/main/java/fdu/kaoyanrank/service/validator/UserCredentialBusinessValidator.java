package fdu.kaoyanrank.service.validator;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.exception.ServiceException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class UserCredentialBusinessValidator implements BusinessValidator<UserDto> {

    private static final Pattern EXAM_NO_PATTERN = Pattern.compile("^\\d{15}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{16}[0-9Xx]$");
    private static final Set<String> PROVINCE_CODES = Set.of(
        "11", "12", "13", "14", "15", "21", "22", "23", "31", "32",
        "33", "34", "35", "36", "37", "41", "42", "43", "44", "45",
        "46", "50", "51", "52", "53", "54", "61", "62", "63", "64",
        "65", "71", "81", "82", "91"
    );
    private static final int[] CHECK_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    private static final DateTimeFormatter BIRTHDAY_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd")
        .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public void validate(UserDto userDto) {
        if (userDto == null) {
            throw new ServiceException(400, "用户信息不能为空");
        }

        String examNo = userDto.getExamNo();
        if (examNo == null || !EXAM_NO_PATTERN.matcher(examNo).matches()) {
            throw new ServiceException(400, "考生编号格式不正确");
        }

        String idCard = userDto.getIdCard();
        if (idCard == null || !isValidIdCard(idCard)) {
            throw new ServiceException(400, "证件号码格式不正确");
        }
    }

    private boolean isValidIdCard(String idCard) {
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }

        String normalized = idCard.toUpperCase();

        if (!isProvinceCodeValid(normalized)) {
            return false;
        }

        if (!isBirthdayValid(normalized)) {
            return false;
        }

        if (!isSequenceCodeValid(normalized)) {
            return false;
        }

        return isCheckCodeValid(normalized);
    }

    private boolean isProvinceCodeValid(String idCard) {
        return PROVINCE_CODES.contains(idCard.substring(0, 2));
    }

    private boolean isBirthdayValid(String idCard) {
        String birthday = idCard.substring(6, 14);
        try {
            LocalDate birthDate = LocalDate.parse(birthday, BIRTHDAY_FORMATTER);
            return !birthDate.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isSequenceCodeValid(String idCard) {
        return !"000".equals(idCard.substring(14, 17));
    }

    private boolean isCheckCodeValid(String idCard) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int num = idCard.charAt(i) - '0';
            sum += num * CHECK_WEIGHTS[i];
        }
        char expected = CHECK_CODES[sum % 11];
        return idCard.charAt(17) == expected;
    }
}
