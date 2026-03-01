package fdu.kaoyanrank.service.impl;

import fdu.kaoyanrank.constant.RedisConstants;
import fdu.kaoyanrank.constant.UserConstants;
import fdu.kaoyanrank.dto.UserDto;
import fdu.kaoyanrank.entity.ExamScore;
import fdu.kaoyanrank.entity.User;
import fdu.kaoyanrank.exception.ServiceException;
import fdu.kaoyanrank.grpc.GetScoreRequest;
import fdu.kaoyanrank.grpc.GetScoreResponse;
import fdu.kaoyanrank.grpc.ScoreServiceGrpc;
import fdu.kaoyanrank.mapper.ExamScoreMapper;
import fdu.kaoyanrank.mapper.UserMapper;
import fdu.kaoyanrank.service.UserService;
import fdu.kaoyanrank.service.validator.BusinessValidator;
import fdu.kaoyanrank.utils.HmacUtil;
import fdu.kaoyanrank.utils.IpUtil;
import fdu.kaoyanrank.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ExamScoreMapper examScoreMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HmacUtil hmacUtil;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private BusinessValidator<UserDto> userCredentialBusinessValidator;

    @Value("${grpc.client.scoreService.address}")
    private String scoreServiceAddress;

    @Override
    public List<User> getAllUserCreateTimes() {
        return userMapper.findAllIdAndCreatedAt();
    }

    private List<ManagedChannel> channels = new ArrayList<>();
    private List<ScoreServiceGrpc.ScoreServiceBlockingStub> stubs = new ArrayList<>();
    private final AtomicInteger pollIndex = new AtomicInteger(0);
    private static final int CHANNEL_COUNT = 5;

    @PostConstruct
    public void init() {
        // 保留 static:// 前缀，以便 StaticNameResolverProvider 处理
        String target = scoreServiceAddress;
        
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            ManagedChannel channel = NettyChannelBuilder.forTarget(target)
                    .defaultLoadBalancingPolicy("round_robin")
                    .usePlaintext()
                    .maxInboundMetadataSize(1024 * 1024) // 1MB
                    .build();
            channels.add(channel);
            stubs.add(ScoreServiceGrpc.newBlockingStub(channel));
        }
    }

    @PreDestroy
    public void destroy() {
        for (ManagedChannel channel : channels) {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown();
            }
        }
    }

    @Override
    @Async("virtualThreadTaskExecutor")
    public void login(UserDto userDto, SseEmitter emitter, String ip){
        try {
            userCredentialBusinessValidator.validate(userDto);
            ipLimitCheck(ip);
            // Hash
            String examNoHash = hmacUtil.hmacSha256Hex(userDto.getExamNo());
            String idCardHash = hmacUtil.hmacSha256Hex(userDto.getIdCard());
            User user = userMapper.findByExamNoHashAndIdCardHash(examNoHash, idCardHash);
            if (user != null) {
                if (UserConstants.DISABLED == user.getStatus())
                    throw new ServiceException("账号已被禁用");
            } else {
                emitter.send(SseEmitter.event().name("info").data("校验完成，排队查询成绩"));
                waitForRateLimit(emitter);
                emitter.send(SseEmitter.event().name("info").data("开始查询成绩"));
                log.info("开始查询成绩");
                GetScoreResponse response = fetchScore(userDto);
                log.info("远程查询成绩结果: {}", response.getMessage());
                if (response == null || !response.getSuccess()) {
                    // 远程查询失败，记录 IP 失败次数
                    String k = RedisConstants.REDIS_IP_FAIL_PREFIX + ip;
                    if (Boolean.FALSE.equals(redisUtil.setIfAbsent(k, "1", 24, TimeUnit.HOURS))) {
                        redisUtil.increment(k, 1);
                    }
                    throw new ServiceException( "查询成绩失败," + response.getMessage());
                }
                user = buildNewUser(examNoHash, idCardHash);
                UserServiceImpl proxy = applicationContext.getBean(UserServiceImpl.class);
                proxy.saveUserWithScores(user, response);
            }
            // 生成 Token
            String token = UUID.randomUUID().toString().replace("-", "");
            redisUtil.set(RedisConstants.REDIS_TOKEN_PREFIX + token, user.getExamNoHash(), RedisConstants.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            emitter.send(SseEmitter.event().name("success").data(token));
            emitter.complete();
        }catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private void ipLimitCheck(String ip) {
        log.info("登录请求 IP: {}", ip);
        if(IpUtil.ipWhitelist.contains(ip))
            return;
        String key = RedisConstants.REDIS_IP_FAIL_PREFIX + ip;
        String countStr = redisUtil.getIfPresent(key);
        if (countStr != null && Integer.parseInt(countStr) >= RedisConstants.MAX_FAIL_COUNT) {
            throw new ServiceException("登录失败次数过多，请明天再试");
        }
    }

    private void waitForRateLimit(SseEmitter emitter) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(RedisConstants.SCORE_SERVICE_RATE_LIMIT_KEY);
        String queueKey = RedisConstants.SCORE_SERVICE_QUEUE_COUNT_KEY;
        redisUtil.increment(queueKey, 1);
        try {
            while (!rateLimiter.tryAcquire(1)) {
                long queueCount = getQueueCount(queueKey);
                log.info("当前排队人数: {}", queueCount);
                emitter.send(SseEmitter.event().name("info").data("当前排队人数: " + queueCount));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ServiceException("请求处理中断");
                }
            }
        }
        catch (Exception e) {
            emitter.completeWithError(e);
        }
        finally
        {
            Long left = redisUtil.increment(queueKey, -1);
            if (left != null && left < 0) {
                redisUtil.increment(queueKey, -left);
            }
        }
    }

    private long getQueueCount(String queueKey) {
        String queueCountStr = redisUtil.getIfPresent(queueKey);
        if (!StringUtils.hasText(queueCountStr)) {
            return 0;
        }
        return Long.parseLong(queueCountStr);
    }

    private GetScoreResponse fetchScore(UserDto userDto) {
        try {
            GetScoreRequest request = GetScoreRequest.newBuilder()
                    .setExamNo(userDto.getExamNo())
                    .setIdCard(userDto.getIdCard())
                    .build();
            
            // 轮询选择一个 Stub
            int index = (pollIndex.getAndIncrement() & 0x7FFFFFFF) % stubs.size();
            return stubs.get(index).getScore(request);
        } catch (Exception e) {
            log.error("scoreService远程调用失败", e);
            throw new ServiceException("官网查询成绩失败");
        }
    }

    private User buildNewUser(String examNoHash, String idCardHash) {
        User user = new User();
        user.setExamNoHash(examNoHash);
        user.setIdCardHash(idCardHash);
        user.setRole(UserConstants.Role.USER);
        user.setStatus(UserConstants.ACTIVE);
        return user;
    }

    @Transactional
    public void saveUserWithScores(User user, GetScoreResponse response) {
        try {
            userMapper.insert(user);
            Map<String, Integer> scores = response.getTotalScoreMap();
            ExamScore examScore = new ExamScore();
            examScore.setExamNoHash(user.getExamNoHash());
            examScore.setEnglishScore(scores.get("english"));
            examScore.setPoliticsScore(scores.get("politics"));
            examScore.setMathScore(scores.get("math"));
            examScore.setScore408(scores.get("408"));
            examScoreMapper.insert(examScore);
        } catch (Exception e) {
            log.error("保存用户信息失败", e);
            throw new ServiceException("用户信息保存失败");
        }
    }


}
