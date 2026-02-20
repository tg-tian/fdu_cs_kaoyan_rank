package fdu.kaoyanrank.controller;

import fdu.kaoyanrank.common.Result;
import fdu.kaoyanrank.entity.ExamScore;
import fdu.kaoyanrank.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/all")
    public Result<List<ExamScore>> getAllScores() {
        List<ExamScore> allScores = scoreService.getAllScores();
        return Result.success(allScores);
    }

    @GetMapping("/me")
    public Result<ExamScore> getMyScore() {
        return Result.success(scoreService.getMyScore());
    }
}
