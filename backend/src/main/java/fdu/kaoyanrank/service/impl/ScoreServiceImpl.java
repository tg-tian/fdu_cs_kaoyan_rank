package fdu.kaoyanrank.service.impl;

import fdu.kaoyanrank.entity.ExamScore;
import fdu.kaoyanrank.interceptor.LoginInterceptor;
import fdu.kaoyanrank.mapper.ExamScoreMapper;
import fdu.kaoyanrank.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ExamScoreMapper examScoreMapper;

    @Override
    public List<ExamScore> getAllScores() {
        return examScoreMapper.selectAllScores();
    }

    @Override
    public ExamScore getMyScore() {
        String examNoHash = LoginInterceptor.getExamNoHash();
        return examScoreMapper.selectByExamNoHash(examNoHash);
    }
}
