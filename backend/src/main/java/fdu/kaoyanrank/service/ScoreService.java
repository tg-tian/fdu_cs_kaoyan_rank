package fdu.kaoyanrank.service;

import fdu.kaoyanrank.entity.ExamScore;
import java.util.List;

public interface ScoreService {
    List<ExamScore> getAllScores();

    ExamScore getMyScore();
}
