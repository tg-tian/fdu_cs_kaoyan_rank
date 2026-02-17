package fdu.kaoyanrank.interceptor;

import fdu.kaoyanrank.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class IpInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<String> IP_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String ip = IpUtil.getIpAddr(request);
        IP_THREAD_LOCAL.set(ip);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        IP_THREAD_LOCAL.remove();
    }
}
