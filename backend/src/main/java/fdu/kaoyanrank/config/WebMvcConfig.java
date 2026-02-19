package fdu.kaoyanrank.config;

import fdu.kaoyanrank.constant.RedisConstants;
import fdu.kaoyanrank.interceptor.IpInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final IpInterceptor ipInterceptor;
    private final RedissonClient redissonClient;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ipInterceptor)
                .addPathPatterns("/user/login");
    }

    @Bean
    public ExecutorService virtualThreadTaskExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vt-", 0).factory());
    }

    @PostConstruct
    public void initRateLimiter() {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(RedisConstants.SCORE_SERVICE_RATE_LIMIT_KEY);
        rateLimiter.setRate(
                RateType.OVERALL,
                RedisConstants.SCORE_SERVICE_RATE_LIMIT_PERMITS,
                RedisConstants.SCORE_SERVICE_RATE_LIMIT_INTERVAL_SECONDS,
                RateIntervalUnit.SECONDS
        );
    }
}
