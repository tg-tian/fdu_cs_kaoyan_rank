package fdu.kaoyanrank.controller;

import fdu.kaoyanrank.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("FDU CS Kaoyan Rank Backend is running!");
    }
}
