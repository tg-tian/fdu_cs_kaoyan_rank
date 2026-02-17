package fdu.kaoyanrank.mapper;

import fdu.kaoyanrank.entity.ExamScore;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamScoreMapper {
    int insert(ExamScore examScore);
}
