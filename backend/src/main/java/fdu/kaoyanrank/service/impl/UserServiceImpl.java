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
import fdu.kaoyanrank.utils.HmacUtil;
import fdu.kaoyanrank.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
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

    @GrpcClient("scoreService")
    private ScoreServiceGrpc.ScoreServiceBlockingStub scoreServiceStub;

    @Override
    @Transactional
    public String login(UserDto userDto) {
        String ip = new ThreadLocal<String>().get();

        if (!StringUtils.hasText(userDto.getExamNo()) || !StringUtils.hasText(userDto.getIdCard())) {
            throw new ServiceException("考生编号和证件号码不能为空");
        }

        // IP 限制检查
        String key = RedisConstants.REDIS_IP_FAIL_PREFIX + ip;
        String countStr = redisUtil.getIfPresent(key);
        if (countStr != null && Integer.parseInt(countStr) >= RedisConstants.MAX_FAIL_COUNT) {
            throw new ServiceException("登录失败次数过多，请明天再试");
        }

        // Hash
        String examNoHash = hmacUtil.hmacSha256Hex(userDto.getExamNo());
        String idCardHash = hmacUtil.hmacSha256Hex(userDto.getIdCard());

        User user = userMapper.findByExamNoHashAndIdCardHash(examNoHash, idCardHash);
        if (user != null) {
            if (UserConstants.DISABLED == user.getStatus()) {
                throw new ServiceException("账号已被禁用");
            }
        } else {
            // 用户不存在，首次登录，调用远程 gRPC 服务查询成绩
            GetScoreResponse response = null;
            try {
                GetScoreRequest request = GetScoreRequest.newBuilder()
                        .setExamNo(userDto.getExamNo())
                        .setIdCard(userDto.getIdCard())
                        .build();
                response = scoreServiceStub.getScore(request);
            } catch (Exception e) {
                log.error("官网查询成绩失败", e);
                throw new ServiceException("官网查询成绩失败");
            }

            if (response == null || !response.getSuccess()) {
                // 远程查询失败，记录 IP 失败次数
                String k = RedisConstants.REDIS_IP_FAIL_PREFIX + ip;
                if (Boolean.FALSE.equals(redisUtil.setIfAbsent(k, "1", 24, TimeUnit.HOURS))) {
                    redisUtil.increment(k, 1);
                }
                throw new ServiceException( "验证失败：考生信息不存在或不匹配");
            }

            // 远程验证通过，保存新用户
            user = new User();
            user.setExamNoHash(examNoHash);
            user.setIdCardHash(idCardHash);
            user.setRole(UserConstants.Role.USER);
            user.setStatus(UserConstants.ACTIVE);
            try {
                userMapper.insert(user);
                // 插入考试成绩
                Map<String, Integer> scores = response.getTotalScoreMap();
                ExamScore examScore = new ExamScore();
                examScore.setExamNoHash(examNoHash);
                examScore.setEnglishScore(scores.get("英语"));
                examScore.setPoliticsScore(scores.get("政治"));
                examScore.setMathScore(scores.get("数学"));
                examScore.setScore408(scores.get("408"));
                examScoreMapper.insert(examScore);
            } catch (Exception e) {
                log.error("保存用户信息失败", e);
                throw new ServiceException("用户信息保存失败");
            }
        }
        // 生成 Token
        String token = UUID.randomUUID().toString().replace("-", "");
        redisUtil.set(RedisConstants.REDIS_TOKEN_PREFIX + token, user.getExamNoHash(), RedisConstants.TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        return token;
    }

}
