package fdu.kaoyanrank.interceptor;

import fdu.kaoyanrank.constant.RedisConstants;
import fdu.kaoyanrank.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<String> EXAM_NO_HASH_THREAD_LOCAL = new ThreadLocal<>();

    public static String getExamNoHash() {
        return EXAM_NO_HASH_THREAD_LOCAL.get();
    }

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取 token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            return false;
        }

        // 校验 token
        String examNoHash = redisUtil.getIfPresent(RedisConstants.REDIS_TOKEN_PREFIX + token);
        if (!StringUtils.hasText(examNoHash)) {
            response.setStatus(401);
            return false;
        }

        EXAM_NO_HASH_THREAD_LOCAL.set(examNoHash);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        EXAM_NO_HASH_THREAD_LOCAL.remove();
    }
}
