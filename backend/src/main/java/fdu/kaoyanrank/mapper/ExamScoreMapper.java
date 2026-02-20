package fdu.kaoyanrank.mapper;

import fdu.kaoyanrank.entity.ExamScore;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ExamScoreMapper {
    int insert(ExamScore examScore);

    List<ExamScore> selectAllScores();

    ExamScore selectByExamNoHash(String examNoHash);
}
