package fdu.kaoyanrank.mapper;

import fdu.kaoyanrank.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByExamNoHashAndIdCardHash(@Param("examNoHash") String examNoHash, @Param("idCardHash") String idCardHash);

    int insert(User user);

    @Select("SELECT id, created_at FROM user")
    List<User> findAllIdAndCreatedAt();
}
