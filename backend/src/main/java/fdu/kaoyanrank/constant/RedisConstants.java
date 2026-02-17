package fdu.kaoyanrank.constant;

public class RedisConstants {

    /**
     * 登录 Token 前缀
     */
    public static final String REDIS_TOKEN_PREFIX = "login:token:";

    /**
     * 登录失败 IP 限制前缀
     */
    public static final String REDIS_IP_FAIL_PREFIX = "login:fail:ip:";

    /**
     * 最大失败次数
     */
    public static final int MAX_FAIL_COUNT = 10;

    /**
     * Token 过期时间（小时）
     */
    public static final long TOKEN_EXPIRE_HOURS = 24;

    public static final String SCORE_SERVICE_RATE_LIMIT_KEY = "score:rate:limiter";

    public static final long SCORE_SERVICE_RATE_LIMIT_PERMITS = 10;

    public static final long SCORE_SERVICE_RATE_LIMIT_INTERVAL_SECONDS = 1;
}
