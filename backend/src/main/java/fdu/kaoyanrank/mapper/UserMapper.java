package fdu.kaoyanrank.mapper;

import fdu.kaoyanrank.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByExamNoHashAndIdCardHash(@Param("examNoHash") String examNoHash, @Param("idCardHash") String idCardHash);

    int insert(User user);
}
