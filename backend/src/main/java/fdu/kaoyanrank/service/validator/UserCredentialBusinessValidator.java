package fdu.kaoyanrank.service.validator;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.exception.ServiceException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserCredentialBusinessValidator implements BusinessValidator<UserDto> {

    private static final Pattern EXAM_NO_PATTERN = Pattern.compile("^\\d{15}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^(?:[1-9]\\d{14}|[1-9]\\d{16}[0-9Xx])$");

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
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw new ServiceException(400, "证件号码格式不正确");
        }
    }
}
