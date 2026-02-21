package fdu.kaoyanrank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank(message = "考生编号不能为空")
    @Size(min = 1, max = 100, message = "考生编号长度必须在1到100之间")
    private String examNo;

    @NotBlank(message = "证件号码不能为空")
    @Size(min = 1, max = 100, message = "证件号码长度必须在1到100之间")
    private String idCard;
}
