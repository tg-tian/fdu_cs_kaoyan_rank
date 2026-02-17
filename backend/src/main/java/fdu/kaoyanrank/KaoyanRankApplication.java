package fdu.kaoyanrank;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("fdu.kaoyanrank.mapper")
public class KaoyanRankApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaoyanRankApplication.class, args);
    }

}
