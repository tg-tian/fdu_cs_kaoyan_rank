package fdu.kaoyanrank.service.validator;

import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialBusinessValidator implements BusinessValidator<UserDto> {

    private static final String EXAM_NO_REGEX = "^\\d{15}$";
    private static final String ID_CARD_REGEX = "^(?:[1-9]\\d{14}|[1-9]\\d{16}[0-9Xx])$";

    @Override
    public void validate(UserDto userDto) {
        if (userDto == null) {
            throw new ServiceException(400, "用户信息不能为空");
        }

        String examNo = userDto.getExamNo();
        if (examNo == null || !examNo.matches(EXAM_NO_REGEX)) {
            throw new ServiceException(400, "考生编号格式不正确");
        }

        String idCard = userDto.getIdCard();
        if (idCard == null || !idCard.matches(ID_CARD_REGEX)) {
            throw new ServiceException(400, "证件号码格式不正确");
        }
    }
}
